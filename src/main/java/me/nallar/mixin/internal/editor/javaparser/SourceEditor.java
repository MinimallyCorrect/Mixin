package me.nallar.mixin.internal.editor.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.editor.ClassEditor;

import java.util.*;

public class SourceEditor implements ClassEditor {
	private final TypeDeclaration type;

	public SourceEditor(TypeDeclaration type) {
		this.type = type;
	}

	@Override
	public AccessFlags getAccessFlags() {
		return new AccessFlags(type.getModifiers());
	}

	@Override
	public void setAccessFlags(AccessFlags accessFlags) {
		type.setModifiers(accessFlags.access);
	}

	@Override
	public void add(MethodInfo description) {
		MethodDeclaration methodDeclaration = new MethodDeclaration();
		methodDeclaration.setModifiers(0);
	}

	@Override
	public void add(FieldInfo field) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MethodInfo> getMethods() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FieldInfo> getFields() {
		throw new UnsupportedOperationException();
	}
}
