package org.minimallycorrect.mixinplugin;

import javax.inject.Inject;

import lombok.NonNull;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;

import org.minimallycorrect.mixin.internal.ApplicationType;

/**
 * Separate concrete impl, only used when not using transforms. Old gradle can't create getters for the Properties.
 */
public class ApplyMixinsImpl extends ApplyMixins {
	@Input
	private final Property<ApplicationType> applicationType;

	@InputFiles
	@Classpath
	private final Property<ConfigurableFileCollection> mixinSource;

	@Inject
	public ApplyMixinsImpl(ObjectFactory objectFactory) {
		applicationType = objectFactory.property(ApplicationType.class);
		mixinSource = objectFactory.property(ConfigurableFileCollection.class);
	}

	@NonNull
	@Input
	public Property<ApplicationType> getApplicationType() {
		return this.applicationType;
	}

	@NonNull
	@InputFiles
	@Classpath
	public Property<ConfigurableFileCollection> getMixinSource() {
		return this.mixinSource;
	}
}
