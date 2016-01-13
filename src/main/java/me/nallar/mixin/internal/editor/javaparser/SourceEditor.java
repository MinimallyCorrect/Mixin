package me.nallar.mixin.internal.editor.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.*;
import me.nallar.mixin.internal.editor.ClassEditor;

import java.util.*;

public class SourceEditor implements ClassEditor {
	private final TypeDeclaration type;
	private final Iterable<ImportDeclaration> imports;

	public SourceEditor(TypeDeclaration type, Iterable<ImportDeclaration> imports) {
		this.type = type;
		this.imports = imports;
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

	class MethodDeclarationWrapper implements MethodInfo {
		private final MethodDeclaration declaration;

		public MethodDeclarationWrapper(MethodDeclaration declaration) {
			this.declaration = declaration;
		}

		@Override
		public Type getReturnType() {
			return Type.resolve(declaration.getType(), imports);
		}

		@Override
		public List<Parameter> getParameters() {
			return null;
		}

		@Override
		public void setReturnType(Type returnType) {

		}

		@Override
		public void setParameters(List<Parameter> parameters) {

		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void setName(String name) {

		}

		@Override
		public AccessFlags getAccessFlags() {
			return null;
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {

		}
	}
}
