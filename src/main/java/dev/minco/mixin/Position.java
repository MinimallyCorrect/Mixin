package dev.minco.mixin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import dev.minco.javatransformer.api.code.CodeFragment;

@Getter
@RequiredArgsConstructor
public enum Position {
	/**
	 * Injects before the matched injection points
	 */
	BEFORE(CodeFragment.InsertionPosition.BEFORE),
	/**
	 * Injects over the matched injection points (replacing it entirely)
	 */
	OVERWRITE(CodeFragment.InsertionPosition.OVERWRITE),
	/**
	 * Injects after the matched injection points
	 */
	AFTER(CodeFragment.InsertionPosition.AFTER),;

	private final CodeFragment.InsertionPosition position;
}
