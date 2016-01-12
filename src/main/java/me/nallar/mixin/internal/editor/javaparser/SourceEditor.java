package me.nallar.mixin.internal.editor.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.editor.JavaEditor;

import java.util.*;

public class SourceEditor implements JavaEditor {
	private final TypeDeclaration type;

	public SourceEditor(TypeDeclaration type) {
		this.type = type;
	}

	@Override
	public void add(MethodInfo description) {
		MethodDeclaration methodDeclaration = new MethodDeclaration();
		methodDeclaration.setModifiers(0);
	}

	@Override
	public void add(FieldInfo field) {

	}

	@Override
	public List<MethodInfo> getMethods() {
		return null;
	}

	@Override
	public void getFields(FieldInfo fields) {

	}
}
