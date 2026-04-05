package com.toto.backend.exception;

import com.toto.backend.common.BaseResponse;
import com.toto.backend.common.BaseResponseFactory;
import com.toto.backend.util.MessageTranslator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final BaseResponseFactory baseResponseFactory;
    private final MessageTranslator messageTranslator;

    // Lỗi AppException do bạn ném trong service
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        String localizedMessage = messageTranslator.translate(ex.getErrorCode().getMessageKey());

        BaseResponse<?> response = baseResponseFactory.error(
                ex.getErrorCode().getStatus().value(),
                localizedMessage
        );

        return ResponseEntity
                .status(ex.getErrorCode().getStatus().value())
                .body(response);
    }

    // Lỗi validation @Valid trong DTO
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Lỗi validation kiểu @PathVariable, @RequestParam
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Lỗi không xác định
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleGeneral(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);

        ApiException api = ApiException.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .code("SERVER_001")
                .message("Internal server error")
                .path(request.getRequestURI())
                .timestamp(Instant.now().toEpochMilli())
                .build();

        return new ResponseEntity<>(api, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

