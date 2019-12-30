package org.minimallycorrect.mixin.internal;

import lombok.SneakyThrows;
import lombok.val;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.minimallycorrect.javatransformer.api.JavaTransformer;
import org.minimallycorrect.mixin.internal.mixinsource.PackageReference;

public class RuntimeApplicatorTest {
	@BeforeClass
	public static void setUpMixinTarget() {
		val applicator = new MixinApplicator();
		applicator.addSource(PackageReference.class);
		val transformer = applicator.getMixinTransformer();
		transformer.load(JavaTransformer.pathFromClass(PackageReference.class));
		Class<?> clazz = transformer.defineClass(RuntimeApplicatorTest.class.getClassLoader(), "org.minimallycorrect.mixin.internal.MixinTarget");
		Assert.assertEquals(MixinTarget.class, clazz);
	}

	@Test
	public void testToString() {
		Assert.assertEquals("mixin applied", new MixinTarget().toString());
	}

	@SneakyThrows
	@Test
	public void testVoidInjectionTestBodyBefore() throws Exception {
		new MixinTarget().voidInjectionTest();
		Assert.assertEquals("true", System.getProperty("mixinInjection"));
	}
}
