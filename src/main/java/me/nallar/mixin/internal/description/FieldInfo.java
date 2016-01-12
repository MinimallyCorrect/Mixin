package me.nallar.mixin.internal.description;

public class FieldInfo {
	public final AccessFlags accessFlags;
	public final Type type;
	public final String name;
	private Integer cachedHashCode = null;

	public FieldInfo(AccessFlags accessFlags, Type type, String name) {
		this.accessFlags = accessFlags;
		this.type = type;
		this.name = name;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode != null) {
			return cachedHashCode;
		}
		int hashCode = type.hashCode();
		hashCode = 31 * hashCode + name.hashCode();
		hashCode = 31 * hashCode + accessFlags.hashCode();
		return (cachedHashCode = hashCode);
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof FieldInfo &&
			((FieldInfo) other).accessFlags.equals(this.accessFlags) &&
			((FieldInfo) other).type.equals(this.type) &&
			((FieldInfo) other).name.equals(this.name));
	}

	@Override
	public String toString() {
		return accessFlags.toString() + ' ' + type + ' ' + name;
	}
}
