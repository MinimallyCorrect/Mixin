package me.nallar.javatransformer.internal.editor.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import lombok.val;
import me.nallar.javatransformer.api.*;
import me.nallar.javatransformer.internal.description.Parameter;
import me.nallar.javatransformer.internal.description.Type;
import me.nallar.javatransformer.internal.util.AnnotationParser;
import me.nallar.javatransformer.internal.util.JVMUtil;

import java.util.*;
import java.util.stream.*;

public class SourceInfo implements ClassInfo {
	private final TypeDeclaration type;
	private final Iterable<ImportDeclaration> imports;
	private final PackageDeclaration packageDeclaration;

	public SourceInfo(TypeDeclaration type, PackageDeclaration packageDeclaration, Iterable<ImportDeclaration> imports) {
		this.type = type;
		this.packageDeclaration = packageDeclaration;
		this.imports = imports;
	}

	@Override
	public String getName() {
		return packageDeclaration.getName().getName() + '.' + type.getName();
	}

	@Override
	public void setName(String name) {
		String packageName = packageDeclaration.getName().getName();
		if (name.startsWith(packageName)) {
			type.setName(name.replace(packageName, ""));
		} else {
			throw new RuntimeException("Name '" + name + "' must be in package: " + packageDeclaration);
		}
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
		val wrapper = new MethodDeclarationWrapper(methodDeclaration);
		wrapper.setAll(description);
	}

	@Override
	public void add(FieldInfo field) {
		FieldDeclaration fieldDeclaration = new FieldDeclaration();
		val vars = new ArrayList<VariableDeclarator>();
		vars.add(new VariableDeclarator(new VariableDeclaratorId("unknown")));
		fieldDeclaration.setVariables(vars);
		FieldDeclarationWrapper wrapper = new FieldDeclarationWrapper(fieldDeclaration);
		wrapper.setAll(field);
	}

	@Override
	public List<MethodInfo> getMethods() {
		return type.getMembers().stream()
			.filter(x -> x instanceof MethodDeclaration)
			.map(x -> new MethodDeclarationWrapper((MethodDeclaration) x))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public List<FieldInfo> getFields() {
		return type.getMembers().stream()
			.filter(x -> x instanceof FieldDeclaration)
			.map(x -> new FieldDeclarationWrapper((FieldDeclaration) x))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private com.github.javaparser.ast.type.Type toType(Type t) {
		String name = t.unresolve(imports);
		if (t.isPrimitiveType()) {
			return new PrimitiveType(JVMUtil.searchEnum(PrimitiveType.Primitive.class, name));
		} else {
			return new ClassOrInterfaceType(name);
		}
	}

	private com.github.javaparser.ast.type.Type setType(Type newType, com.github.javaparser.ast.type.Type currentType) {
		val newType_ = toType(newType);

		if (currentType instanceof ClassOrInterfaceType && newType_ instanceof ClassOrInterfaceType) {
			val annotations = currentType.getAnnotations();
			if (annotations != null && !annotations.isEmpty())
				newType_.setAnnotations(annotations);
		}
		return newType_;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return getAnnotations(type.getAnnotations());
	}

	private List<Annotation> getAnnotations(List<AnnotationExpr> l) {
		return l.stream().map((it) -> AnnotationParser.annotationFromAnnotationExpr(it, imports)).collect(Collectors.toList());
	}

	@Override
	public ClassInfo getClassInfo() {
		return SourceInfo.this;
	}

	class FieldDeclarationWrapper implements FieldInfo {
		private final FieldDeclaration declaration;

		FieldDeclarationWrapper(FieldDeclaration declaration) {
			this.declaration = declaration;
			if (declaration.getVariables().size() != 1) {
				throw new UnsupportedOperationException("Not yet implemented: multiple variables in one field decl.");
			}
		}

		@Override
		public String getName() {
			return declaration.getVariables().get(0).getId().getName();
		}

		@Override
		public void setName(String name) {
			declaration.getVariables().get(0).getId().setName(name);
		}

		@Override
		public AccessFlags getAccessFlags() {
			return new AccessFlags(declaration.getModifiers());
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {
			declaration.setModifiers(accessFlags.access);
		}

		@Override
		public Type getType() {
			return Type.resolve(declaration.getType(), imports);
		}

		@Override
		public void setType(Type type) {
			declaration.setType(SourceInfo.this.setType(type, declaration.getType()));
		}

		@Override
		public List<Annotation> getAnnotations() {
			return SourceInfo.this.getAnnotations(declaration.getAnnotations());
		}

		@Override
		public ClassInfo getClassInfo() {
			return SourceInfo.this;
		}
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
		public void setReturnType(Type type) {
			declaration.setType(setType(type, declaration.getType()));
		}

		@Override
		public List<Parameter> getParameters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setParameters(List<Parameter> parameters) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return declaration.getName();
		}

		@Override
		public void setName(String name) {
			declaration.setName(name);
		}

		@Override
		public AccessFlags getAccessFlags() {
			return new AccessFlags(declaration.getModifiers());
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {
			declaration.setModifiers(accessFlags.access);
		}

		@Override
		public List<Annotation> getAnnotations() {
			return SourceInfo.this.getAnnotations(declaration.getAnnotations());
		}

		@Override
		public ClassInfo getClassInfo() {
			return SourceInfo.this;
		}
	}
}
