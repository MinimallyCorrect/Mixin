package me.nallar.mixin.internal.util;

import java.util.*;

public interface Joiner {
	String join(Iterable<Object> s);

	static Joiner on(String join) {
		return parts -> {
			Iterator<Object> i = parts.iterator();

			if (!i.hasNext())
				return "";

			StringBuilder sb = new StringBuilder(i.next().toString());

			while (i.hasNext())
				sb.append(join).append(i.next().toString());

			return sb.toString();
		};
	}
}
