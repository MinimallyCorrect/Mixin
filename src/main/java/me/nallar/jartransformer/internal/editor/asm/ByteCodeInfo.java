package me.nallar.jartransformer.internal.editor.asm;

import lombok.val;
import me.nallar.jartransformer.api.*;
import me.nallar.jartransformer.internal.description.MethodDescriptor;
import me.nallar.jartransformer.internal.description.Parameter;
import me.nallar.jartransformer.internal.description.Type;
import me.nallar.jartransformer.internal.util.AnnotationParser;
import me.nallar.jartransformer.internal.util.CollectionUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.*;

public class ByteCodeInfo implements ClassInfo {
	private final ClassNode node;

	public ByteCodeInfo(ClassNode node) {
		this.node = node;
	}

	@Override
	public String getName() {
		return node.name.replace('/', '.');
	}

	@Override
	public void setName(String name) {
		node.name = name.replace('.', '/');
	}

	@Override
	public AccessFlags getAccessFlags() {
		return new AccessFlags(node.access);
	}

	@Override
	public void setAccessFlags(AccessFlags accessFlags) {
		node.access = accessFlags.access;
	}

	@Override
	public void add(MethodInfo method) {
		MethodNode node = new MethodNode();
		MethodInfo info = MethodNodeInfo.wrap(node);
		info.setAll(method);
		this.node.methods.add(node);
	}

	@Override
	public void add(FieldInfo field) {
		val node = new FieldNode(0, null, null, null, null);
		val nodeInfo = new FieldNodeInfo(node);
		nodeInfo.setAll(field);
	}

	@Override
	public List<MethodInfo> getMethods() {
		return node.methods.stream().map(MethodNodeInfo::new).collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public List<FieldInfo> getFields() {
		return node.fields.stream().map(FieldNodeInfo::new).collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public List<Annotation> getAnnotations() {
		return CollectionUtil.union(node.invisibleAnnotations, node.visibleAnnotations).map(AnnotationParser::annotationFromAnnotationNode).collect(Collectors.toList());
	}

	static class FieldNodeInfo implements FieldInfo {
		private final FieldNode node;
		private Type type;

		FieldNodeInfo(FieldNode node) {
			this.node = node;
			type = new Type(node.desc, node.signature);
		}

		@Override
		public String getName() {
			return node.name;
		}

		@Override
		public void setName(String name) {
			node.name = name;
		}

		@Override
		public AccessFlags getAccessFlags() {
			return new AccessFlags(node.access);
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {
			node.access = accessFlags.access;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void setType(Type type) {
			this.type = type;
			node.desc = type.real;
			node.signature = type.generic;
		}
	}

	static class MethodNodeInfo implements MethodInfo {
		private final MethodNode node;
		private MethodDescriptor descriptor;

		private MethodNodeInfo(MethodNode node) {
			this.node = node;
			descriptor = new MethodDescriptor(node.desc, node.signature);
		}

		public static MethodInfo wrap(MethodNode node) {
			return new MethodNodeInfo(node);
		}

		@Override
		public AccessFlags getAccessFlags() {
			return new AccessFlags(node.access);
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {
			node.access = accessFlags.access;
		}

		@Override
		public String getName() {
			return node.name;
		}

		@Override
		public void setName(String name) {
			node.name = name;
		}

		@Override
		public Type getReturnType() {
			return new MethodDescriptor(node.desc, node.signature).getReturnType();
		}

		@Override
		public void setReturnType(Type returnType) {
			descriptor = descriptor.withReturnType(returnType);
			descriptor.saveTo(node);
		}

		@Override
		public List<Parameter> getParameters() {
			val descriptor = new MethodDescriptor(node.desc, node.signature);
			return descriptor.getParameters();
		}

		@Override
		public void setParameters(List<Parameter> parameters) {
			descriptor = descriptor.withParameters(parameters);
			descriptor.saveTo(node);
		}

		public String getDescriptor() {
			return descriptor.getDescriptor();
		}
	}
}
