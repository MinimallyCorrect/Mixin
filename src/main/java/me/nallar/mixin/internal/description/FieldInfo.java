package me.nallar.mixin.internal.description;

public class FieldInfo {
	public final String type;
	public final String name;
	public final Flags flags;
	private Integer cachedHashCode = null;

	public FieldInfo(String type, String name, Flags flags) {
		this.type = type;
		this.name = name;
		this.flags = flags;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode != null) {
			return cachedHashCode;
		}
		int hashCode = type.hashCode();
		hashCode = 31 * hashCode + name.hashCode();
		hashCode = 31 * hashCode + flags.hashCode();
		return (cachedHashCode = hashCode);
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof FieldInfo &&
			((FieldInfo) other).type.equals(this.type) &&
			((FieldInfo) other).name.equals(this.name));
	}

	@Override
	public String toString() {
		return type + '.' + name;
	}
}
