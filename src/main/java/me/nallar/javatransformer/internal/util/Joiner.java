package me.nallar.javatransformer.internal.util;

import java.util.*;

public interface Joiner {
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

	String join(Iterable<Object> s);
}
