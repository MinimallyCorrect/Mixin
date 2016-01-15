package me.nallar.jartransformer.api;

import me.nallar.jartransformer.internal.description.Type;
import me.nallar.jartransformer.internal.description.impl.FieldInfoImplementation;

public interface FieldInfo extends Named, Accessible {
	static FieldInfo of(AccessFlags accessFlags, Type type, String name) {
		return new FieldInfoImplementation(accessFlags, type, name);
	}

	Type getType();

	void setType(Type type);

	default void setAll(FieldInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setType(info.getType());
	}

}
