package me.nallar.mixin.internal.editor.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class MethodAdder extends ClassVisitor {
	private int mAccess;
	private String mName;
	private String mDesc;
	private String mSignature;
	private String[] mExceptions;

	public MethodAdder(ClassVisitor cv,
					   int mthAccess, String mthName,
					   String mthDesc, String mthSignature,
					   String[] mthExceptions) {
		super(Opcodes.ASM5, cv);
		this.mAccess = mthAccess;
		this.mName = mthName;
		this.mDesc = mthDesc;
		this.mSignature = mthSignature;
		this.mExceptions = mthExceptions;
	}

	public void visitEnd() {
		MethodVisitor mv = cv.visitMethod(mAccess, mName, mDesc, mSignature, mExceptions);
		// create method body
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		super.visitEnd();
	}
}
