package me.nallar.mixin.internal.editor.asm;

import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodDescription;
import me.nallar.mixin.internal.editor.JavaEditor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ByteCodeEditor implements JavaEditor {
	private final ClassNode node;

	public ByteCodeEditor(ClassReader reader) {
		node = new ClassNode();
		reader.accept(node, ClassReader.EXPAND_FRAMES);
	}

	@Override
	public void addStub(MethodDescription description) {
		MethodNode method = new MethodNode();
		method.access = Opcodes.ACC_PUBLIC;
	}

	@Override
	public void addStub(FieldInfo field) {

	}

	@Override
	public void makePublic(FieldInfo field) {

	}

	@Override
	public void makePublic(MethodDescription method) {

	}

	@Override
	public void getMethods(MethodDescription method) {

	}

	@Override
	public void getFields(FieldInfo fields) {

	}
}
