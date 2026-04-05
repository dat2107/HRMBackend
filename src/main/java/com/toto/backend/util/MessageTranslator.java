package com.toto.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageTranslator {
    private final MessageSource messageSource;

    public String translate(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
