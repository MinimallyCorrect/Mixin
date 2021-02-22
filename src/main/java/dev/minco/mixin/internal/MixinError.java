package dev.minco.mixin.internal;

/**
 * Thrown to indicate an unhandlable error parsing or applying Mixins.
 * <p>
 * This is an error instead of an exception as failing to apply mixins will be unrecoverable in most contexts.
 */
public class MixinError extends Error {
	private static final long serialVersionUID = 0;

	MixinError(String message) {
		super(message);
	}

	MixinError(String message, Throwable cause) {
		super(message, cause);
	}
}
