package me.nallar.mixin.internal;

import java.io.*;

public class MixinApplicator {
	public MixinApplicator(File targetJar, File mixinSource) {

	}

	public static void main(String[] args) {

	}

	// TODO: 17/01/2016 Make this work. :p
	/*
	private void handleAnnotation(Annotation a, ClassMember member) {
		String annotationName = a.getType().getClassName();

		if (!annotationName.startsWith("me.nallar.mixin")) {
			// Unrelated annotation
			return;
		}

		annotationName = annotationName.substring(16);
		String className = member.getClassInfo().getName();

		switch (annotationName) {
			case "Mixin":
				patches.put(className, (classMember) -> handleMixin);
				handleMixin(a, member.getClassInfo());

			default:
				throw new RuntimeException("Unknown mixin annotation: " + a.getType());
		}
	}

	private void handleMixin(Annotation a, ClassInfo classInfo) {

	}

	@Override
	public boolean shouldTransformClass(String name) {
		return getPatchInfo(name) != null;
	}

	@Override
	public void transformClass(ClassInfo editor) {
		val patchInfo = getPatchInfo(editor.getName());

		editor.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(true));

		// TODO patchInfo.exposeInners support - add methods for inner classes
		editor.getFields().forEach(d -> modifyDeclarations(d, patchInfo));
		editor.getMethods().forEach(d -> modifyDeclarations(d, patchInfo));

		patchInfo.fields.forEach(editor::add);

		patchInfo.methods.forEach(editor::add);
	}

	private void modifyDeclarations(Accessible accessible, PatchInfo patchInfo) {
		accessible.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(patchInfo.makePublic));
	}
	*/
}
