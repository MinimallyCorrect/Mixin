package me.nallar.mixin.internal.description;

import me.nallar.mixin.internal.description.impl.MethodInfoImplementation;

import java.util.*;

public interface MethodInfo extends DeclarationInfo {
	Type getReturnType();
	List<Parameter> getParameters();

	void setReturnType(Type returnType);
	void setParameters(List<Parameter> parameters);

	default void setAll(MethodInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setReturnType(info.getReturnType());
		this.setParameters(info.getParameters());
	}

	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, Parameter... parameters) {
		return new MethodInfoImplementation(accessFlags, name, returnType, parameters);
	}

	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, List<Parameter> parameters) {
		return new MethodInfoImplementation(accessFlags, name, returnType, parameters);
	}
}
