package me.nallar.mixin.internal.editor.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodDescription;
import me.nallar.mixin.internal.editor.JavaEditor;

public class SourceEditor implements JavaEditor {
	private final TypeDeclaration type;

	public SourceEditor(TypeDeclaration type) {
		this.type = type;
	}

	@Override
	public void addStub(MethodDescription description) {
		MethodDeclaration methodDeclaration = new MethodDeclaration();
		methodDeclaration.setModifiers();
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
