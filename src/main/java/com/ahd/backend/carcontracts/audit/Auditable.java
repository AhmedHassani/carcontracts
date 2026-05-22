package com.ahd.backend.carcontracts.audit;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String operation();
    String method() default "";
    boolean captureArgs() default false;
    boolean captureResult() default false;
}