package me.nallar.mixin.internal.editor;

import me.nallar.mixin.internal.description.FieldDescription;
import me.nallar.mixin.internal.description.MethodDescription;

public interface JavaEditor {
	void addStub(MethodDescription description);

	void addStub(FieldDescription field);

	void makePublic(FieldDescription field);

	void makePublic(MethodDescription method);

	void getMethods(MethodDescription method);

	void getFields(FieldDescription fields);
}
