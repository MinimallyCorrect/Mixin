package org.minimallycorrect.mixin;

import java.lang.annotation.*;

/**
 * Overrides a static method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OverrideStatic {
}
