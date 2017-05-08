package me.nallar.mixin;

import java.lang.annotation.*;

/**
 * Apply to a method to insert the contents of that method at the start of another method
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
	String target() default "";
}
