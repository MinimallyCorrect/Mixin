package me.nallar.javatransformer.internal.description.impl;

import lombok.Data;
import me.nallar.javatransformer.api.AccessFlags;
import me.nallar.javatransformer.api.Annotation;
import me.nallar.javatransformer.api.ClassInfo;
import me.nallar.javatransformer.api.FieldInfo;
import me.nallar.javatransformer.internal.description.Type;

import java.util.*;

@Data
public class FieldInfoImplementation implements FieldInfo {
	public AccessFlags accessFlags;
	public String name;
	public Type type;
	public List<Annotation> annotations;

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

	@Override
	public ClassInfo getClassInfo() {
		return null;
	}
}
