package me.nallar.mixin.internal.description.impl;

import lombok.Data;
import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.Type;

@Data
public class FieldInfoImplementation implements FieldInfo {
	public AccessFlags accessFlags;
	public String name;
	public Type type;

	public FieldInfoImplementation(AccessFlags accessFlags, Type type, String name) {
		this.accessFlags = accessFlags;
		this.type = type;
		this.name = name;
	}

	@Override
	public String toString() {
		return accessFlags.toString() + ' ' + type + ' ' + name;
	}
}
