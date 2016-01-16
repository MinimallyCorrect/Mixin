package me.nallar.javatransformer.internal.util;

import java.util.*;

public interface Splitter {
	static Splitter on(char c) {
		return s -> {
			ArrayList<String> split = new ArrayList<>();
			int current = 0;
			do {
				int next = current = s.indexOf(c, current);

				if (next == -1)
					next = s.length();

				String part = s.substring(0, next).trim();

				if (!part.isEmpty())
					split.add(part);
			} while (current != -1);
			return split;
		};
	}

	Iterable<String> split(String s);
}
