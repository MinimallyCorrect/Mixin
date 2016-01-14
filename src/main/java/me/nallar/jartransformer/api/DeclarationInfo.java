package me.nallar.jartransformer.api;

import java.util.function.*;

public interface DeclarationInfo {
	String getName();

	void setName(String name);

	AccessFlags getAccessFlags();

	void setAccessFlags(AccessFlags accessFlags);

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
