package me.nallar.mixin.internal.description;

import org.junit.Assert;

public class DescriptorTest {
	@org.junit.Test
	public void testGetReturnType() throws Exception {
		Descriptor d = new Descriptor("()Ljava/lang/String;", null);
		Type t = d.getReturnType();

		Assert.assertEquals("Ljava/lang/String;", t.real);
		Assert.assertEquals(null, t.generic);
		Assert.assertEquals("java.lang.String", t.getClassName());
	}
}
