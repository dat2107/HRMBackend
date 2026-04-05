package com.toto.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // USER ERRORS
    USER_NOT_FOUND("USER_001", "error.user.not_found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_002", "error.user.already_exists", HttpStatus.BAD_REQUEST),
    USERNAME_TAKEN("USER_003", "error.username.taken", HttpStatus.BAD_REQUEST),
    EMAIL_TAKEN("USER_004", "error.email.taken", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("USER_005", "error.password.invalid", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("AUTH_006", "error.auth.token_expired", HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED("USER_007", "error.user.already_verified", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED("USER_008", "error.user.not_verified", HttpStatus.BAD_REQUEST),
    USER_NOT_ACTIVE("USER_009", "error.user.not_active", HttpStatus.BAD_REQUEST),
    INVALID_ROLE("USER_010", "error.role.invalid", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND("USER_011", "error.auth.email_not_found", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_REFRESH_TOKEN("USER_012", "error.auth.invalid_refresh_token", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("USER_013", "error.auth.invalid_request", HttpStatus.BAD_REQUEST),
    USER_NOT_PENDING("USER_014", "error.user.not_pending", HttpStatus.BAD_REQUEST),
    USER_ALREADY_DELETED("USER_015", "error.user.already_deleted", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ADMIN("USER_016", "error.cannot_delete_admin", HttpStatus.BAD_REQUEST),
    UPLOAD_AVATAR_FAILED("USER_017", "error.upload.avatar_failed", HttpStatus.BAD_REQUEST),
    // AUTH
    UNAUTHORIZED("AUTH_001", "error.auth.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_002", "error.auth.forbidden", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("AUTH_003", "error.auth.invalid_token", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("AUTH_004", "error.auth.invalid_refresh_token", HttpStatus.FORBIDDEN),
    KEYCLOAK_ERROR("AUTH_005", "error.auth.keycloak", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_CONFIRM_NOT_MATCH("AUTH_006", "error.auth.password_confirm_not_match", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD("AUTH_007", "error.invalidOldPassword", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH("AUTH_008", "error.passwordNotMatch", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD("AUTH_009", "error.password_same_as_old", HttpStatus.BAD_REQUEST),
    INVALID_OTP("AUTH_0010", "error.invalidOtp", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("AUTH_0011", "error.otp.expired", HttpStatus.BAD_REQUEST),

    // DATA ERRORS
    DATA_NOT_FOUND("DATA_001", "error.data.not_found", HttpStatus.NOT_FOUND),
    CONFLICT_DATA("DATA_002", "error.conflict", HttpStatus.CONFLICT),
    NOT_DELETE("DATA_003", "error.conflict", HttpStatus.BAD_REQUEST),

    EXAM_NAME_DUPLICATE("DATA_004", "error.exam.duplicate", HttpStatus.BAD_REQUEST),
    EXAM_NOT_FOUND("DATA_005", "error.exam.not_found", HttpStatus.NOT_FOUND),
    EXAM_ALREADY_ATTEMPTED("DATA_006", "error.exam.already_attempted", HttpStatus.BAD_REQUEST),
    EXAM_NOT_ENOUGH_QUESTIONS("DATA_007", "error.exam.not_enough_questions", HttpStatus.BAD_REQUEST),

    // VALIDATION
    VALIDATION_ERROR("VALID_001", "error.validation.failed", HttpStatus.BAD_REQUEST),

    // SERVER
    EMAIL_SEND_FAILED("MAIL_001", "error.mail.send_failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR("SERVER_001", "error.server.internal", HttpStatus.INTERNAL_SERVER_ERROR),

    //NutritionGuide
    INVALID_CODE("VALID_002", "error.validation.code", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus status;

    ErrorCode(String code, String messageKey, HttpStatus status) {
        this.code = code;
        this.messageKey = messageKey;
        this.status = status;
    }
}
