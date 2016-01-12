package me.nallar.mixin.internal.description;

import me.nallar.mixin.internal.description.impl.MethodInfoImplementation;

import java.util.*;

public interface MethodInfo extends DeclarationInfo {
	String getName();
	Type getReturnType();
	List<Parameter> getParameters();

	void setName(String name);
	void setReturnType(Type returnType);

	default void setAll(MethodInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setReturnType(info.getReturnType());
		this.getParameters().clear();
		this.getParameters().addAll(info.getParameters());
	}

	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, Parameter... parameters) {
		return new MethodInfoImplementation(accessFlags, name, returnType, parameters);
	}

	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, List<Parameter> parameters) {
		return new MethodInfoImplementation(accessFlags, name, returnType, parameters);
	}
}
