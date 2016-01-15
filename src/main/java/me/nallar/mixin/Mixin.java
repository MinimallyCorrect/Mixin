package me.nallar.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adding this annotation to a class marks it as a mixin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mixin {
	/**
	 * By default, the target is set to the super-class. You may also specify the target class name.
	 */
	String target() default "";
}
