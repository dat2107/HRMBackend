package com.toto.backend.controller;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.dto.object.CategoryPeriodDTO;
import com.toto.backend.dto.object.FeedbackStatus;
import com.toto.backend.security.SecurityUtils;
import com.toto.backend.service.LookupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;
    private final BaseResponseFactory responseFactory;

    @GetMapping("/categories")
    public BaseResponse<Map<String, List<CategoryPeriodDTO>>> getCategories() {
        return responseFactory.success("success.default", lookupService.getCategories());
    }

    @GetMapping("/categories/{id}/data")
    public BaseResponse<Map<String, Object>> getCategoryData(@PathVariable Long id) {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        return responseFactory.success("success.default", lookupService.getCategoryData(id, employeeId));
    }

    @GetMapping("/feedback/{categoryId}")
    public BaseResponse<FeedbackStatus> getFeedback(@PathVariable Long categoryId) {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        return responseFactory.success("success.default",
                lookupService.getFeedback(employeeId, categoryId));
    }

    @PostMapping("/feedback/{categoryId}")
    public BaseResponse<Void> submitFeedback(
            @PathVariable Long categoryId,
            @RequestBody FeedbackRequest request
    ) {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        lookupService.submitFeedback(employeeId, categoryId, request.getStatus(), request.getNote());
        return responseFactory.success("success.default", null);
    }

    @Data
    public static class FeedbackRequest {
        private String status;
        private String note;
    }
}
