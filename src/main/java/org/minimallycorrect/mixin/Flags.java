package org.minimallycorrect.mixin;

import java.lang.annotation.*;

/**
 * Sets the flags of the target
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Flags {
	String flags();

	FlagsMode mode() default FlagsMode.ADD;

	enum FlagsMode {
		ADD,
		REMOVE,
		SET,
	}
}
