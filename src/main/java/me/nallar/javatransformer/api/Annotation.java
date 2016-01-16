package me.nallar.javatransformer.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.nallar.javatransformer.internal.description.Type;

import java.util.*;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Annotation {
	public final Type type;
	public final Map<String, Object> values;

	public static Annotation of(Type t, Object value) {
		val map = new HashMap<String, Object>();
		map.put("value", value);
		return of(t, map);
	}

	public static Annotation of(Type t) {
		return of(t, Collections.emptyMap());
	}
}
