package me.nallar.mixin.internal;

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
