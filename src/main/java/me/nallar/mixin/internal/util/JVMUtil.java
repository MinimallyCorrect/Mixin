package me.nallar.mixin.internal.util;

import me.nallar.mixin.internal.description.AccessFlags;

import java.lang.reflect.*;
import java.util.*;

public class JVMUtil {
	public static String getDescriptor(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			if (clazz.equals(Boolean.TYPE)) {
				return "Z";
			} else if (clazz.equals(Short.TYPE)) {
				return "S";
			} else if (clazz.equals(Long.TYPE)) {
				return "J";
			} else if (clazz.equals(Integer.TYPE)) {
				return "I";
			} else if (clazz.equals(Float.TYPE)) {
				return "F";
			} else if (clazz.equals(Double.TYPE)) {
				return "D";
			} else if (clazz.equals(Character.TYPE)) {
				return "C";
			}
		}
		return 'L' + clazz.getCanonicalName() + ';';
	}

	public static String primitiveTypeToDescriptor(String primitive) {
		switch (primitive) {
			case "byte":
				return "B";
			case "char":
				return "C";
			case "double":
				return "D";
			case "float":
				return "F";
			case "int":
				return "I";
			case "long":
				return "J";
			case "short":
				return "S";
			case "void":
				return "V";
			case "boolean":
				return "Z";
		}

		throw new RuntimeException("Invalid primitive type: " + primitive);
	}

	public static String getParameterList(Method m) {
		List<Class<?>> parameterClasses = new ArrayList<>(Arrays.asList(m.getParameterTypes()));
		StringBuilder parameters = new StringBuilder();
		for (Class<?> clazz : parameterClasses) {
			parameters.append(getDescriptor(clazz));
		}
		return parameters.toString();
	}

	public static String accessIntToString(int access) {
		StringBuilder result = new StringBuilder();

		if (hasFlag(access, AccessFlags.ACC_PUBLIC))
			result.append(" public");

		if (hasFlag(access, AccessFlags.ACC_PRIVATE))
			result.append(" private");

		if (hasFlag(access, AccessFlags.ACC_PROTECTED))
			result.append(" protected");

		if (hasFlag(access, AccessFlags.ACC_STATIC))
			result.append(" static");

		if (hasFlag(access, AccessFlags.ACC_FINAL))
			result.append(" final");

		return result.toString().trim();
	}

	public static int accessStringToInt(String access) {
		int a = 0;
		for (String accessPart : Splitter.on(' ').split(access)) {
			switch (accessPart) {
				case "public":
					a |= AccessFlags.ACC_PUBLIC;
					break;
				case "protected":
					a |= AccessFlags.ACC_PROTECTED;
					break;
				case "private":
					a |= AccessFlags.ACC_PRIVATE;
					break;
				case "static":
					a |= AccessFlags.ACC_STATIC;
					break;
				case "synthetic":
					a |= AccessFlags.ACC_SYNTHETIC;
					break;
				default:
					throw new RuntimeException("Unknown access string " + access);
			}
		}
		return a;
	}

	public static boolean hasFlag(int access, int flag) {
		return (access & flag) != 0;
	}

	public static int replaceFlag(int in, int from, int to) {
		if ((in & from) != 0) {
			in &= ~from;
			in |= to;
		}
		return in;
	}

	public static int makeAccess(int access, boolean makePublic) {
		access = makeAtLeastProtected(access);
		if (makePublic) {
			access = replaceFlag(access, AccessFlags.ACC_PROTECTED, AccessFlags.ACC_PUBLIC);
		}
		return access;
	}

	public static int makeAtLeastProtected(int access) {
		if (hasFlag(access, AccessFlags.ACC_PUBLIC) || hasFlag(access, AccessFlags.ACC_PROTECTED)) {
			// already protected or public
			return access;
		}
		if (hasFlag(access, AccessFlags.ACC_PRIVATE)) {
			// private -> protected
			return replaceFlag(access, AccessFlags.ACC_PRIVATE, AccessFlags.ACC_PROTECTED);
		}
		// not public, protected or private so must be package-local
		// change to public - protected doesn't include package-local.
		return access | AccessFlags.ACC_PUBLIC;
	}
}
