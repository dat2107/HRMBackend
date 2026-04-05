package com.toto.backend.controller;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.dto.request.ChangePasswordRequest;
import com.toto.backend.dto.request.ForgotPasswordRequest;
import com.toto.backend.dto.request.LoginRequest;
import com.toto.backend.dto.response.LoginResponse;
import com.toto.backend.security.SecurityUtils;
import com.toto.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BaseResponseFactory responseFactory;

    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return responseFactory.success("success.login", authService.login(request));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout() {
        String employeeId = SecurityUtils.getCurrentEmployeeId();
        if (employeeId != null) authService.logout(employeeId);
        return responseFactory.success("success.logout", null);
    }

    @PutMapping("/change-password")
    public BaseResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getCurrentEmployeeId(), request);
        return responseFactory.success("success.changePassword", null);
    }

    @PostMapping("/forgot-password")
    public BaseResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return responseFactory.success("success.default", null);
    }
}
