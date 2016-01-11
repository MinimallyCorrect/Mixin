package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodDescription;

public interface JavaEditor {
	void addStub(MethodDescription description);

	void addStub(FieldInfo field);

	void makePublic(FieldInfo field);

	void makePublic(MethodDescription method);

	void getMethods(MethodDescription method);

	void getFields(FieldInfo fields);
}
