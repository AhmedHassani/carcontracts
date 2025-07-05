package com.ahd.backend.carcontracts.util.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class‐level constraint: ensures that at least one declared field of the target object is non‐null.
 */
@Documented
@Constraint(validatedBy = AtLeastOneFieldNotNullValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface AtLeastOneFieldNotNull {
    String message() default "At least one field must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
