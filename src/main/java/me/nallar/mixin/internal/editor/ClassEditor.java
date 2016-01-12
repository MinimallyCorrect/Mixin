package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ClassEditor {
	AccessFlags getAccessFlags();

	void setAccessFlags(AccessFlags accessFlags);

	void add(MethodInfo method);

	void add(FieldInfo field);

	List<MethodInfo> getMethods();

	List<FieldInfo> getFields();

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
