package me.nallar.jartransformer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.jartransformer.api.ClassEditor;
import me.nallar.jartransformer.internal.editor.asm.ByteCodeEditor;
import me.nallar.jartransformer.internal.editor.javaparser.SourceEditor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.charset.*;

public abstract class JarTransformer {
	/**
	 * Determines whether a class should be transformed
	 *
	 * @param name Full class name, eg. java.lang.String
	 * @return Whether the given class should be transformed
	 */
	public abstract boolean shouldTransformClass(String name);

	/**
	 * @param editor editor instance associated with a class
	 */
	public abstract void transformClass(ClassEditor editor);

	public String transformJava(String source, String name) {
		if (!shouldTransformClass(name))
			return source;

		CompilationUnit cu;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(source.getBytes(Charset.forName("UTF-8"))), "UTF-8");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		String packageName = cu.getPackage().getName().getName();
		for (TypeDeclaration typeDeclaration : cu.getTypes()) {
			String shortClassName = typeDeclaration.getName();
			if ((packageName + '.' + shortClassName).equalsIgnoreCase(name)) {
				transformClass(new SourceEditor(typeDeclaration, cu.getPackage(), cu.getImports()));
			}
		}

		return cu.toString();
	}

	public byte[] transformClass(byte[] bytes, String name) {
		if (!shouldTransformClass(name))
			return bytes;

		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, ClassReader.EXPAND_FRAMES);

		transformClass(new ByteCodeEditor(node));

		ClassWriter classWriter = new ClassWriter(reader, 0);
		node.accept(classWriter);
		return classWriter.toByteArray();
	}
}
