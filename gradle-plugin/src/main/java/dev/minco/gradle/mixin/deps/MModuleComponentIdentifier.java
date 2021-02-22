package dev.minco.gradle.mixin.deps;

import lombok.Data;
import lombok.NonNull;

import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;

@Data
public class MModuleComponentIdentifier implements ModuleComponentIdentifier {
	String group;
	String name;
	String version;

	public MModuleComponentIdentifier(String group, String name, String version) {
		this.group = group;
		this.name = name;
		this.version = version;
	}

	@NonNull
	@Override
	public String getModule() {
		return name;
	}

	@NonNull
	@Override
	public ModuleIdentifier getModuleIdentifier() {
		return new ModuleIdentifier() {
			@NonNull
			@Override
			public String getGroup() {
				return group;
			}

			@NonNull
			@Override
			public String getName() {
				return name;
			}
		};
	}

	@NonNull
	@Override
	public String getDisplayName() {
		return group + ':' + name + ':' + version;
	}
}
