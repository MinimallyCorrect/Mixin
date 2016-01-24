package me.nallar.mixin.internal;

import lombok.Data;
import lombok.NonNull;
import lombok.val;
import me.nallar.javatransformer.api.Annotation;
import me.nallar.javatransformer.api.ClassInfo;
import me.nallar.javatransformer.api.ClassMember;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Data
public class MixinApplicator {
	private static Map<String, AnnotationApplier> consumerMap = new HashMap<>();

	static {
		addAnnotationHandler(Names.ADD_FULL, (annotation, member, target) -> {
			target.add(member);
		});
	}

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

	@SuppressWarnings("unchecked")
	private static void addAnnotationHandler(String name, AnnotationApplier methodInfoConsumer) {
		consumerMap.put(name, methodInfoConsumer);
	}

	private static Stream<Consumer<ClassInfo>> handleAnnotation(ClassMember annotated) {
		return annotated.getAnnotations().stream().map(annotation -> {
			AnnotationApplier applier = consumerMap.get(annotation.type.getClassName());
			if (applier == null)
				return null;

			return (Consumer<ClassInfo>) (target) -> applier.apply(annotation, annotated, target);
		}).filter(Objects::nonNull);
	}

	private Consumer<ClassInfo> processMixinSource(ClassInfo clazz) {
		List<Annotation> mixins = clazz.getAnnotations(Names.MIXIN_FULL);

		if (mixins.size() == 0)
			if (noMixinIsError) throw new RuntimeException("Class " + clazz.getName() + " is not an @Mixin");
			else return x -> {
			};

		if (mixins.size() > 1)
			throw new RuntimeException(clazz.getName() + " can not use @Mixin multiple times");

		val mixin = mixins.get(0);
		String target = (String) mixin.values.get("target");

		if (target == null || target.isEmpty()) {
			target = clazz.getSuperType().getClassName();
		}

		List<Consumer<ClassInfo>> applicators = clazz.getMembers().stream().flatMap(MixinApplicator::handleAnnotation).collect(Collectors.toList());

		logInfo("Found Mixin class '" + clazz.getName() + "' targeting class '" + target + " with " + applicators.size() + " applicators.");

		return classInfo -> applicators.forEach(applicator -> applicator.accept(classInfo));
	}

	private void logInfo(String s) {
		// TODO: 24/01/2016 Proper logging
		System.out.println(s);
	}

	private interface AnnotationApplier {
		void apply(Annotation annotation, ClassMember annotatedMember, ClassInfo mixinTarget);
	}

	public static class Names {
		public static String PACKAGE = "me.nallar.mixin.";
		public static String ADD = "Add";
		public static String MIXIN = "Mixin";

		public static String ADD_FULL = PACKAGE + ADD;
		public static String MIXIN_FULL = PACKAGE + MIXIN;
	}
}
