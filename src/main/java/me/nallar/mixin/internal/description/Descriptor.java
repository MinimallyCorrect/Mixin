package me.nallar.mixin.internal.description;

import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.tree.MethodNode;

@Getter
@ToString
public class Descriptor {
	private final String descriptor;
	private final String signature;

	public Descriptor(String descriptor, String signature) {
		this.descriptor = descriptor;
		this.signature = signature;
	}

	public Descriptor(MethodNode node) {
		this(node.desc, node.signature);
	}

	public Type getReturnType() {
		String returnDescriptor = after(')', descriptor);
		String returnSignature = null;

		if (signature != null)
			returnSignature = after(')', signature);

		return new Type(returnDescriptor, returnSignature);
	}

	private static String upto(char c, String in) {
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
