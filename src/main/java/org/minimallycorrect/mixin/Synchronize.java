package org.minimallycorrect.mixin;

import java.lang.annotation.*;

@java.lang.annotation.Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Synchronize {}
