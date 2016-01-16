package me.nallar.javatransformer.internal.description;

import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

@Getter
@ToString
public class MethodDescriptor {
	private final List<Parameter> parameters;
	private final Type returnType;

	public MethodDescriptor(List<Parameter> parameters, Type returnType) {
		this.parameters = parameters;
		this.returnType = returnType;
	}

	public MethodDescriptor(String descriptor, String signature) {
		this(descriptor, signature, null);
	}

	public MethodDescriptor(String descriptor, String signature, List<String> parameterNames) {
		this(getParameters(descriptor, signature, parameterNames), getReturnType(descriptor, signature));
	}

	private static Type getReturnType(String descriptor, String signature) {
		String returnDescriptor = after(')', descriptor);
		String returnSignature = null;

		if (signature != null)
			returnSignature = after(')', signature);

		return new Type(returnDescriptor, returnSignature);
	}

	private static List<Parameter> getParameters(String descriptor, String signature, List<String> parameterNames) {
		val parameters = new ArrayList<Parameter>();

		List<Type> parameterTypes = Type.of(getParameters(descriptor), getParameters(signature));

		for (int i = 0; i < parameterTypes.size(); i++) {
			String name = parameterNames.isEmpty() ? null : parameterNames.get(i);
			parameters.add(new Parameter(parameterTypes.get(i), name));
		}

		return parameters;
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

	public MethodDescriptor withParameters(List<Parameter> parameters) {
		return new MethodDescriptor(parameters, returnType);
	}

	public MethodDescriptor withReturnType(Type returnType) {
		return new MethodDescriptor(parameters, returnType);
	}

	public void saveTo(MethodNode node) {
		node.desc = getDescriptor();
		node.signature = getSignature();
	}

	public String getDescriptor() {
		StringBuilder desc = new StringBuilder("(");

		for (Parameter parameter : parameters) {
			desc.append(parameter.real);
		}

		desc.append(")").append(returnType.real);

		return desc.toString();
	}

	private String getSignature() {
		boolean any = false;
		StringBuilder signature = new StringBuilder("(");

		for (Parameter parameter : parameters) {
			String generic = parameter.generic;
			if (generic == null)
				generic = parameter.real;
			else
				any = true;

			signature.append(generic);
		}

		signature.append(")");
		String generic = returnType.generic;
		if (generic == null)
			generic = returnType.real;
		else
			any = true;

		signature.append(generic);

		if (any)
			return signature.toString();

		return null;
	}
}
