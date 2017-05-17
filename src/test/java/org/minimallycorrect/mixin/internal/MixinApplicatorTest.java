package org.minimallycorrect.mixin.internal;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import org.minimallycorrect.mixin.internal.mixinsource.MixinSource;

public class MixinApplicatorTest {
	@Test
	public void testGetMixinTransformer() throws Exception {
		val applicator = new MixinApplicator();
		applicator.addSource(MixinSource.class);
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
}
