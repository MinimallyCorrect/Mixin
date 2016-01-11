package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.regex.*;

public class Patcher {
	public static String patchSource(String inputSource, String inputClassName) {
		PatchInfo patchInfo = patchForClass(inputClassName);
		if (patchInfo == null) {
			return inputSource;
		}
		inputSource = inputSource.trim().replace("\t", "    ");
		String shortClassName = patchInfo.shortClassName;
		StringBuilder sourceBuilder = new StringBuilder(inputSource.substring(0, inputSource.lastIndexOf('}')))
			.append("\n// TT Patch Declarations\n");
		for (MethodInfo methodInfo : patchInfo.methods) {
			if (sourceBuilder.indexOf(methodInfo.javaCode) == -1) {
				sourceBuilder.append(methodInfo.javaCode).append('\n');
			}
		}
		for (FieldInfo FieldInfo : patchInfo.fields) {
			if (sourceBuilder.indexOf(FieldInfo.javaCode) == -1) {
				sourceBuilder.append(FieldInfo.javaCode).append('\n');
			}
		}
		sourceBuilder.append("\n}");
		inputSource = sourceBuilder.toString();
		/*Matcher genericMatcher = genericMethodPattern.matcher(contents);
		while (genericMatcher.find()) {
			String original = genericMatcher.group(1);
			String withoutGenerics = original.replace(' ' + generic + ' ', " Object ");
			int index = inputSource.indexOf(withoutGenerics);
			if (index == -1) {
				continue;
			}
			int endIndex = inputSource.indexOf("\n    }", index);
			String body = inputSource.substring(index, endIndex);
			inputSource = inputSource.replace(body, body.replace(withoutGenerics, original).replace("return ", "return (" + generic + ") "));
		}*/
		inputSource = inputSource.replace("\nfinal ", " ");
		inputSource = inputSource.replace(" final ", " ");
		inputSource = inputSource.replace("\nclass", "\npublic class");
		inputSource = inputSource.replace("\n    " + shortClassName, "\n    public " + shortClassName);
		inputSource = inputSource.replace("\n    protected " + shortClassName, "\n    public " + shortClassName);
		inputSource = inputSource.replace("private class", "public class");
		inputSource = inputSource.replace("protected class", "public class");
		inputSource = privatePattern.matcher(inputSource).replaceAll("$1protected");
		if (patchInfo.makePublic) {
			inputSource = inputSource.replace("protected ", "public ");
		}
		Matcher packageMatcher = packageFieldPattern.matcher(inputSource);
		StringBuffer sb = new StringBuffer();
		while (packageMatcher.find()) {
			packageMatcher.appendReplacement(sb, "\n    public " + packageMatcher.group(1) + ';');
		}
		packageMatcher.appendTail(sb);
		inputSource = sb.toString();
		Matcher innerClassMatcher = innerClassPattern.matcher(inputSource);
		while (innerClassMatcher.find()) {
			String name = innerClassMatcher.group(1);
			inputSource = inputSource.replace("    " + name + '(', "    public " + name + '(');
		}
		return inputSource.replace("    ", "\t");
	}

	public static byte[] patchCode(byte[] inputCode, String inputClassName) {
		ClassReader classReader = new ClassReader(inputCode);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);
		String superName = classNode.superName.replace("/", ".");
		if (superName != null && !superName.equals("java.lang.Object")) {
			classExtends.put(classNode.name.replace("/", "."), superName);
		}
		PatchInfo patchInfo = patchForClass(inputClassName);
		if (patchInfo == null) {
			return inputCode;
		}

