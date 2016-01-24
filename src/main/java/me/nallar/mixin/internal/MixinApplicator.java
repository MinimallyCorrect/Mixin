package me.nallar.mixin.internal;

import lombok.Data;
import lombok.NonNull;
import lombok.val;
import me.nallar.javatransformer.api.Annotated;
import me.nallar.javatransformer.api.Annotation;
import me.nallar.javatransformer.api.ClassInfo;
import me.nallar.javatransformer.api.MethodInfo;

import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.*;

@Data
public class MixinApplicator {
	private static Map<String, Consumer<Annotated>> consumerMap = new HashMap<>();
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

	static {
		addAnnotationHandler("New", x -> {
			// TODO: 24/01/2016
		});
	}

	private void handleAnnotations(ClassInfo clazz) {
		List<Annotation> mixins = clazz.getAnnotations(Names.MIXIN_FULL);

		if (mixins.size() == 0)
			if (noMixinIsError) throw new RuntimeException("Class " + clazz.getName() + " is not an @Mixin");
			else return;

		if (mixins.size() > 1)
			throw new RuntimeException(clazz.getName() + " can not use @Mixin multiple times");

		val mixin = mixins.get(0);
		String target = (String) mixin.values.get("target");

		if (target == null || target.isEmpty()) {
			target = clazz.getSuperType().getClassName();
		}

		logInfo("Found Mixin class '" + clazz.getName() + "' targeting class '" + target);
		clazz.getMethods().forEach(MixinApplicator::handleAnnotation);
	}

	@SuppressWarnings("unchecked")
	private static void addAnnotationHandler(String name, Consumer<? extends Annotated> methodInfoConsumer) {
		consumerMap.put(name, (Consumer<Annotated>) methodInfoConsumer);
	}

	private static void handleAnnotation(Annotated annotated) {
		annotated.getAnnotations().forEach(annotation -> Optional.of(consumerMap.get(annotation.type.getClassName())).ifPresent(x -> x.accept(annotated)));
	}

	private void logInfo(String s) {
		// TODO: 24/01/2016 Call
		System.out.println(s);
	}

	public static class Names {
		public static String PACKAGE = "me.nallar.mixin.";
		public static String ADD = "Add";
		public static String MIXIN = "Mixin";

		public static String ADD_FULL = PACKAGE + ADD;
		public static String MIXIN_FULL = PACKAGE + MIXIN;
	}

	private interface AnnotationApplier {
		public void apply(Annotation a, Annotated annotatedNode, ClassInfo mixinSource, ClassInfo mixinTarget);
	}
}
