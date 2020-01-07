package org.minimallycorrect.mixinplugin.transform;

import java.io.File;
import java.util.Map;

import lombok.NonNull;
import lombok.val;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.minimallycorrect.mixinplugin.ApplyMixins;

public abstract class MixinTransform implements TransformAction<MixinTransform.Parameters> {
	private static final Logger logger = LoggerFactory.getLogger(MixinTransform.class);

	public interface Parameters extends TransformParameters {
		@Nested
		Map<String, ApplyMixins> getPerDependencyApplyMixins();

		void setPerDependencyApplyMixins(Map<String, ApplyMixins> value);

		@Input
		String getArtifactType();

		void setArtifactType(String value);

		@Input
		String getCacheBust();

		void setCacheBust(String value);
	}

	@InputArtifact
	public abstract Provider<FileSystemLocation> getInputArtifact();

	@Override
	public void transform(@NonNull TransformOutputs outputs) {
		val input = getInputArtifact().get().getAsFile();

		String id = null;
		try {
			id = guessIdentifier(input);
			logger.debug("Received " + id + " at " + input);
		} catch (Throwable throwable) {
			logger.warn("Exception guessing id of " + input, throwable);
		}

		val applier = getParameters().getPerDependencyApplyMixins().get(id);

		if (applier == null) {
			outputs.file(input);
			return;
		}

		applier.transformArtifact(input, outputs.file(input.getName()));
	}

	// TODO this is awful but we don't get module data any other way? :C
	private static String guessIdentifier(File input) {
		for (int i = 0; i < 2; i++) {
			val result = tryIdentifier(input, i);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static String tryIdentifier(File input, int skips) {
		File skipped = input;
		for (int i = 0; i < skips; i++) {
			skipped = skipped.getParentFile();
		}
		val version = skipped.getParentFile();
		val name = version.getParentFile();
		File groupFile = name.getParentFile();

		if (!input.getName().startsWith(name.getName() + '-' + version.getName())) {
			// name + version not as expected, fail
			return null;
		}

		StringBuilder group = new StringBuilder(groupFile.getName());

		// special case for mavenLocal()
		if (!groupFile.getName().contains(".")) {
			int tries = 7;
			while (groupFile.exists()) {
				groupFile = groupFile.getParentFile();
				if (groupFile == null) {
					return null;
				}

				if (groupFile.getName().equals("repository") && groupFile.getParentFile().getName().equals(".m2")) {
					break;
				}

				group.insert(0, groupFile.getName() + ".");

				if (tries-- <= 0) {
					return null;
				}
			}
		}

		return group.append(':').append(name.getName()).append(':').append(version.getName()).toString();
	}
}
