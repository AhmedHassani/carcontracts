package com.ahd.backend.carcontracts.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageSource messageSource;
    
    /**
     * الحصول على رسالة مترجمة بدون متغيرات
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
    
    /**
     * الحصول على رسالة مترجمة مع متغيرات
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * تنسيق رسالة مع متغيرات (بدون استخدام MessageSource)
     */
    public String format(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
    }
}