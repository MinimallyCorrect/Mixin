package org.minimallycorrect.mixinplugin.deps;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.internal.file.FileCollectionInternal;

@Getter
@Setter
@ToString(callSuper = true)
public class GeneratedDependency extends DefaultSelfResolvingDependency {
	public String group;
	public String name;
	public String version;
	public FileCollectionInternal source;

	public GeneratedDependency(MModuleComponentIdentifier identifier, FileCollectionInternal source) {
		super(identifier, source);

		group = identifier.group;
		name = identifier.name;
		version = identifier.version;
		this.source = source;
		try {
			because("Generated from Mixins");
		} catch (NoSuchMethodError ignored) {
			// not supported in gradle 4.2.1
		}
	}

	@Override
	public DefaultSelfResolvingDependency copy() {
		return new GeneratedDependency(new MModuleComponentIdentifier(group, name, version), source);
	}

	public static GeneratedDependency makeGeneratedDependency(Project project, Task task, File file, MModuleComponentIdentifier identifier) {
		return new GeneratedDependency(identifier, (FileCollectionInternal) taskDependentFileCollection(project, task, file));
	}

	static FileCollection taskDependentFileCollection(Project project, Task task, File file) {
		return project.files(file, it -> it.builtBy(task));
	}
}
