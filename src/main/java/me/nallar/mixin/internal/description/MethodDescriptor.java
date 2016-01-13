package me.nallar.mixin.internal.description;

import lombok.Getter;
import lombok.ToString;
import lombok.val;

import java.util.*;

@Getter
@ToString
public class MethodDescriptor {
	// TODO: 13/01/2016 Refactor to store Type returnType and List<Parameter> parameters
	private final String descriptor;
	private final String signature;
	private final List<String> parameterNames;

	public MethodDescriptor(String descriptor, String signature) {
		this(descriptor, signature, null);
	}

	public MethodDescriptor(String descriptor, String signature, List<String> parameterNames) {
		this.descriptor = descriptor;
		this.signature = signature;
		this.parameterNames = parameterNames == null ? Collections.emptyList() : Collections.unmodifiableList(parameterNames);
	}

	public Type getReturnType() {
		String returnDescriptor = after(')', descriptor);
		String returnSignature = null;

		if (signature != null)
			returnSignature = after(')', signature);

		return new Type(returnDescriptor, returnSignature);
	}

	public List<Parameter> getParameters() {
		val parameters = new ArrayList<Parameter>();

		List<Type> parameterTypes = Type.of(getParameters(descriptor), getParameters(signature));

		for (int i = 0; i < parameterTypes.size(); i++) {
			String name = parameterNames.isEmpty() ? null : parameterNames.get(i);
			parameters.add(new Parameter(parameterTypes.get(i), name));
		}

		return parameters;
	}

	public MethodDescriptor withParameters(List<Parameter> t) {
		val descriptor = withParameters(type.)
	}

	public MethodDescriptor withReturnType(Type t) {
		val descriptor = withReturnType(this.descriptor, t.real);

		String signature = null;

		if (this.signature != null || t.generic != null)
			signature = withReturnType(this.signatureOrDescriptor(), t.genericOrReal());

		return new MethodDescriptor(descriptor, signature);
	}

	private String signatureOrDescriptor() {
		return signature == null ? descriptor : signature;
	}

	private static String withReturnType(String desc, String newType) {
		String before = before(')', desc);
		return before + ')' + newType;
	}

	private static String getParameters(String descriptor) {
		if (descriptor == null)
			return null;
		return before(')', after('(', descriptor));
	}

	private static String before(char c, String in) {
		int index = in.indexOf(c);

		if (index == -1)
			throw new RuntimeException("Could not find '" + c + "' in '" + in + "'");

		return in.substring(0, index);
	}

	private static String after(char c, String in) {
		int index = in.indexOf(c);

		if (index == -1)
			throw new RuntimeException("Could not find '" + c + "' in '" + in + "'");

		return in.substring(index + 1, in.length());
	}
}
