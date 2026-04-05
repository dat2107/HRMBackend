package com.toto.backend.service;

import com.toto.backend.dto.object.CategoryPeriodDTO;
import com.toto.backend.dto.object.FeedbackStatus;
import com.toto.backend.entity.LookupCategory;
import com.toto.backend.entity.LookupFeedback;
import com.toto.backend.exception.AppException;
import com.toto.backend.exception.ErrorCode;
import com.toto.backend.repository.LookupCategoryRepository;
import com.toto.backend.repository.LookupFeedbackRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final LookupCategoryRepository categoryRepository;
    private final LookupFeedbackRepository feedbackRepository;
    private final ExcelParserService excelParserService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Map<String, List<CategoryPeriodDTO>> getCategories() {
        List<LookupCategory> categories = categoryRepository.findByStatusOrderByCategoryNameAsc(1);
        Map<String, List<CategoryPeriodDTO>> result = new LinkedHashMap<>();

        for (LookupCategory cat : categories) {

            String period = cat.getPeriod() != null
                    ? cat.getPeriod().format(DATE_FMT)
                    : "Mặc định";

            String name = (cat.getCategoryName() == null || cat.getCategoryName().isBlank())
                    ? "Khác"
                    : cat.getCategoryName();

            result.computeIfAbsent(name, k -> new ArrayList<>())
                    .add(CategoryPeriodDTO.builder()
                            .categoryId(cat.getId())
                            .categoryName(cat.getCategoryName())
                            .period(period)
                            .feedbackType(cat.getFeedbackType())
                            .customOptions(cat.getCustomOptions())
                            .isPriority(Boolean.TRUE.equals(cat.getIsPriority()))
                            .build());
        }

        // Sort newest first
        result.forEach((k, v) ->
                v.sort(Comparator.comparingLong(CategoryPeriodDTO::getCategoryId).reversed())
        );

        return result;
    }

    public FeedbackStatus getFeedback(String employeeId, Long categoryId) {
        return feedbackRepository.findByEmployeeIdAndCategoryId(employeeId, categoryId)
                .map(f -> FeedbackStatus.builder()
                        .status(f.getStatusValue())
                        .note(f.getNote())
                        .build())
                .orElse(FeedbackStatus.builder().status("").note("").build());
    }

    @Transactional
    public LookupCategory createCategory(String categoryName, String fileLink,
                                         LocalDate period, String feedbackType,
                                         String customOptions, boolean isPriority,
                                         MultipartFile file) {
        String dataJson = null;
        if (file != null && !file.isEmpty()) {
            dataJson = excelParserService.parseToJson(file);
        }

        LookupCategory cat = LookupCategory.builder()
                .categoryName(categoryName.trim())
                .fileLink(fileLink)
                .period(period)
                .feedbackType(feedbackType != null ? feedbackType.toUpperCase() : "NONE")
                .customOptions(customOptions)
                .isPriority(isPriority)
                .dataJson(dataJson)
                .status(1)
                .build();
        return categoryRepository.save(cat);
    }

    public Map<String, Object> getCategoryData(Long categoryId, String employeeId) {
        LookupCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new com.toto.backend.exception.AppException(
                        com.toto.backend.exception.ErrorCode.DATA_NOT_FOUND));

        if (cat.getDataJson() == null || cat.getDataJson().isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("headers", List.of());
            empty.put("rows", List.of());
            return empty;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(cat.getDataJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<>() {});

            if (employeeId != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> rows = (List<Map<String, String>>) data.get("rows");
                if (rows != null) {
                    List<Map<String, String>> filtered = rows.stream()
                            .filter(row -> row.values().stream()
                                    .anyMatch(v -> v != null && v.equalsIgnoreCase(employeeId)))
                            .collect(java.util.stream.Collectors.toList());
                    data.put("rows", filtered);
                }
            }

            return data;
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("headers", List.of());
            err.put("rows", List.of());
            return err;
        }
    }

    @Transactional
    public void submitFeedback(String employeeId, Long categoryId, String status, String note) {
        LookupCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        LookupFeedback feedback = feedbackRepository
                .findByEmployeeIdAndCategoryId(employeeId, categoryId)
                .orElse(LookupFeedback.builder()
                        .employeeId(employeeId)
                        .category(category)
                        .build());

        feedback.setStatusValue(status);
        feedback.setNote(note);
        feedback.setUpdatedAt(java.time.LocalDateTime.now());
        feedbackRepository.save(feedback);
    }
}
