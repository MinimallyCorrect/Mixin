package me.nallar.javatransformer.api;

import java.util.function.*;

public interface Accessible {
	AccessFlags getAccessFlags();

	void setAccessFlags(AccessFlags accessFlags);

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
