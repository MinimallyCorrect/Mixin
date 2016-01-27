package me.nallar.mixin.internal;

import lombok.Data;
import lombok.val;
import me.nallar.javatransformer.api.*;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Data
public class MixinApplicator {
	private static final Map<String, AnnotationApplier<? extends ClassMember>> consumerMap = new HashMap<>();

	static {
		addAnnotationHandler(ClassInfo.class, (applicator, annotation, member, target) -> {
			System.out.println("Handling class " + member + " with annotation " + annotation);

			if (!applicator.makeAccessible)
				return;

			Object makePublicObject = annotation.values.get("makePublic");
			boolean makePublic = makePublicObject != null && (Boolean) makePublicObject;

			target.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL));
			target.getMembers().forEach((it) -> it.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL)));
		}, "Mixin");

		addAnnotationHandler((applicator, annotation, member, target) -> target.add(member), "Add");

		addAnnotationHandler(MethodInfo.class, (applicator, annotation, member, target) -> {
			MethodInfo existing = target.get(member);

			if (existing == null) {
				throw new MixinError("Can't override method " + member + " as it does not exist in target: " + target);
			}

			target.remove(existing);
			target.add(member);
		}, "java.lang.Override", "OverrideStatic");
	}

	private boolean makeAccessible = true;
	private boolean noMixinIsError = false;

	@SuppressWarnings({"unchecked"})
	private static void addAnnotationHandler(AnnotationApplier<?> methodInfoConsumer, String... names) {
		if (names.length == 0)
			throw new IllegalArgumentException("Must provide at least one name");

		for (String name : names) {
			if (!name.contains(".")) {
				name = "me.nallar.mixin." + name;
			}
			consumerMap.put(name, methodInfoConsumer);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends ClassMember> void addAnnotationHandler(Class<T> clazz, AnnotationApplier<T> methodInfoConsumer, String... names) {
		AnnotationApplier<?> applier = (applicator, annotation, annotated, target) -> {
			if (clazz.isAssignableFrom(clazz)) {
				methodInfoConsumer.apply(applicator, annotation, (T) annotated, target);
			}
			// TODO else log warning here?
		};
		addAnnotationHandler(applier, names);
	}

	private Stream<Consumer<ClassInfo>> handleAnnotation(ClassMember annotated) {
		return annotated.getAnnotations().stream().map(annotation -> {
			@SuppressWarnings("unchecked")
			AnnotationApplier<ClassMember> applier = (AnnotationApplier<ClassMember>) consumerMap.get(annotation.type.getClassName());
			if (applier == null)
				return null;

			return (Consumer<ClassInfo>) (target) -> applier.apply(this, annotation, annotated, target);
		}).filter(Objects::nonNull);
	}

	public JavaTransformer getMixinTransformer(Class<?> mixinSource) {
		return getMixinTransformer(JavaTransformer.pathFromClass(mixinSource), mixinSource.getPackage().getName());
	}

	public JavaTransformer getMixinTransformer(Path mixinSource) {
		return getMixinTransformer(mixinSource, null);
	}

	public JavaTransformer getMixinTransformer(Path mixinSource, String packageName) {
		JavaTransformer transformer = new JavaTransformer();

		val transformers = new ArrayList<Transformer.TargetedTransformer>();
		transformer.addTransformer(classInfo -> {
			if (!classInfo.getName().startsWith(packageName))
				return;

			Optional.ofNullable(MixinApplicator.this.processMixinSource(classInfo)).ifPresent(transformers::add);
		});

		transformer.parse(mixinSource);

		transformer = new JavaTransformer();
		transformers.forEach(transformer::addTransformer);
		return transformer;
	}

	private Transformer.TargetedTransformer processMixinSource(ClassInfo clazz) {
		List<Annotation> mixins = clazz.getAnnotations("me.nallar.mixin.Mixin");

		if (mixins.size() == 0)
			if (noMixinIsError) throw new RuntimeException("Class " + clazz.getName() + " is not an @Mixin");
			else return null;

		if (mixins.size() > 1)
			throw new MixinError(clazz.getName() + " can not use @Mixin multiple times");

		val mixin = mixins.get(0);
		String target = (String) mixin.values.get("target");

		if (target == null || target.isEmpty()) {
			target = clazz.getSuperType().getClassName();
		}

		if (!clazz.getAccessFlags().has(AccessFlags.ACC_ABSTRACT)) {
			throw new MixinError(clazz.getName() + " must be abstract to use @Mixin");
		}

		List<Consumer<ClassInfo>> applicators = Stream.concat(Stream.of(clazz), clazz.getMembers().stream())
			.flatMap(this::handleAnnotation).collect(Collectors.toList());

		logInfo("Found Mixin class '" + clazz.getName() + "' targeting class '" + target + " with " + applicators.size() + " applicators.");

		final String finalTarget = target;
		return new Transformer.TargetedTransformer() {
			@Override
			public Collection<String> getTargetClasses() {
				return Collections.singletonList(finalTarget);
			}

			@Override
			public void transform(ClassInfo classInfo) {
				applicators.forEach(applicator -> applicator.accept(classInfo));
			}
		};
	}

	private void logInfo(String s) {
		// TODO: 24/01/2016 Proper logging
		System.out.println(s);
	}

	private interface AnnotationApplier<T extends ClassMember> {
		void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget);
	}
}
