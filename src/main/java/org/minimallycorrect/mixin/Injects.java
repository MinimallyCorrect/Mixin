package org.minimallycorrect.mixin;

import java.lang.annotation.*;

@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface Injects {
	Inject[] value();
}
