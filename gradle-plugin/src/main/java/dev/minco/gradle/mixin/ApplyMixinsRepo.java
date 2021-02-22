package dev.minco.gradle.mixin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import dev.minco.gradle.mixin.deps.MModuleComponentIdentifier;

@AllArgsConstructor
public class ApplyMixinsRepo {
	private ApplyMixins applyMixins;

	@NonNull
	public File repo;

	@NonNull
	private transient Configuration mixinConfiguration;

	@Nested
	public ApplyMixins getApplyMixins() {
		return this.applyMixins;
	}

	@NonNull
	@OutputDirectory
	public File getRepo() {
		return this.repo;
	}

	@Internal
	@NonNull
	public final Map<Dependency, File> getOutputDependencyFiles() {
		val result = new HashMap<Dependency, File>();
		for (Dependency dependency : mixinConfiguration.getDependencies()) {
			result.put(dependency, new File(repo, getMavenPath(dependency.getGroup(), dependency.getName(), dependency.getVersion() + '-' + getStage()) + ".jar"));
		}
		return result;
	}

	@Internal
	@NonNull
	public String getStage() {
		val type = applyMixins.getApplicationType().get();
		switch (type) {
			case PRE_PATCH:
				return "pre";
			case FINAL_PATCH:
				return "final";
		}
		throw new IllegalStateException("Unexpected value: " + type);
	}

	@SuppressWarnings("unchecked")
	public void remapMixinArtifacts(@NonNull DependencyHandler dependencyHandler) {
		val applicator = applyMixins.makeApplicator();
		val config = mixinConfiguration;
		config.resolve();
		val resolved = config.getResolvedConfiguration();
		val stage = getStage();
		resolved.rethrowFailure();

		for (val artifact : resolved.getResolvedArtifacts()) {
			val id = artifact.getId().getComponentIdentifier();
			if (id instanceof ModuleComponentIdentifier) {
				val mcid = (ModuleComponentIdentifier) id;
				val output = new File(repo, getMavenPath(mcid.getGroup(), mcid.getModule(), mcid.getVersion() + '-' + stage) + ".jar");
				output.getParentFile().mkdirs();
				applicator.getMixinTransformer().transform(artifact.getFile().toPath(), output.toPath());
			}
		}

		ArtifactResolutionQuery query = artifactResolution(dependencyHandler, mixinConfiguration.getDependencies());
		query = query.withArtifacts(MavenModule.class, MavenPomArtifact.class);
		val result = query.execute();
		for (val resolvedComponent : result.getResolvedComponents()) {
			val id = resolvedComponent.getId();
			if (id instanceof ModuleComponentIdentifier) {
				val mcid = (ModuleComponentIdentifier) id;
				for (val artifact : resolvedComponent.getArtifacts(MavenPomArtifact.class)) {
					if (artifact instanceof ResolvedArtifactResult) {
						Utils.setPomRootVal(((ResolvedArtifactResult) artifact).getFile(), new File(repo, getMavenPath(mcid.getGroup(), mcid.getModule(), mcid.getVersion() + '-' + stage) + ".pom"), "version", ((ModuleComponentIdentifier) id).getVersion() + '-' + stage);
					}
				}
			}
		}
	}

	private static ArtifactResolutionQuery artifactResolution(DependencyHandler dependencyHandler, DependencySet dependencies) {
		ArtifactResolutionQuery query = dependencyHandler.createArtifactResolutionQuery();
		for (val target : dependencies) {
			query = query.forComponents(new MModuleComponentIdentifier(target.getGroup(), target.getName(), target.getVersion()));
		}
		return query;
	}

	private static String getMavenPath(String group, String name, String version) {
		return group.replace('.', '/') + '/' + name + '/' + version + '/' + name + '-' + version;
	}
}
