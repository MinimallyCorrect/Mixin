package mixin;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FilesTest {
	@Test
	public void toByteArray() throws IOException {
		byte[] result = com.google.common.io.Files.toByteArray(new File("any"));
		Assert.assertArrayEquals(result, new byte[]{0, 1, 2, 3, 4});
	}
}
