package me.nallar.mixin.internal.description;

import java.util.function.Function;

public interface DeclarationInfo {
	void setAccessFlags(AccessFlags accessFlags);

	AccessFlags getAccessFlags();

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
