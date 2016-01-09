package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.FieldDescription;
import me.nallar.mixin.internal.description.MethodDescription;
import org.objectweb.asm.ClassReader;

public class ByteCodeEditor implements JavaEditor {
	private final ClassReader reader;

	public ByteCodeEditor(ClassReader reader) {
		this.reader = reader;
	}

	@Override
	public void addStub(MethodDescription description) {

	}

	@Override
	public void addStub(FieldDescription field) {

	}

	@Override
	public void makePublic(FieldDescription field) {

	}

	@Override
	public void makePublic(MethodDescription method) {

	}

	@Override
	public void getMethods(MethodDescription method) {

	}

	@Override
	public void getFields(FieldDescription fields) {

	}
}
