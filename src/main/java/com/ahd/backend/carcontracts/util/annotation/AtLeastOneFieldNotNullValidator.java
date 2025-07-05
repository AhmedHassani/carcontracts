package com.ahd.backend.carcontracts.util.annotation;



import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * A generic ConstraintValidator that inspects all declared fields of the given object
 * and returns true if at least one of them is non‐null.
 */
public class AtLeastOneFieldNotNullValidator
        implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {

    @Override
    public void initialize(AtLeastOneFieldNotNull constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value != null) {
                    return true;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return false;
    }
}
