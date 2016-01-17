package me.nallar.javatransformer.api;

public interface Transformer {
	/**
	 * Determines whether a class should be transformed
	 *
	 * @param className Full class name, eg. java.lang.String
	 * @return Whether the given class should be transformed
	 */
	default boolean shouldTransform(String className) {
		return true;
	}

	/**
	 * @param editor editor instance associated with a class
	 */
	void transform(ClassInfo editor);
}
