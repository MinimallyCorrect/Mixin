package me.nallar.mixin.internal.description;

import java.util.*;

public class Type {
	private static final String[] searchPackages = {
		"java.lang",
		"java.util",
		"java.io",
	};
	public final String clazz;
	public final int arrayDimensions;
	public final List<Type> generics = new ArrayList<Type>();
	public boolean noClass = false;

	Type(String raw, List<String> imports) {
		String clazz;
		int arrayLevels = 0;
		while (raw.length() - (arrayLevels * 2) - 2 > 0) {
			int startPos = raw.length() - 2 - arrayLevels * 2;
			if (!raw.substring(startPos, startPos + 2).equals("[]")) {
				break;
			}
			arrayLevels++;
		}
		raw = raw.substring(0, raw.length() - arrayLevels * 2); // THE MORE YOU KNOW: String.substring(begin) special cases begin == 0.
		arrayDimensions = arrayLevels;
		if (raw.contains("<")) {
			String genericRaw = raw.substring(raw.indexOf('<') + 1, raw.length() - 1);
			clazz = raw.substring(0, raw.indexOf('<'));
			if (clazz.isEmpty()) {
				clazz = "java.lang.Object"; // For example, <T> methodName(Class<T> parameter) -> <T> as return type -> erases to object
				noClass = true;
			}
			for (String genericRawSplit : commaSplitter.split(genericRaw)) {
				generics.add(new Type(genericRawSplit, imports));
			}
		} else {
			clazz = raw;
		}
		this.clazz = fullyQualifiedName(clazz, imports);
	}

	private static String fullyQualifiedName(String original, Collection<String> imports) {
		int dots = CharMatcher.is('.').countIn(original);
		if (imports == null || dots > 1) {
			return original;
		}
		if (dots == 1) {
			String start = original.substring(0, original.indexOf('.'));
			String end = original.substring(original.indexOf('.') + 1);
			String qualifiedStart = fullyQualifiedName(start, imports);
			if (!qualifiedStart.equals(start)) {
				return qualifiedStart + '$' + end;
			}
			return original;
		}
		for (String className : imports) {
			String shortClassName = className;
			shortClassName = shortClassName.substring(shortClassName.lastIndexOf('.') + 1);
			if (shortClassName.equals(original)) {
				return className;
			}
		}
		for (String package_ : searchPackages) {
			String packagedName = package_ + "." + original;
			try {
				Class.forName(packagedName, false, PrePatcher.class.getClassLoader());
				return packagedName;
			} catch (ClassNotFoundException ignored) {
			}
		}
		if (primitiveTypeToDescriptor(original) == null) {
			log.severe("Failed to find fully qualified name for '" + original + "'.");
		}
		return original;
	}

	public String arrayDimensionsString() {
		return Strings.repeat("[", arrayDimensions);
	}

	public String toString() {
		return arrayDimensionsString() + clazz + (generics.isEmpty() ? "" : '<' + generics.toString() + '>');
	}

	private String genericSignatureIfNeeded(boolean useGenerics) {
		if (generics.isEmpty() || !useGenerics) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		for (Type generic : generics) {
			sb.append(generic.signature());
		}
		sb.append('>');
		return sb.toString();
	}

	private String javaString(boolean useGenerics) {
		if (clazz.contains("<") || clazz.contains(">")) {
			log.severe("Invalid Type " + this + ", contains broken generics info.");
		} else if (clazz.contains("[") || clazz.contains("]")) {
			log.severe("Invalid Type " + this + ", contains broken array info.");
		} else if (clazz.contains(".")) {
			return arrayDimensionsString() + 'L' + clazz.replace(".", "/") + genericSignatureIfNeeded(useGenerics) + ';';
		}
		String primitiveType = primitiveTypeToDescriptor(clazz);
		if (primitiveType != null) {
			return arrayDimensionsString() + primitiveType;
		}
		log.warning("Either generic type or unrecognized type: " + this.toString());
		return arrayDimensionsString() + 'T' + clazz + ';';
	}

	public String descriptor() {
		return javaString(false);
	}

	public String signature() {
		return javaString(true);
	}
}
