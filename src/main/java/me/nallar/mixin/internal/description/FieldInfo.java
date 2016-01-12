package me.nallar.mixin.internal.description;

import me.nallar.mixin.internal.description.impl.FieldInfoImplementation;

public interface FieldInfo extends DeclarationInfo {
	AccessFlags getAccessFlags();

	String getName();

	Type getType();

	void setAccessFlags(AccessFlags accessFlags);

	void setName(String name);

	void setType(Type type);

	static FieldInfo of(AccessFlags accessFlags, Type type, String name) {
		return new FieldInfoImplementation(accessFlags, type, name);
	}

	default void setAll(FieldInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setType(info.getType());
	}

}
