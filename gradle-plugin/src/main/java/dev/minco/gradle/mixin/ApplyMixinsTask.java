package dev.minco.gradle.mixin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dev.minco.gradle.mixin.deps.GeneratedDependency;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import dev.minco.gradle.mixin.deps.MModuleComponentIdentifier;

@CacheableTask
public class ApplyMixinsTask extends DefaultTask {
	private final Property<ApplyMixinsRepo> applyMixinsRepo;

	@Nested
	public Property<ApplyMixinsRepo> getApplyMixinsRepo() {
		return applyMixinsRepo;
	}

	@Inject
	public ApplyMixinsTask(ObjectFactory factory) {
		applyMixinsRepo = factory.property(ApplyMixinsRepo.class);
	}

	@TaskAction
	public void run() {
		applyMixinsRepo.get().remapMixinArtifacts(getProject().getDependencies());
	}

	public List<Dependency> getGeneratedDependenciesForOutputs(Project project, Task task) {
		List<Dependency> result = new ArrayList<>();
		applyMixinsRepo.get().getOutputDependencyFiles().forEach((dep, file) -> result.add(GeneratedDependency.makeGeneratedDependency(project, task == null ? this : task, file,
			new MModuleComponentIdentifier(dep.getGroup(), dep.getName(), dep.getVersion()))));
		return result;
	}
}
