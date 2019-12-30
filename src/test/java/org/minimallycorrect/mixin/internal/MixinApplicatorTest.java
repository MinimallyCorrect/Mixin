package org.minimallycorrect.mixin.internal;

import java.nio.file.*;

import lombok.SneakyThrows;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import org.minimallycorrect.mixin.internal.mixinsource.PackageReference;

public class MixinApplicatorTest {
	@Test
	public void testGetMixinTransformer() throws Exception {
		val applicator = new MixinApplicator();
		applicator.addSource(PackageReference.class);
		val transformer = applicator.getMixinTransformer();

		Assert.assertTrue("Must have at least one mixin transformer registered", transformer.getClassTransformers().size() != 0);
	}

	@Test
	public void testGetMixinTransformerWithPackageSource() throws Exception {
		val applicator = new MixinApplicator();
		applicator.addSource("org.minimallycorrect.mixin.internal.mixinsource");
		val transformer = applicator.getMixinTransformer();

		Assert.assertTrue("Must have at least one mixin transformer registered", transformer.getClassTransformers().size() != 0);
	}

	@Test
	public void testGetMixinTransformerWithPackageSourceWrongOrder() throws Exception {
		val applicator = new MixinApplicator();
		applicator.getMixinTransformer();
		applicator.addSource("org.minimallycorrect.mixin.internal.mixinsource");
		val transformer = applicator.getMixinTransformer();

		Assert.assertTrue("Must have at least one mixin transformer registered", transformer.getClassTransformers().size() != 0);
	}

	@Test
	public void testSourcesShouldBeEmptyAfterNewCreated() throws Exception {
		new MixinApplicator().addSource("org.minimallycorrect.mixin.internal.mixinsource");
		Assert.assertEquals(0, new MixinApplicator().getSources().size());
	}

	@Test
	public void testSearchPathShouldBeAdded() throws Exception {
		val applicator = new MixinApplicator();
		val classPath = applicator.getClassPath();
		Assert.assertTrue("path should be added successfully", classPath.addPath(Paths.get("test")));
		Assert.assertFalse("path should not be added successfully", classPath.addPath(Paths.get("test")));
		Assert.assertFalse("path should not be added successfully", classPath.addPath(Paths.get("./test")));
		Assert.assertFalse("path should not be added successfully", classPath.addPath(Paths.get("./asds/../test")));
	}

	@SneakyThrows
	@Test
	public void testApplication() throws Exception {
		val applicator = new MixinApplicator();
		applicator.setFailOnInjectionError(false);
		applicator.addSource("org.minimallycorrect.mixin.internal.mixinsource");
		val transformer = applicator.getMixinTransformer();
		transformer.load(Paths.get("src/test/java"));
	}
}
