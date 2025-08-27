package com.ahd.backend.carcontracts.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Locale;

@Component
public class Messages {

    @Autowired
    private MessageSource messageSource;

    private static MessageSource staticMessageSource;

    @PostConstruct
    public void init() {
        staticMessageSource = messageSource;
    }

    public static String tr(String key, Object... args) {
        if (staticMessageSource == null) {
            return key;
        }
        try {
            return staticMessageSource.getMessage(key, args, Locale.ROOT);
        } catch (Exception e) {
            return key;
        }
    }

    public static String tr(String key) {
        return tr(key, (Object[]) null);
    }
}

