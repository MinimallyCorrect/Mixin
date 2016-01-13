package me.nallar.mixin.internal.editor.asm;

import lombok.val;
import me.nallar.mixin.internal.description.*;
import me.nallar.mixin.internal.editor.ClassEditor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public class ByteCodeEditor implements ClassEditor {
	private final ClassNode node;

	public ByteCodeEditor(ClassNode node) {
		this.node = node;
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

	static class MethodNodeInfo implements MethodInfo {
		private final MethodNode node;
		private MethodDescriptor descriptor;

		private MethodNodeInfo(MethodNode node) {
			this.node = node;
			descriptor = new MethodDescriptor(node.desc, node.signature);
		}

		@Override
		public AccessFlags getAccessFlags() {
			return new AccessFlags(node.access);
		}

		@Override
		public String getName() {
			return node.name;
		}

		@Override
		public Type getReturnType() {
			return new MethodDescriptor(node.desc, node.signature).getReturnType();
		}

		@Override
		public List<Parameter> getParameters() {
			val descriptor = new MethodDescriptor(node.desc, node.signature);
			return descriptor.getParameters();
		}

		@Override
		public void setAccessFlags(AccessFlags accessFlags) {
			node.access = accessFlags.access;
		}

		@Override
		public void setName(String name) {
			node.name = name;
		}

		@Override
		public void setParameters(List<Parameter> parameters) {
			descriptor = descriptor.withParameters(parameters);
			descriptor.saveTo(node);
		}

		@Override
		public void setReturnType(Type returnType) {
			descriptor = descriptor.withReturnType(returnType);
			descriptor.saveTo(node);
		}

		public static MethodInfo wrap(MethodNode node) {
			return new MethodNodeInfo(node);
		}
	}
}
