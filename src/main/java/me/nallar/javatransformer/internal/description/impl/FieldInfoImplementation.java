package me.nallar.javatransformer.internal.description.impl;

import lombok.Data;
import me.nallar.javatransformer.api.AccessFlags;
import me.nallar.javatransformer.api.FieldInfo;
import me.nallar.javatransformer.internal.description.Type;

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

	public static FieldInfo of(AccessFlags accessFlags, Type type, String name) {
		return new FieldInfoImplementation(accessFlags, type, name);
	}

	@Override
	public String toString() {
		return accessFlags.toString() + ' ' + type + ' ' + name;
	}
}
