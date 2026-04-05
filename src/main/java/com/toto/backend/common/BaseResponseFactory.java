package com.toto.backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseResponseFactory {

    private final MessageSource messageSource;

    public <T> BaseResponse<T> success(String messageKey, T data, Object... args) {
        String message = messageSource.getMessage(
                messageKey,
                args,
                LocaleContextHolder.getLocale()
        );

        return BaseResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .status(200)
                .message(messageSource.getMessage("success.default", null, LocaleContextHolder.getLocale()))
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public BaseResponse<?> error(int status, String messageKey) {
        String message = messageSource.getMessage(
                messageKey,
                null,
                LocaleContextHolder.getLocale()
        );

        return BaseResponse.builder()
                .status(status)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
