package me.nallar.mixin.internal.editor.asm;

import lombok.val;
import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.Parameter;
import me.nallar.mixin.internal.description.Type;
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

		val info = ByteCodeEditor.MethodNodeInfo.wrap(node);

		Assert.assertEquals("test", info.getName());
		Assert.assertEquals("java.lang.String", info.getReturnType().getClassName());
		Assert.assertEquals(AccessFlags.ACC_PUBLIC, info.getAccessFlags().access);

		info.setReturnType(new Type("Ljava/lang/Boolean;"));
		Assert.assertEquals("()Ljava/lang/Boolean;", node.desc);

		val parameters = info.getParameters();
		parameters.add(new Parameter(null, "test"));
		Assert.assertEquals(info.getParameters());
	}
}
