package dev.minco.test.mixins;

import java.io.File;
import java.io.IOException;

import dev.minco.mixin.Mixin;
import dev.minco.mixin.Overwrite;

@Mixin(target = "com.google.common.io.Files")
public abstract class MixinFiles {
	@Overwrite
	public static byte[] toByteArray(File file) throws IOException {
		return new byte[]{0, 1, 2, 3, 4};
	}
}
