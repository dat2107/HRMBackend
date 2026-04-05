package com.toto.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String employeeId;

    @NotBlank
    private String password;

    // Cloudflare Turnstile token (optional - skip verification in dev)
    private String captchaToken;
}
