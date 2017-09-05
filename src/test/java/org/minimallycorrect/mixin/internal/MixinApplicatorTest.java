package org.minimallycorrect.mixin.internal;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import org.minimallycorrect.mixin.internal.mixinsource.PackageReference;

import java.nio.file.*;

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
		Assert.assertTrue("path should be added successfully", applicator.addSearchPath(Paths.get("test")));
		Assert.assertFalse("path should not be added successfully", applicator.addSearchPath(Paths.get("test")));
		Assert.assertFalse("path should not be added successfully", applicator.addSearchPath(Paths.get("./test")));
		Assert.assertFalse("path should not be added successfully", applicator.addSearchPath(Paths.get("./asds/../test")));
		val transformer = applicator.getMixinTransformer();
		Assert.assertEquals("test", transformer.getSearchPaths().get(0).getFileName().toString());
		Assert.assertEquals(1, transformer.getSearchPaths().size());
	}

	@SneakyThrows
	@Test
	public void testApplication() throws Exception {
		val applicator = new MixinApplicator();
		applicator.addSource("org.minimallycorrect.mixin.internal.mixinsource");
		val transformer = applicator.getMixinTransformer();
		transformer.load(Paths.get("src/test/java"));
	}
}
