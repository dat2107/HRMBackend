package com.toto.backend.service;

import com.toto.backend.dto.object.CategoryPeriodDTO;
import com.toto.backend.dto.object.FeedbackStatus;
import com.toto.backend.entity.LookupCategory;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LookupService {
    Map<String, List<CategoryPeriodDTO>> getCategories();
    FeedbackStatus getFeedback(String employeeId, Long categoryId);
    LookupCategory createCategory(String categoryName, String fileLink, LocalDate period,
                                  String feedbackType, String customOptions, boolean isPriority,
                                  MultipartFile file);
    Map<String, Object> getCategoryData(Long categoryId, String employeeId);
    void submitFeedback(String employeeId, Long categoryId, String status, String note);
}
