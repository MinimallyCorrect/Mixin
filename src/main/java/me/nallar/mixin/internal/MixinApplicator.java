package me.nallar.mixin.internal;

import lombok.Data;
import lombok.NonNull;
import lombok.val;
import me.nallar.javatransformer.api.Annotation;
import me.nallar.javatransformer.api.ClassInfo;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Data
public class MixinApplicator {
	@NonNull
	private final Path targetJar;
	@NonNull
	private final Path mixinSource;
	private boolean noMixinIsError = false;

	public MixinApplicator(Path targetJar, Path mixinSource) {
		this.targetJar = targetJar;
		this.mixinSource = mixinSource;
	}

	public static void main(String[] args) {

	}

	private void handleAnnotations(ClassInfo info) {
		List<Annotation> mixins = info.getAnnotations(Names.MIXIN_FULL);

		if (mixins.size() == 0)
			if (noMixinIsError) throw new RuntimeException("Class " + info.getName() + " is not an @Mixin");
			else return;

		if (mixins.size() > 1)
			throw new RuntimeException(info.getName() + " can not use @Mixin multiple times");

		val mixin = mixins.get(0);
		String target = (String) mixin.values.get("target");

		if (target == null || target.isEmpty()) {
			target = info.getSuperType().getClassName();
		}
	}

	public static class Names {
		public static String PACKAGE = "me.nallar.mixin.";
		public static String MIXIN = "Mixin";
		public static String NEW = "New";

		public static String MIXIN_FULL = PACKAGE + MIXIN;
	}
}
