package org.minimallycorrect.mixin.internal;

import lombok.*;
import me.nallar.whocalled.WhoCalled;
import org.minimallycorrect.javatransformer.api.*;
import org.minimallycorrect.javatransformer.internal.SimpleMethodInfo;
import org.minimallycorrect.mixin.*;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("CodeBlock2Expr")
@Data
public class MixinApplicator {
	private static final Map<String, List<IndexedAnnotationApplier<? extends ClassMember>>> consumerMap = new HashMap<>();
	private static final Map<Path, List<String>> sources = new HashMap<>();

	static {
		addAnnotationHandler(ClassInfo.class, Mixin.class, Integer.MIN_VALUE, (applicator, annotation, member, target) -> {
			applicator.logInfo("Handling class " + member.getName() + " with annotation " + annotation);
		});

		addAnnotationHandler(ClassInfo.class, Mixin.class, 1, (applicator, annotation, member, target) -> {
			if (applicator.applicationType == ApplicationType.FINAL_PATCH)
				return;

			// Add no-args protected constructor if there are existing constructors and none of them are no-args
			val constructors = target.getConstructors().collect(Collectors.toList());
			if (!constructors.isEmpty() && constructors.stream().noneMatch(it -> it.getParameters().isEmpty())) {
				target.add(SimpleMethodInfo.of(new AccessFlags(AccessFlags.ACC_PROTECTED), Collections.emptyList(), target.getType(), "<init>", Collections.emptyList()));
			}

			boolean makePublic = annotation.makePublic();

			target.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL));
			target.getMembers().forEach((it) -> it.accessFlags((f) -> f.makeAccessible(makePublic).without(AccessFlags.ACC_FINAL)));
		});

		addAnnotationHandler(FieldInfo.class, Add.class, (applicator, annotation, member, target) -> {
			String name = member.getName();
			if (!name.endsWith("_"))
				throw new MixinError("Name of @Add-ed field must end with '_'");

			target.add(member);
			target.get(member).setName(name.substring(0, name.length() - 1));
		});

		addAnnotationHandler(MethodInfo.class, Add.class, (applicator, annotation, member, target) -> {
			target.add(member);
		});

		addAnnotationHandler(MethodInfo.class, (applicator, annotation, member, target) -> {
			MethodInfo existing = target.get(member);

			if (existing == null) {
				throw new MixinError("Can't override method " + member + " as it does not exist in target: " + target + "\nMethods in target: " + target.getMethods());
			}

			if (applicator.applicationType == ApplicationType.PRE_PATCH)
				return;

			target.remove(existing);
			target.add(member);
		}, "java.lang.Override", "OverrideStatic");

		addAnnotationHandler(MethodInfo.class, Synchronize.class, (applicator, annotation, member, target) -> {
			target.get(member).accessFlags(it -> it.with(AccessFlags.ACC_SYNCHRONIZED));
		});
	}

	private final List<TargetedTransformer> transformers = new ArrayList<>();
	private Consumer<String> log = System.out::println;
	private boolean noMixinIsError = false;
	private boolean notAppliedIsError = true;
	private ApplicationType applicationType = ApplicationType.FINAL_PATCH;
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private JavaTransformer transformer;

	private static void addAnnotationHandler(IndexedAnnotationApplier<?> applier, String name) {
		if (!name.contains("."))
			name = "org.minimallycorrect.mixin." + name;
		consumerMap.computeIfAbsent(name, k -> new ArrayList<>()).add(applier);
	}

	private static void addAnnotationHandler(AnnotationApplier<?> applier, int index, String... names) {
		for (String name : names)
			addAnnotationHandler(new IndexedAnnotationApplier<>(index, applier), name);
	}

	@SuppressWarnings("unchecked")
	private static <T extends ClassMember> void addAnnotationHandler(Class<T> clazz, int index, AnnotationApplier<T> applier, String... names) {
		addAnnotationHandler((applicator, annotation, member, target) -> {
			if (clazz.isAssignableFrom(target.getClass()))
				applier.apply(applicator, annotation, (T) member, target);
		}, index, names);
	}

	private static <T extends ClassMember> void addAnnotationHandler(Class<T> clazz, AnnotationApplier<T> applier, String... names) {
		addAnnotationHandler(clazz, 0, applier, names);
	}

	private static <T extends ClassMember, A extends java.lang.annotation.Annotation> void addAnnotationHandler(Class<T> clazz, Class<A> annotationClass, int index, SpecificAnnotationApplier<T, A> applier) {
		addAnnotationHandler(clazz, index, (applicator, annotation, member, target) -> {
			applier.apply(applicator, annotation.toInstance(annotationClass), member, target);
		}, annotationClass.getName());
	}

	private static <T extends ClassMember, A extends java.lang.annotation.Annotation> void addAnnotationHandler(Class<T> clazz, Class<A> annotationClass, SpecificAnnotationApplier<T, A> applier) {
		addAnnotationHandler(clazz, annotationClass, 0, applier);
	}

	private static boolean packageNameMatches(String className, List<String> packages) {
		for (String s : packages) {
			if (s == null || className.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	private static String ignoreException(Supplier<String> supplier, String name) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			return "Failed to get '" + name + "' due to " + t;
		}
	}

	private void logInfo(String s) {
		log.accept(s);
	}

	private Stream<SortableConsumer<ClassInfo>> handleAnnotation(ClassMember annotated) {
		return annotated.getAnnotations().stream().flatMap(annotation -> {
			@SuppressWarnings("unchecked")
			List<IndexedAnnotationApplier<ClassMember>> appliers = (List<IndexedAnnotationApplier<ClassMember>>) (List) consumerMap.get(annotation.type.getClassName());
			if (appliers == null)
				return null;

			return appliers.stream().map(applier -> SortableConsumer.of(applier.sortIndex, (Consumer<ClassInfo>) (target) -> {
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
		List<String> current = sources.computeIfAbsent(mixinSource, k -> new ArrayList<>());

		if (current.contains(null))
			return;

		if (packageName == null)
			current.clear();

		current.add(packageName);

		transformer = null;
	}

	public JavaTransformer getMixinTransformer() {
		JavaTransformer transformer = this.transformer;
		if (transformer != null)
			return transformer;

		val transformers = new ArrayList<Transformer.TargetedTransformer>();

		for (Map.Entry<Path, List<String>> pathListEntry : sources.entrySet()) {
			transformer = new JavaTransformer();
			transformer.addTransformer(classInfo -> {
				if (packageNameMatches(classInfo.getName(), pathListEntry.getValue()))
					Optional.ofNullable(MixinApplicator.this.processMixinSource(classInfo)).ifPresent(transformers::add);
			});

			transformer.parse(pathListEntry.getKey());
		}

		transformer = new JavaTransformer();
		transformers.forEach(transformer::addTransformer);
		if (notAppliedIsError)
			transformer.getAfterTransform().add(javaTransformer -> checkForSkippedTransformers());
		return this.transformer = transformer;
	}

	public void setLog(Consumer<String> log) {
		this.log.accept("Unregistering logger " + this.log + ", registering " + log);
		this.log = log;
	}

	private void checkForSkippedTransformers() {
		HashSet<Transformer.TargetedTransformer> notRan = transformers.stream()
			.filter(targetedTransformer -> !targetedTransformer.ran).collect(Collectors.toCollection(HashSet::new));

		if (!notRan.isEmpty()) {
			throw new MixinError(notRan.size() + " Transformers were not applied: " + transformers);
		}
	}

	private Transformer.TargetedTransformer processMixinSource(ClassInfo clazz) {
		List<Annotation> mixins = clazz.getAnnotations("org.minimallycorrect.mixin.Mixin");

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

		List<Consumer<ClassInfo>> applicators = Stream.concat(Stream.of(clazz), clazz.getMembers())
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

	@FunctionalInterface
	private interface AnnotationApplier<T extends ClassMember> {
		void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget);
	}

	@FunctionalInterface
	private interface SpecificAnnotationApplier<T extends ClassMember, A extends java.lang.annotation.Annotation> {
		void apply(MixinApplicator applicator, A annotation, T annotatedMember, ClassInfo mixinTarget);
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

	@AllArgsConstructor
	static class IndexedAnnotationApplier<T extends ClassMember> {
		final int sortIndex;
		final AnnotationApplier<T> applier;

		void apply(MixinApplicator applicator, Annotation annotation, T annotatedMember, ClassInfo mixinTarget) {
			applier.apply(applicator, annotation, annotatedMember, mixinTarget);
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
