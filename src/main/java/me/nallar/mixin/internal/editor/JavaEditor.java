package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;

import java.util.*;

public interface JavaEditor {
	void add(MethodInfo method);

	void add(FieldInfo field);

	List<MethodInfo> getMethods();

	void getFields(FieldInfo fields);
}
