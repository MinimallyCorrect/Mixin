package org.minimallycorrect.mixin;

import java.lang.annotation.*;

/**
 * Replaces the overriden method in the target class
 * <p>
 * Also works for static methods
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Overwrite {}
