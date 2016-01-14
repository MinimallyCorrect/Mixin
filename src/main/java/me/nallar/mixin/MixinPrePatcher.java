package me.nallar.mixin;

import lombok.val;
import me.nallar.jartransformer.JarTransformer;
import me.nallar.jartransformer.api.*;
import me.nallar.jartransformer.internal.util.JVMUtil;

import java.util.*;

public class MixinPrePatcher extends JarTransformer {
	private final Map<String, PatchInfo> patchClasses = new HashMap<>();

	private static void modifyDeclarations(DeclarationInfo declarationInfo, PatchInfo patchInfo) {
		declarationInfo.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(patchInfo.makePublic));
	}

	@Override
	public boolean shouldTransformClass(String name) {
		return getPatchInfo(name) != null;
	}

	@Override
	public void transformClass(ClassEditor editor) {
		val patchInfo = getPatchInfo(editor.getName());

		editor.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(true));

		// TODO patchInfo.exposeInners support - add methods for inner classes
		editor.getFields().forEach(d -> modifyDeclarations(d, patchInfo));
		editor.getMethods().forEach(d -> modifyDeclarations(d, patchInfo));

		patchInfo.fields.forEach(editor::add);

		patchInfo.methods.forEach(editor::add);
	}

	private PatchInfo getOrMakePatchInfo(String className, String shortClassName) {
		className = JVMUtil.fileNameToClassName(className);
		PatchInfo patchInfo = patchClasses.get(className);
		if (patchInfo == null) {
			patchInfo = new PatchInfo();
			patchClasses.put(className, patchInfo);
		}
		patchInfo.shortClassName = shortClassName;
		return patchInfo;
	}

	private PatchInfo getPatchInfo(String className) {
		className = JVMUtil.fileNameToClassName(className);
		return patchClasses.get(className);
	}

	private static class PatchInfo {
		public boolean exposeInners = false;
		List<MethodInfo> methods = new ArrayList<>();
		List<FieldInfo> fields = new ArrayList<>();
		boolean makePublic = false;
		String shortClassName;
	}
}
