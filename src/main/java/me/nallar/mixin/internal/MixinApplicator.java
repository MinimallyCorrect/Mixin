package me.nallar.mixin.internal;

import lombok.Data;
import lombok.val;
import me.nallar.javatransformer.api.*;
import me.nallar.whocalled.WhoCalled;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@Data
public class MixinApplicator {
	private static final Map<String, AnnotationApplier<? extends ClassMember>> consumerMap = new HashMap<>();
	private static final Map<Path, List<String>> sources = new HashMap<>();

	static {
		addAnnotationHandler(ClassInfo.class, (applicator, annotation, member, target) -> {
			logInfo("Handling class " + member.getName() + " with annotation " + annotation);

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

	private static boolean packageNameMatches(String className, List<String> packages) {
		for (String s : packages) {
			if (s == null || className.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	private static void logInfo(String s) {
		// TODO: 24/01/2016 Proper logging
		System.out.println(s);
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

	public void addSource(String mixinPackage) {
		try {
			addSource(Class.forName(mixinPackage + ".package-info", true, WhoCalled.$.getCallingClass().getClassLoader()));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void addSource(Class<?> mixinSource) {
		addSource(JavaTransformer.pathFromClass(mixinSource), mixinSource.getPackage().getName());
	}

	public void addSource(Path mixinSource) {
		addSource(mixinSource, null);
	}

	public void addSource(Path mixinSource, String packageName) {
		List<String> current = sources.get(mixinSource);

		if (current == null) {
			current = new ArrayList<>();
			sources.put(mixinSource, current);
		}

		if (current.contains(null))
			return;

		if (packageName == null)
			current.clear();

		current.add(packageName);
	}

	public JavaTransformer getMixinTransformer() {
		val transformers = new ArrayList<Transformer.TargetedTransformer>();

		for (Map.Entry<Path, List<String>> pathListEntry : sources.entrySet()) {
			JavaTransformer transformer = new JavaTransformer();
			transformer.addTransformer(classInfo -> {
				if (packageNameMatches(classInfo.getName(), pathListEntry.getValue()))
					Optional.ofNullable(MixinApplicator.this.processMixinSource(classInfo)).ifPresent(transformers::add);
			});

			transformer.parse(pathListEntry.getKey());
		}

		JavaTransformer transformer = new JavaTransformer();
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

	private interface AnnotationApplier<T extends ClassMember> {
		void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget);
	}
}
