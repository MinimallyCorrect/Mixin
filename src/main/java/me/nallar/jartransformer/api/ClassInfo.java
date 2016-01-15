package me.nallar.jartransformer.api;

import java.util.*;
import java.util.function.*;

public interface ClassInfo extends Named, Accessible {
	void add(MethodInfo method);

	void add(FieldInfo field);

	List<MethodInfo> getMethods();

	List<FieldInfo> getFields();

	default void accessFlags(Function<AccessFlags, AccessFlags> c) {
		setAccessFlags(c.apply(getAccessFlags()));
	}
}
