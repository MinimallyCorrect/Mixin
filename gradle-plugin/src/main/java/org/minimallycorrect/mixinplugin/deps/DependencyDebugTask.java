package org.minimallycorrect.mixinplugin.deps;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.tasks.TaskAction;

public class DependencyDebugTask extends DefaultTask {
	@TaskAction
	public final void run() {
		for (Configuration configuration : getProject().getConfigurations()) {
			debugConfiguration(configuration);
		}
	}

	private void debugConfiguration(Configuration configuration) {
		System.out.println("configuration: " + configuration.getName() + " in " + getProject().getPath());
		for (Dependency dependency : configuration.getDependencies()) {
			System.out.println("\tdependency " + dependency + " of type " + dependency.getClass().getCanonicalName());
		}

		if (configuration.isCanBeResolved()) {
			ResolvedConfiguration var9 = configuration.getResolvedConfiguration();

			for (ResolvedArtifact resolved : var9.getResolvedArtifacts()) {
				System.out.println("\tartifact: " + resolved + "\t" + resolved.getFile());
			}
		}
	}
}
