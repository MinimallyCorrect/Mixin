package me.nallar.mixin.internal.description;

import lombok.Data;

/**
 *   // access flags 0x8
 // signature <T:Ljava/lang/Object;>(Ljava/util/ArrayList<TT;>;Ljava/util/List<Ljava/lang/String;>;)TT;
 // declaration: T test<T>(java.util.ArrayList<T>, java.util.List<java.lang.String>)
 static test(Ljava/util/ArrayList;Ljava/util/List;)Ljava/lang/Object;
 *
 * </code></pre>
 */
@Data
public class Type {
	/**
	 * <pre><code>
	 *		a variable of type List<String> has:
	 *			real type: Ljava/util/List;
	 *			generic type: Ljava/util/List<Ljava/lang/String
	 *
	 *		;>
	 *
	 *     When the type parameter T is <T:Ljava/lang/Object;>
	 *
	 *		a variable of type T has:
	 *			real type: Ljava/lang/Object;
	 *			generic type: TT;
	 *
	 *		a variable of type List<T> has:
	 *			real type: Ljava/util/List;
	 *			generic type: Ljava/util/List<TT;>
	 * </code></pre>
	 */
	public final String real;
	public final String generic;

	public Type(String real, String generic) {
		this.real = real;
		this.generic = generic;
	}

	public boolean isPrimitiveType() {
		return real.charAt(0) != 'L';
	}

	public String getClassName() {
		if (isPrimitiveType()) {
			throw new RuntimeException("Can't get classname for primitive type");
		}
		return real.substring(1, real.length() - 1).replace('/', '.');
	}

	public Type(String real) {
		this(real, null);
	}
}
