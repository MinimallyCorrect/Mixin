package me.nallar.mixin.internal.editor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.FieldDescription;
import me.nallar.mixin.internal.description.MethodDescription;

public class SourceEditor implements JavaEditor {
	private final TypeDeclaration type;

	public SourceEditor(TypeDeclaration type) {
		this.type = type;
	}

	@Override
	public void addStub(MethodDescription description) {
		MethodDeclaration methodDeclaration = new MethodDeclaration();
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
