package com.toto.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private boolean requireChangePassword;
    private EmployeeBasicInfo employee;

    @Data
    @Builder
    public static class EmployeeBasicInfo {
        private String employeeId;
        private String fullName;
        private String department;
        private String positionCode;
        private String email;
        private String role;
    }
}
