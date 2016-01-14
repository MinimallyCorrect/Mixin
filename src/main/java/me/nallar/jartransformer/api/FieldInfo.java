package me.nallar.jartransformer.api;

import me.nallar.jartransformer.internal.description.Type;
import me.nallar.jartransformer.internal.description.impl.FieldInfoImplementation;

public interface FieldInfo extends DeclarationInfo {
	static FieldInfo of(AccessFlags accessFlags, Type type, String name) {
		return new FieldInfoImplementation(accessFlags, type, name);
	}

	AccessFlags getAccessFlags();

	void setAccessFlags(AccessFlags accessFlags);

	Type getType();

	void setType(Type type);

	default void setAll(FieldInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setType(info.getType());
	}

}
