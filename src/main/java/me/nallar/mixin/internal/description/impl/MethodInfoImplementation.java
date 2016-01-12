package me.nallar.mixin.internal.description.impl;

import lombok.Data;
import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.description.Parameter;
import me.nallar.mixin.internal.description.Type;

import java.util.*;

@Data
public class MethodInfoImplementation implements MethodInfo {
	public AccessFlags accessFlags;
	public String name;
	public Type returnType;
	public final List<Parameter> parameters;

	public MethodInfoImplementation(AccessFlags accessFlags, String name, Type returnType, Parameter... parameters) {
		this(accessFlags, name, returnType, Arrays.asList(parameters));
	}

	public MethodInfoImplementation(AccessFlags accessFlags, String name, Type returnType, List<Parameter> parameters) {
		this.accessFlags = accessFlags;
		this.name = name;
		this.returnType = returnType;
		this.parameters = new ArrayList<>(parameters);
	}
}
