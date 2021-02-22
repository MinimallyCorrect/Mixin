package dev.minco.gradle.mixin;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.minco.mixin.internal.ApplicationType;
import dev.minco.gradle.mixin.deps.DependencyDebugTask;
import dev.minco.gradle.mixin.transform.MixinTransform;

@SuppressWarnings("unused")
public class MixinGradlePlugin implements Plugin<Project> {
	private Settings settings;
	private static final Logger logger = LoggerFactory.getLogger(MixinGradlePlugin.class);
	private static final Attribute<String> artifactType = Attribute.of("artifactType", String.class);
	private static final Attribute<Boolean> mixined = Attribute.of("mixined", Boolean.class);

	public void apply(@NonNull Project project) {
		settings = new Settings();
		project.getExtensions().add("mixin", settings);
		val mixinsTask = project.task("applySubprojectMixins");
		project.getTasks().named("compileJava").configure(it -> {
			it.dependsOn(mixinsTask);
		});
		Utils.registerTask(project.getTasks(), "mixinDependencyDebug", DependencyDebugTask.class);

		project.afterEvaluate(this::afterEvaluate);
	}

	private void registerTransform(Project project, String type, Map<String, ApplyMixins> applyMixinsMap) {
		project.getDependencies().registerTransform(MixinTransform.class, it -> {
			it.getFrom().attribute(artifactType, type).attribute(mixined, false);
			it.getTo().attribute(artifactType, type).attribute(mixined, true);

			it.parameters(params -> {
				params.getPerDependencyApplyMixins().putAll(applyMixinsMap);
				params.setArtifactType(type);
				params.setCacheBust(LocalDate.now().toString());
			});
		});
	}

	private void afterEvaluate(Project project) {
		if (project.getState().getFailure() != null) {
			return;
		}

		val useTransforms = settings.canApplyTransforms();
		if (useTransforms) {
			project.getDependencies().attributesSchema(it -> it.attribute(mixined));
			project.getDependencies().getArtifactTypes().getByName(ArtifactTypeDefinition.JAR_TYPE).getAttributes().attribute(mixined, false);
			project.getConfigurations().all(it -> it.getAttributes().attribute(mixined, true));
		}

		val applyMixinsMap = new HashMap<String, ApplyMixins>();

		val mixinsTask = project.getTasks().getByName("applySubprojectMixins");
		val allMixedinCfg = project.getConfigurations().create("mixedin");

		project.getConfigurations().getByName("implementation").extendsFrom(allMixedinCfg);

		settings.targets.forEach((subproject, deps) -> {
			val mixinProject = project.project(subproject);
			val mixinTargetsCfg = mixinProject.getConfigurations().create("mixinTargets", it -> {
				it.setVisible(false);
				it.setTransitive(false);
				it.getDependencies().addAll(deps);
			});
			val mixinPrePatchedCfg = mixinProject.getConfigurations().create("mixinPrePatched");
			val mixinTransitive = mixinProject.getConfigurations().create("mixinTransitive", it -> it.getDependencies().addAll(deps));
			val mixinAppliedCfg = mixinProject.getConfigurations().create("mixinApplied", it -> it.extendsFrom(mixinTransitive));
			mixinProject.getPlugins().apply(JavaPlugin.class);

			val sourceSet = mixinProject.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main");

			// if we're using transform, use the abstract class which gradle makes getters for properties
			// older gradle needs the concrete one
			ApplyMixins preApplyMixins = useTransforms ? project.getObjects().newInstance(ApplyMixins.class) : new ApplyMixinsImpl(project.getObjects(), project.files());
			preApplyMixins.getMixinSource().from(sourceSet.getAllJava().getSourceDirectories());
			preApplyMixins.getApplicationType().set(ApplicationType.PRE_PATCH);
			val preApplyMixinsRepo = new ApplyMixinsRepo(preApplyMixins, new File(mixinProject.getBuildDir(), "mixin-pre"), mixinTargetsCfg);

			val preApplyMixinTask = mixinProject.getTasks().create("preApplyMixins", ApplyMixinsTask.class, it -> it.getApplyMixinsRepo().set(preApplyMixinsRepo));
			mixinPrePatchedCfg.getDependencies().addAll(preApplyMixinTask.getGeneratedDependenciesForOutputs(mixinProject, preApplyMixinTask));
			mixinProject.getConfigurations().getByName("implementation").extendsFrom(mixinPrePatchedCfg);

			ApplyMixins applyMixins = useTransforms ? project.getObjects().newInstance(ApplyMixins.class) : new ApplyMixinsImpl(project.getObjects(), project.files());
			applyMixins.getMixinSource().from(mixinProject.getTasks().getByName("jar").getOutputs().getFiles());
			applyMixins.getApplicationType().set(ApplicationType.FINAL_PATCH);
			val applyMixinsRepo = new ApplyMixinsRepo(applyMixins, new File(mixinProject.getBuildDir(), "mixin"), mixinTargetsCfg);
			val mixinTask = mixinProject.getTasks().create("applyMixins", ApplyMixinsTask.class, it -> it.getApplyMixinsRepo().set(applyMixinsRepo));
			// gradle <= 4.10.2 doesn't set this automatically from the file dependency
			mixinTask.dependsOn("jar");
			mixinsTask.dependsOn(mixinTask);

			for (Dependency dep : deps) {
				applyMixinsMap.put(Utils.getId(dep), applyMixins);
			}

			for (val file : mixinTask.getOutputs().getFiles()) {
				mixinProject.getArtifacts().add(mixinAppliedCfg.getName(), file, it -> it.builtBy(mixinTask));
			}

			if (useTransforms) {
				project.getConfigurations().getByName("implementation").getDependencies().addAll(deps);
			} else {
				val generatedDependencies = mixinTask.getGeneratedDependenciesForOutputs(project, mixinsTask);
				project.getConfigurations().getByName("implementation").getDependencies().addAll(generatedDependencies);

				logger.info("Mixin subproject {} set up with generated deps: {}", mixinProject.getPath(), generatedDependencies);
			}
		});

		if (useTransforms) {
			registerTransform(project, ArtifactTypeDefinition.JAR_TYPE, applyMixinsMap);
			registerTransform(project, ArtifactTypeDefinition.JVM_CLASS_DIRECTORY, applyMixinsMap);
			// TODO: this is intended to be for source jars but doesn't work?
			registerTransform(project, "java", applyMixinsMap);
		}
	}

	@Getter
	public static class Settings {
		@Getter
		Map<String, List<Dependency>> targets = new HashMap<>();

		@Setter
		boolean useArtifactTransforms = true;

		public void target(String subproject, List<Dependency> deps) {
			targets.put(subproject, deps);
		}

		public boolean canApplyTransforms() {
			if (!useArtifactTransforms) {
				return false;
			}
			try {
				TransformAction.class.getCanonicalName();
			} catch (NoClassDefFoundError ignored) {
				return false;
			}
			// Need at least 5.6 for @Nested
			return GradleVersion.current().compareTo(GradleVersion.version("5.6")) >= 0;
		}
	}
}
