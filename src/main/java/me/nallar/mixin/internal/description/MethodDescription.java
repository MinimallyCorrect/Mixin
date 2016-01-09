package me.nallar.mixin.internal.description;

import me.nallar.mixin.internal.util.JVMUtil;
import me.nallar.mixin.internal.ParsingException;

import java.lang.reflect.*;
import java.util.*;

public class MethodDescription {
	public final String clazz;
	public final String returnType;
	public final String parameters;
	public final String name;
	private Integer cachedHashCode = null;

	private MethodDescription(String clazz, String name, String returnType, String parameters) {
		this.clazz = clazz;
		this.returnType = returnType;
		this.parameters = parameters;
		this.name = name;
	}

	public static MethodDescription of(String clazz, String name, String MCPDescription) {
		//MCP style - (Lxv;IIILanw;Ljava/util/List;Llq;)V
		return new MethodDescription(clazz, name, MCPDescription.substring(MCPDescription.lastIndexOf(')') + 1), MCPDescription.substring(1, MCPDescription.indexOf(')')));
	}

	public static MethodDescription of(Method m) {
		return new MethodDescription(m.getDeclaringClass().getCanonicalName(), m.getName(), JVMUtil.getJVMName(m.getReturnType()), JVMUtil.getParameterList(m));
	}

	public static MethodDescription of(String clazz, String methodString) {
		if (methodString.contains("(")) {
			try {
				String methodName = methodString.substring(0, methodString.indexOf('('));
				methodString = methodString.replace('.', '/');
				return MethodDescription.of(clazz, methodName, methodString.substring(methodString.indexOf('(')));
			} catch (Exception e) {
				throw new ParsingException("Failed to parse " + methodString, e);
			}
		}
		return new MethodDescription(clazz, methodString, "", "");
	}

	public static List<MethodDescription> ofList(String clazz, String methodList) {
		ArrayList<MethodDescription> methodDescriptions = new ArrayList<>();
		for (String methodString : methodList.split(",")) {
			if (methodString.trim().isEmpty()) continue;
			methodDescriptions.add(of(clazz, methodString));
		}
		return methodDescriptions;
	}

	@Override
	public String toString() {
		return name + getMCPName();
	}

	String getMCPName() {
		return '(' + parameters + ')' + returnType;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode != null) {
			return cachedHashCode;
		}
		int hashCode = returnType.hashCode();
		hashCode = 31 * hashCode + parameters.hashCode();
		hashCode = 31 * hashCode + name.hashCode();
		hashCode = 31 * hashCode + clazz.hashCode();
		return (cachedHashCode = hashCode);
	}

	@Override
	public boolean equals(Object other) {
		return this == other ||
			(other instanceof MethodDescription &&
				((MethodDescription) other).clazz.equals(this.clazz) &&
				((MethodDescription) other).returnType.equals(this.returnType) &&
				((MethodDescription) other).parameters.equals(this.parameters) &&
				((MethodDescription) other).name.equals(this.name))
			|| (other instanceof Method && MethodDescription.of((Method) other).equals(this));
	}
}
