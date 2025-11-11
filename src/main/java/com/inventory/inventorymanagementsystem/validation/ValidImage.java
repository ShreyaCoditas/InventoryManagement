package com.inventory.inventorymanagementsystem.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {
    String message() default "Only image files (JPG, JPEG, PNG) are allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


