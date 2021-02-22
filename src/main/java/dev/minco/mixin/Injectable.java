package dev.minco.mixin;

import java.lang.annotation.*;

@java.lang.annotation.Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Injectable {
	/**
	 * Name of this Injectable. Referenced by Inject.injectable
	 * <p>
	 * Defaults to the method name of the injectable
	 *
	 * @see Inject
	 **/
	String name() default "";
}
