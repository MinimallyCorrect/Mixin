package dev.minco.mixin;

import java.lang.annotation.*;

@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Injects.class)
public @interface Inject {
	/**
	 * @return
	 */
	String injectable();

	/**
	 * @see Type
	 */
	Type type();

	/**
	 * @see Position
	 */
	Position position() default Position.BEFORE;

	/**
	 * By default, all matched injection points are used. If the index is set only the nth point is used.
	 */
	int index() default -1;

	/**
	 * Parameter for the injection type. May be used to set the field or method name. Not used by all types.
	 *
	 * For more complicated matching, use {@link #match()} and {@link Matcher}
	 */
	String value() default "";

	/**
	 * Name of a matcher to use. If multiple matchers have the same {@link Matcher#name()}, they will all be used.
	 *
	 * @see Matcher
	 */
	String match() default "";
}
