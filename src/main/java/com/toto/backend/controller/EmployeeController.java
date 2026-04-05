package com.toto.backend.controller;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.dto.request.UpdateRequestDto;
import com.toto.backend.dto.response.EmployeeProfileResponse;
import com.toto.backend.security.SecurityUtils;
import com.toto.backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final BaseResponseFactory responseFactory;

    @GetMapping("/profile")
    public BaseResponse<EmployeeProfileResponse> getProfile() {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        return responseFactory.success("success.default", employeeService.getProfile(employeeId));
    }

    @PostMapping("/update-request")
    public BaseResponse<Void> submitUpdateRequest(@Valid @RequestBody UpdateRequestDto dto) {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        employeeService.submitUpdateRequest(employeeId, dto);
        return responseFactory.success("success.default", null);
    }

    @PostMapping("/confirm")
    public BaseResponse<Void> submitConfirmation() {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        employeeService.submitConfirmation(employeeId);
        return responseFactory.success("success.default", null);
    }
}
