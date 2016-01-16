package me.nallar.javatransformer.api;

import me.nallar.javatransformer.internal.util.JVMUtil;

public class AccessFlags {
	public static final int ACC_PUBLIC = 0x0001; // class, field, method
	public static final int ACC_PRIVATE = 0x0002; // class, field, method
	public static final int ACC_PROTECTED = 0x0004; // class, field, method
	public static final int ACC_STATIC = 0x0008; // field, method
	public static final int ACC_FINAL = 0x0010; // class, field, method, parameter
	public static final int ACC_SUPER = 0x0020; // class
	public static final int ACC_SYNCHRONIZED = 0x0020; // method
	public static final int ACC_VOLATILE = 0x0040; // field
	public static final int ACC_BRIDGE = 0x0040; // method
	public static final int ACC_VARARGS = 0x0080; // method
	public static final int ACC_TRANSIENT = 0x0080; // field
	public static final int ACC_NATIVE = 0x0100; // method
	public static final int ACC_INTERFACE = 0x0200; // class
	public static final int ACC_ABSTRACT = 0x0400; // class, method
	public static final int ACC_STRICT = 0x0800; // method
	public static final int ACC_SYNTHETIC = 0x1000; // class, field, method, parameter
	public static final int ACC_ANNOTATION = 0x2000; // class
	public static final int ACC_ENUM = 0x4000; // class(?) field inner
	public static final int ACC_MANDATED = 0x8000; // parameter
	public final int access;

	public AccessFlags(int access) {
		this.access = access;
	}

	@Override
	public String toString() {
		return "Access: " + access + " (" + JVMUtil.accessIntToString(access) + ")";
	}

	@Override
	public boolean equals(Object o) {
		return o == this || (o instanceof AccessFlags && ((AccessFlags) o).access == access);
	}

	@Override
	public int hashCode() {
		return access;
	}

	public AccessFlags makeAccessible(boolean needsPublic) {
		return new AccessFlags(JVMUtil.makeAccess(access, needsPublic));
	}

	public AccessFlags with(int flag) {
		return new AccessFlags(access | flag);
	}

	public AccessFlags without(int flag) {
		return new AccessFlags(access & ~flag);
	}
}
