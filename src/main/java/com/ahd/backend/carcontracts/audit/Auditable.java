package com.ahd.backend.carcontracts.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /** مثال: "CREATE_CONTRACT", "DELETE_USER"… */
    String operation();
}