package me.nallar.javatransformer.internal.description;

import org.junit.Assert;

public class MethodDescriptorTest {
	@org.junit.Test
	public void testGetReturnType() throws Exception {
		MethodDescriptor d = new MethodDescriptor("()Ljava/lang/String;", null);
		Type t = d.getReturnType();

		Assert.assertEquals("Ljava/lang/String;", t.real);
		Assert.assertEquals(null, t.generic);
		Assert.assertEquals("java.lang.String", t.getClassName());
	}
}
