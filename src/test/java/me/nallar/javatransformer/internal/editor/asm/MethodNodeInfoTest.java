package me.nallar.javatransformer.internal.editor.asm;

import lombok.val;
import me.nallar.javatransformer.api.AccessFlags;
import me.nallar.javatransformer.api.MethodInfo;
import me.nallar.javatransformer.internal.description.Parameter;
import me.nallar.javatransformer.internal.description.Type;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.tree.MethodNode;

public class MethodNodeInfoTest {
	@Test
	public void testWrap() throws Exception {
		MethodNode node = new MethodNode();
		node.access = 1;
		node.name = "test";
		node.desc = "()Ljava/lang/String;";

		ByteCodeInfo b = new ByteCodeInfo(null);
		MethodInfo info = b.wrap(node);

		Assert.assertEquals("test", info.getName());
		Assert.assertEquals("java.lang.String", info.getReturnType().getClassName());
		Assert.assertEquals(AccessFlags.ACC_PUBLIC, info.getAccessFlags().access);

		info.setReturnType(new Type("Ljava/lang/Boolean;"));
		Assert.assertEquals("()Ljava/lang/Boolean;", node.desc);

		val parameters = info.getParameters();
		parameters.add(new Parameter(new Type("Ljava/lang/String;", null), "test"));
		info.setParameters(parameters);

		Assert.assertEquals("(Ljava/lang/String;)Ljava/lang/Boolean;", ((ByteCodeInfo.MethodNodeInfo) info).getDescriptor());
	}
}
