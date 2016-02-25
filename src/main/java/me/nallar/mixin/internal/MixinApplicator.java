package me.nallar.mixin.internal;

import lombok.*;
import me.nallar.javatransformer.api.*;
import me.nallar.whocalled.WhoCalled;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("CodeBlock2Expr")
@Data
public class MixinApplicator {
	private static final Map<String, List<SortableAnnotationApplier<? extends ClassMember>>> consumerMap = new HashMap<>();
	private static final Map<Path, List<String>> sources = new HashMap<>();

	static {
		addAnnotationHandler(ClassInfo.class, SortableAnnotationApplier.of(-1, (applicator, annotation, member, target) -> {
			logInfo("Handling class " + member.getName() + " with annotation " + annotation);
		}), "Mixin");

		addAnnotationHandler(ClassInfo.class, SortableAnnotationApplier.of(1, (applicator, annotation, member, target) -> {
			if (!applicator.makeAccessible)
				return;

			Object makePublicObject = annotation.values.get("makePublic");
			boolean makePublic = makePublicObject != null && (Boolean) makePublicObject;

			target.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL));
			target.getMembers().forEach((it) -> it.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL)));
		}), "Mixin");

		addAnnotationHandler(FieldInfo.class, (applicator, annotation, member, target) -> {
			String name = member.getName();
			if (!name.endsWith("_"))
				throw new MixinError("Name of @Add-ed field must end with '_'");

			target.add(member);
			target.get(member).setName(name.substring(0, name.length() - 1));
		}, "Add");

		addAnnotationHandler(MethodInfo.class, (applicator, annotation, member, target) -> {
			target.add(member);
		}, "Add");

		addAnnotationHandler(MethodInfo.class, (applicator, annotation, member, target) -> {
			MethodInfo existing = target.get(member);

			if (existing == null) {
				throw new MixinError("Can't override method " + member + " as it does not exist in target: " + target + "\nMethods in target: " + target.getMethods());
			}

			target.remove(existing);
			target.add(member);
		}, "java.lang.Override", "OverrideStatic");
	}

	private final List<TargetedTransformer> transformers = new ArrayList<>();
	private boolean makeAccessible = true;
	private boolean noMixinIsError = false;
	private boolean notAppliedIsError = true;
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private JavaTransformer transformer;

	@SuppressWarnings({"unchecked"})
	private static void addAnnotationHandler(AnnotationApplier<?> applier, String... names) {
		if (names.length == 0)
			throw new IllegalArgumentException("Must provide at least one name");

		for (String name : names) {
			if (!name.contains(".")) {
				name = "me.nallar.mixin." + name;
			}
			addAnnotationHandler(applier, name);
		}
	}

	private static void addAnnotationHandler(AnnotationApplier<?> applier, String name) {
		List<SortableAnnotationApplier<? extends ClassMember>> appliers = consumerMap.get(name);

		if (appliers == null)
			consumerMap.put(name, appliers = new ArrayList<>());

		appliers.add(applier instanceof SortableAnnotationApplier ? (SortableAnnotationApplier) applier : SortableAnnotationApplier.of(0, applier));
	}

	@SuppressWarnings("unchecked")
	private static <T extends ClassMember> void addAnnotationHandler(Class<T> clazz, AnnotationApplier<T> specificApplier, String... names) {
		int index = specificApplier instanceof SortableAnnotationApplier ? ((SortableAnnotationApplier) specificApplier).getSortIndex() : 0;
		SortableAnnotationApplier<?> applier = SortableAnnotationApplier.of(index, (applicator, annotation, annotated, target) -> {
			if (clazz.isAssignableFrom(annotated.getClass())) {
				specificApplier.apply(applicator, annotation, (T) annotated, target);
			}
		});
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

	private static String ignoreException(Supplier<String> supplier, String name) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			return "Failed to get '" + name + "' due to " + t;
		}
	}

	private Stream<SortableConsumer<ClassInfo>> handleAnnotation(ClassMember annotated) {
		return annotated.getAnnotations().stream().flatMap(annotation -> {
			@SuppressWarnings("unchecked")
			List<SortableAnnotationApplier<ClassMember>> appliers = (List<SortableAnnotationApplier<ClassMember>>) (List) consumerMap.get(annotation.type.getClassName());
			if (appliers == null)
				return null;

			return appliers.stream().map(applier -> SortableConsumer.of(applier.getSortIndex(), (Consumer<ClassInfo>) (target) -> {
				try {
					applier.apply(this, annotation, annotated, target);
				} catch (Exception e) {
					throw new MixinError("Failed to apply handler for annotation '" + annotation.type.getClassName() + "' on '" + ignoreException(annotated::toString, "annotated") + "' in '" + annotated.getClassInfo().getName() + "' to '" + target.getName() + "'", e);
				}
			}));
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
		if (this.transformer != null)
			return transformer;

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
		if (notAppliedIsError)
			transformer.getAfterTransform().add(this::checkForSkippedTransformers);
		return this.transformer = transformer;
	}

	private void checkForSkippedTransformers(JavaTransformer javaTransformer) {
		HashSet<Transformer.TargetedTransformer> notRan = transformers.stream()
			.filter(targetedTransformer -> !targetedTransformer.ran).collect(Collectors.toCollection(HashSet::new));

		if (!notRan.isEmpty()) {
			throw new MixinError(notRan.size() + " Transformers were not applied: " + transformers);
		}
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
			.flatMap(this::handleAnnotation).sorted().collect(Collectors.toList());

		logInfo("Found Mixin class '" + clazz.getName() + "' targeting class '" + target + " with " + applicators.size() + " applicators.");

		assert !applicators.isEmpty();

		final String finalTarget = target;
		TargetedTransformer transformer = new TargetedTransformer() {
			@Override
			public Collection<String> getTargetClasses() {
				return Collections.singletonList(finalTarget);
			}

			@Override
			public void transform(ClassInfo classInfo) {
				ran = true;
				applicators.forEach(applicator -> applicator.accept(classInfo));
			}
		};
		transformers.add(transformer);
		return transformer;
	}

	private interface AnnotationApplier<T extends ClassMember> {
		void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget);
	}

	private interface SortableAnnotationApplier<T extends ClassMember> extends AnnotationApplier<T> {
		static <T extends ClassMember> SortableAnnotationApplier<T> of(int index, AnnotationApplier<T> applier) {
			return new SortableAnnotationApplier<T>() {
				@Override
				public int getSortIndex() {
					return index;
				}

				@Override
				public void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget) {
					applier.apply(applicator, annotation, annotatedMember, mixinTarget);
				}
			};
		}

		int getSortIndex();
	}

	@SuppressWarnings("rawtypes")
	private interface SortableConsumer<T> extends Consumer<T>, Comparable {
		static <T> SortableConsumer<T> of(int sortIndex, Consumer<T> consumer) {
			return new SortableConsumer<T>() {
				@Override
				public int getSortIndex() {
					return sortIndex;
				}

				@Override
				public void accept(T t) {
					consumer.accept(t);
				}
			};
		}

		int getSortIndex();

		default int compareTo(Object other) {
			return Integer.compare(getSortIndex(), ((SortableConsumer) other).getSortIndex());
		}
	}

	private static abstract class TargetedTransformer implements Transformer.TargetedTransformer {
		boolean ran;

		public String toString() {
			val classes = getTargetClasses();
			return classes.size() == 1 ? classes.iterator().next() : classes.toString();
		}
	}
}
