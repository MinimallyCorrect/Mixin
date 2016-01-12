package me.nallar.mixin.internal.description;

import java.util.function.Function;

public interface DeclarationInfo {
	String getName();

	void setName(String name);

	AccessFlags getAccessFlags();

	void setAccessFlags(AccessFlags accessFlags);

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
