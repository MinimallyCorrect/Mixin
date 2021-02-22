package dev.minco.mixin;

import java.lang.annotation.*;

@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Injects {
	Inject[] value();
}
