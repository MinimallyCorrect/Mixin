package me.nallar.mixin.internal.description;

import java.util.*;

public class GenericTest {
	// access flags 0x8
	// signature <T:Ljava/lang/Object;>(Ljava/util/ArrayList<TT;>;Ljava/util/List<Ljava/lang/String;>;)TT;
	// declaration: T test<T>(java.util.ArrayList<T>, java.util.List<java.lang.String>)
	// static test(Ljava/util/ArrayList;Ljava/util/List;)Ljava/lang/Object;
	static <T> T test(ArrayList<T> a, List<String> b) {
		return a.get(0);
	}
}
