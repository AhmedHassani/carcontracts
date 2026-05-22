package com.ahd.backend.carcontracts.notification.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyCarOperation {
    String operation(); // CREATE, UPDATE, DELETE
    String title();
    String messageTemplate();
}