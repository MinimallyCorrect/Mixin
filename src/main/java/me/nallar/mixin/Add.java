package me.nallar.mixin;

import java.lang.annotation.*;

/**
 * Apply to a field or method to add it to the target class
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Add {
}