		classNode.access = classNode.access & ~Opcodes.ACC_FINAL;
		classNode.access = makeAccess(classNode.access, true);
		if (patchInfo.exposeInners) {
			for (InnerClassNode innerClassNode : classNode.innerClasses) {
				innerClassNode.access = makeAccess(innerClassNode.access, true);
			}
		}
		for (FieldNode fieldNode : classNode.fields) {
			fieldNode.access = fieldNode.access & ~Opcodes.ACC_FINAL;
			fieldNode.access = makeAccess(fieldNode.access, patchInfo.makePublic);
		}
		for (MethodNode methodNode : classNode.methods) {
			methodNode.access = methodNode.access & ~Opcodes.ACC_FINAL;
			methodNode.access = makeAccess(methodNode.access, methodNode.name.equals("<init>") || patchInfo.makePublic);
		}
		for (FieldInfo fieldInfo : patchInfo.fields) {
			classNode.fields.add(new FieldNode(makeAccess(fieldInfo.accessAsInt() & ~Opcodes.ACC_FINAL, patchInfo.makePublic), fieldInfo.name, fieldInfo.type.descriptor(), fieldInfo.type.signature(), null));
		}
		for (MethodInfo methodInfo : patchInfo.methods) {
			classNode.methods.add(new MethodNode(makeAccess(methodInfo.accessAsInt() & ~Opcodes.ACC_FINAL, patchInfo.makePublic), methodInfo.name, methodInfo.descriptor(), methodInfo.signature(), null));
		}
		ClassWriter classWriter = new ClassWriter(classReader, 0);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

	private static class FieldInfo {
		public String name;
		public Type type;
		public String access;
		public boolean static_;
		public boolean volatile_;
		public boolean final_;
		public String javaCode;

		public String toString() {
			return "field: " + access + ' ' + (static_ ? "static " : "") + (volatile_ ? "volatile " : "") + type + ' ' + name;
		}

		public int accessAsInt() {
			int accessInt = 0;
			if (static_) {
				accessInt |= Opcodes.ACC_STATIC;
			}
			if (volatile_) {
				accessInt |= Opcodes.ACC_VOLATILE;
			}
			if (final_) {
				accessInt |= Opcodes.ACC_FINAL;
			}
			accessInt |= accessStringToInt(access);
			return accessInt;
		}
	}

	private static class PatchInfo {
		public boolean exposeInners = false;
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		List<FieldInfo> fields = new ArrayList<FieldInfo>();
		boolean makePublic = false;
		String shortClassName;
	}

	private static class MethodInfo {
		private static final Joiner parameterJoiner = Joiner.on(", ");
		public String name;
		public List<Type> parameterTypes = new ArrayList<Type>();
		public Type returnType;
		public String access;
		public boolean static_;
		public boolean synchronized_;
		public boolean final_;
		public String javaCode;
		public String genericType;

		public String toString() {
			return "method: " + access + ' ' + (static_ ? "static " : "") + (final_ ? "final " : "") + (synchronized_ ? "synchronized " : "") + returnType + ' ' + name + " (" + parameterJoiner.join(parameterTypes) + ')';
		}

		public int accessAsInt() {
			int accessInt = 0;
			if (static_) {
				accessInt |= Opcodes.ACC_STATIC;
			}
			if (synchronized_) {
				accessInt |= Opcodes.ACC_SYNCHRONIZED;
			}
			if (final_) {
				accessInt |= Opcodes.ACC_FINAL;
			}
			accessInt |= accessStringToInt(access);
			return accessInt;
		}

		public String descriptor() {
			StringBuilder sb = new StringBuilder();
			sb
				.append('(');
			for (Type type : parameterTypes) {
				sb.append(type.descriptor());
			}
			sb
				.append(')')
				.append(returnType.descriptor());
			return sb.toString();
		}

		public String signature() {
			StringBuilder sb = new StringBuilder();
			String genericType = this.genericType;
			if (genericType != null) {
				sb.append('<');
				genericType = genericType.substring(1, genericType.length() - 1);
				for (String genericTypePart : commaSplitter.split(genericType)) {
					if (genericTypePart.contains(" extends ")) {
						log.severe("Extends unsupported, TODO implement - in " + this.genericType); // TODO
					}
					sb
						.append(genericTypePart)
						.append(":Ljava/lang/Object;");
				}
				sb.append('>');
			}
			sb
				.append('(');
			for (Type type : parameterTypes) {
				sb.append(type.signature());
			}
			sb
				.append(')')
				.append(returnType.signature());
			return sb.toString();
		}
	}
}
