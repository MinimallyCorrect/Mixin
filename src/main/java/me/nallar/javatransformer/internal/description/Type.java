package me.nallar.javatransformer.internal.description;

import com.github.javaparser.ast.ImportDeclaration;
import lombok.Data;
import lombok.val;
import me.nallar.javatransformer.internal.util.JVMUtil;

import java.util.*;

/**
 * // access flags 0x8
 * // signature <T:Ljava/lang/Object;>(Ljava/util/ArrayList<TT;>;Ljava/util/List<Ljava/lang/String;>;)TT;
 * // declaration: T test<T>(java.util.ArrayList<T>, java.util.List<java.lang.String>)
 * static test(Ljava/util/ArrayList;Ljava/util/List;)Ljava/lang/Object;
 * <p>
 * </code></pre>
 */
@Data
public class Type {
	/**
	 * <pre><code>
	 * 		a variable of type List<String> has:
	 * 			real type: Ljava/util/List;
	 * 			generic type: Ljava/util/List<Ljava/lang/String
	 *
	 * 		;>
	 *
	 *     When the type parameter T is <T:Ljava/lang/Object;>
	 *
	 * 		a variable of type T has:
	 * 			real type: Ljava/lang/Object;
	 * 			generic type: TT;
	 *
	 * 		a variable of type List<T> has:
	 * 			real type: Ljava/util/List;
	 * 			generic type: Ljava/util/List<TT;>
	 * </code></pre>
	 */
	public final String real;
	public final String generic;

	public Type(String real, String generic) {
		if (generic != null && generic.equals(real))
			generic = null;
		this.real = real;
		this.generic = generic;
	}

	public Type(String real) {
		this(real, null);
	}

	public static List<Type> of(String realDesc, String genericDesc) {
		val realTypes = splitTypes(realDesc);
		val genericTypes = splitTypes(genericDesc);

		val types = new ArrayList<Type>();
		for (int i = 0; i < realTypes.size(); i++) {
			String real = realTypes.get(i);
			String generic = genericTypes == null ? null : genericTypes.get(i);
			types.add(new Type(real, generic));
		}

		return types;
	}

	public static List<String> splitTypes(final String signature) {
		if (signature == null)
			return null;

		val types = new ArrayList<String>();
		int pos = 0;
		char c;
		String current = "";
		String name;

		while (pos < signature.length())
			switch (c = signature.charAt(pos++)) {
				case 'Z':
				case 'C':
				case 'B':
				case 'S':
				case 'I':
				case 'F':
				case 'J':
				case 'D':
				case 'V':
					types.add(current + c + "");
					current = "";
					break;

				case '[':
					current += '[';
					break;

				case 'T':
					int end = signature.indexOf(';', pos);
					name = signature.substring(pos, end);
					types.add(current + 'T' + name + ';');
					current = "";
					break;

				default: // case 'L':
					int start = pos;
					int genericCount = 0;
					innerLoop:
					while (pos < signature.length())
						switch (signature.charAt(pos++)) {
							case ';':
								if (genericCount > 0)
									break;
								name = signature.substring(start, pos - 1);
								types.add(current + 'L' + name + ';');
								current = "";
								break innerLoop;
							case '<':
								genericCount++;
								break;
							case '>':
								genericCount--;
								break;
						}
			}
		return types;
	}

	public static Type resolve(com.github.javaparser.ast.type.Type type, Iterable<ImportDeclaration> imports) {
		return resolve(type.toStringWithoutComments().trim(), imports);
	}

	public static Type resolve(String name, Iterable<ImportDeclaration> imports) {
		return new Type("L" + resolveName(name, imports) + ";");
	}

	private static String resolveName(String name, Iterable<ImportDeclaration> imports) {
		// TODO: 13/01/2016 Handle generic types (ArrayList<Type> -> resolve Type, and ArrayList.)

		for (ImportDeclaration anImport : imports) {
			String importName = anImport.getName().getName();
			if (importName.endsWith(name)) {
				return importName;
			}
		}

		if (!name.contains(".") && !Objects.equals(System.getProperty("JarTransformer.allowDefaultPackage"), "true")) {
			throw new RuntimeException("Couldn't resolve name: " + name);
		}

		return name;
	}

	public boolean isPrimitiveType() {
		return real.charAt(0) != 'L';
	}

	public String getSimpleName() {
		if (isPrimitiveType())
			return getPrimitiveTypeName();
		else
			return getClassName();
	}

	public String getPrimitiveTypeName() {
		if (!isPrimitiveType())
			throw new RuntimeException("Can't get primitive name for class type");
		return JVMUtil.descriptorToPrimitiveType(real);
	}

	public String getClassName() {
		if (isPrimitiveType()) {
			throw new RuntimeException("Can't get classname for primitive type");
		}
		return real.substring(1, real.length() - 1).replace('/', '.');
	}

	public String genericOrReal() {
		return generic == null ? real : generic;
	}

	public String unresolve(Iterable<ImportDeclaration> imports) {
		if (isPrimitiveType()) {
			return getPrimitiveTypeName();
		}
		String className = getClassName();

		for (ImportDeclaration anImport : imports) {
			String importName = anImport.getName().getName();
			if (className.startsWith(importName)) {
				return className.replace(importName + ".", "");
			}
		}

		return className;
	}
}
