package org.minimallycorrect.mixin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.minimallycorrect.javatransformer.api.code.CodeFragment;

@Getter
@RequiredArgsConstructor
public enum Type {
	/**
	 * Matches the entire body of the injectable.
	 */
	BODY(CodeFragment.Body.class),
	/**
	 * Matches all return statements.
	 * <p>
	 * Implicit returns at the end of void methods are also matched.
	 */
	RETURN(CodeFragment.Return.class),
	/**
	 * Matches field loads for the given field, or all field loads if no field name is given
	 */
	FIELD_LOAD(CodeFragment.FieldLoad.class),
	/**
	 * Matches field stores for the given field, or all field stores if no field name is given
	 */
	FIELD_STORE(CodeFragment.FieldStore.class),
	/**
	 * Matches injectable calls for the given injectable, or all injectable calls if no injectable name is given
	 */
	METHOD_CALL(CodeFragment.MethodCall.class),
	/**
	 * Matches new operator (object creation)
	 */
	NEW(CodeFragment.New.class),;

	private final Class<? extends CodeFragment> fragmentClass;
}
