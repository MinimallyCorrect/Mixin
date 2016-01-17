package me.nallar.javatransformer.api;

import me.nallar.javatransformer.internal.description.Parameter;
import me.nallar.javatransformer.internal.description.Type;
import me.nallar.javatransformer.internal.description.impl.MethodInfoImplementation;

import java.util.*;

public interface MethodInfo extends Accessible, Annotated, ClassMember, Named {
	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, Parameter... parameters) {
		return of(accessFlags, name, returnType, Arrays.asList(parameters));
	}

	static MethodInfo of(AccessFlags accessFlags, String name, Type returnType, List<Parameter> parameters) {
		return MethodInfoImplementation.of(accessFlags, name, returnType, parameters);
	}

	Type getReturnType();

	void setReturnType(Type returnType);

	List<Parameter> getParameters();

	void setParameters(List<Parameter> parameters);

	default void setAll(MethodInfo info) {
		this.setName(info.getName());
		this.setAccessFlags(info.getAccessFlags());
		this.setReturnType(info.getReturnType());
		this.setParameters(info.getParameters());
	}
}
