package com.toto.backend.controller;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.dto.request.AdminReviewRequest;
import com.toto.backend.entity.UpdateRequest;
import com.toto.backend.security.SecurityUtils;
import com.toto.backend.service.AdminService;
import com.toto.backend.service.LookupService;
import com.toto.backend.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final LookupService lookupService;
    private final MinioService minioService;
    private final BaseResponseFactory responseFactory;

    @GetMapping("/requests")
    public BaseResponse<List<UpdateRequest>> getRequests(
            @RequestParam(defaultValue = "PENDING") String status) {
        return responseFactory.success("success.default", adminService.getRequests(status));
    }

    @PostMapping("/requests/{id}/approve")
    public BaseResponse<Void> approve(@PathVariable Long id) {
        String adminId = SecurityUtils.getCurrentEmployeeId();
        adminService.approveRequest(id, adminId);
        return responseFactory.success("success.default", null);
    }

    @PostMapping("/requests/{id}/reject")
    public BaseResponse<Void> reject(@PathVariable Long id,
                                     @RequestBody(required = false) AdminReviewRequest body) {
        String adminId = SecurityUtils.getCurrentEmployeeId();
        String note = body != null ? body.getAdminNote() : "";
        adminService.rejectRequest(id, adminId, note);
        return responseFactory.success("success.default", null);
    }

    @PostMapping(value = "/lookup/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> uploadLookupFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam String categoryName,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "NONE") String feedbackType,
            @RequestParam(required = false) String customOptions,
            @RequestParam(defaultValue = "false") boolean isPriority
    ) {
        String fileLink = minioService.uploadMultipartFile(file, "lookup");

        LocalDate periodDate = null;
        if (period != null && !period.isBlank()) {
            try {
                periodDate = LocalDate.parse(period);
            } catch (Exception ignored) {}
        }

        lookupService.createCategory(categoryName, fileLink, periodDate, feedbackType, customOptions, isPriority, file);
        return responseFactory.success("success.default", null);
    }
}
