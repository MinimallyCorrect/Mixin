package org.minimallycorrect.test.mixins;

import java.io.File;
import java.io.IOException;

import org.minimallycorrect.mixin.Mixin;
import org.minimallycorrect.mixin.Overwrite;

@Mixin(target = "com.google.common.io.Files")
public abstract class MixinFiles {
	@Overwrite
	public static byte[] toByteArray(File file) throws IOException {
		return new byte[]{0, 1, 2, 3, 4};
	}
}
