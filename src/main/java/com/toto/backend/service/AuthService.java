package com.toto.backend.service;

import com.toto.backend.dto.request.ChangePasswordRequest;
import com.toto.backend.dto.request.ForgotPasswordRequest;
import com.toto.backend.dto.request.LoginRequest;
import com.toto.backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String employeeId);
    void changePassword(String employeeId, ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
}
