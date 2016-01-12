package me.nallar.mixin.internal.description;

import java.util.*;

public interface MethodInfo {
	AccessFlags getAccessFlags();
	String getName();
	Type getReturnType();
	List<Parameter> getParameters();

	void setAccessFlags(AccessFlags accessFlags);
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
