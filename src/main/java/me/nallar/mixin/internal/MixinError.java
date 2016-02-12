package me.nallar.mixin.internal;

/**
 * Thrown to indicate an unhandlable error parsing or applying Mixins.
 *
 * This is an error instead of an exception as failing to apply mixins will be unrecoverable in most contexts.
 */
public class MixinError extends Error {
	private static final long serialVersionUID = 0;

	public MixinError() {
		super();
	}

	public MixinError(String message) {
		super(message);
	}

	public MixinError(String message, Throwable cause) {
		super(message, cause);
	}

	public MixinError(Throwable cause) {
		super(cause);
	}

	protected MixinError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
