package me.nallar.mixin.internal;

import lombok.val;
import me.nallar.mixin.internal.mixinsource.MixinSource;
import org.junit.Assert;
import org.junit.Test;

public class MixinApplicatorTest {
	@Test
	public void testGetMixinTransformer() throws Exception {
		val applicator = new MixinApplicator();
		val transformer = applicator.getMixinTransformer(MixinSource.class);

		Assert.assertTrue("Must have at least one mixin transformer registered", transformer.getClassTransformers().size() != 0);
	}
}
