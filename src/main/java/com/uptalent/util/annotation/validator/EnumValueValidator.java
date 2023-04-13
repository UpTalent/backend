package com.uptalent.util.annotation.validator;

import com.uptalent.util.annotation.EnumValue;
import com.uptalent.util.exception.InvalidEnumValueException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private Set<String> allowedValues;

    @Override
    public void initialize(EnumValue enumValue) {
        Class<? extends Enum<?>> enumClass = enumValue.enumClass();
        Enum[] enumConstants = enumClass.getEnumConstants();
        allowedValues = new HashSet<>(enumConstants.length);
        for (Enum e : enumConstants) {
            allowedValues.add(e.name());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null && allowedValues.contains(value.toUpperCase())) {
            return true;
        } else {
            throw new InvalidEnumValueException("Invalid enum value: " + value);
        }
    }
}
