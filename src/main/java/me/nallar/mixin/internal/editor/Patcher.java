package me.nallar.mixin.internal.editor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.util.JVMUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class Patcher {
	private static final Map<String, PatchInfo> patchClasses = new HashMap<>();

	public byte[] patchCode(byte[] inputCode, String inputClassName) {
		PatchInfo patchInfo = patchForClass(inputClassName);
		if (patchInfo == null) {
			return inputCode;
		}

		ClassReader classReader = new ClassReader(inputCode);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

		classNode.access = classNode.access & ~Opcodes.ACC_FINAL;
		classNode.access = JVMUtil.makeAccess(classNode.access, true);
		if (patchInfo.exposeInners) {
			for (InnerClassNode innerClassNode : classNode.innerClasses) {
				innerClassNode.access = JVMUtil.makeAccess(innerClassNode.access, true);
			}
		}
		for (FieldNode fieldNode : classNode.fields) {
			fieldNode.access = fieldNode.access & ~Opcodes.ACC_FINAL;
			fieldNode.access = JVMUtil.makeAccess(fieldNode.access, patchInfo.makePublic);
		}
		for (MethodNode methodNode : classNode.methods) {
			methodNode.access = methodNode.access & ~Opcodes.ACC_FINAL;
			methodNode.access = JVMUtil.makeAccess(methodNode.access, methodNode.name.equals("<init>") || patchInfo.makePublic);
		}
		for (FieldInfo fieldInfo : patchInfo.fields) {
			//classNode.fields.add(new FieldNode(JVMUtil.makeAccess(fieldInfo.accessAsInt() & ~Opcodes.ACC_FINAL, patchInfo.makePublic), fieldInfo.real, fieldInfo.type.descriptor(), fieldInfo.type.signature(), null));
		}
		for (MethodInfo methodInfo : patchInfo.methods) {
			//classNode.methods.add(new MethodNode(JVMUtil.makeAccess(methodInfo.accessAsInt() & ~Opcodes.ACC_FINAL, patchInfo.makePublic), methodInfo.real, methodInfo.descriptor(), methodInfo.signature(), null));
		}
		ClassWriter classWriter = new ClassWriter(classReader, 0);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

	public String patchSource(String inputSource, String inputClassName) {
		PatchInfo patchInfo = patchForClass(inputClassName);
		if (patchInfo == null) {
			return inputSource;
		}

		CompilationUnit cu;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(inputSource.getBytes(Charset.forName("UTF-8"))), "UTF-8");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		for (TypeDeclaration typeDeclaration : cu.getTypes()) {
			System.out.println(typeDeclaration.toString());
		}

		return inputSource;
	}

	private static PatchInfo getOrMakePatchInfo(String className, String shortClassName) {
		PatchInfo patchInfo = patchClasses.get(className);
		if (patchInfo == null) {
			patchInfo = new PatchInfo();
			patchClasses.put(className, patchInfo);
		}
		patchInfo.shortClassName = shortClassName;
		return patchInfo;
	}

	private static PatchInfo patchForClass(String className) {
		return patchClasses.get(className.replace("/", ".").replace(".java", "").replace(".class", ""));
	}

	private static String readFile(File file) {
		Scanner fileReader = null;
		try {
			fileReader = new Scanner(file, "UTF-8").useDelimiter("\\A");
			return fileReader.next().replace("\r\n", "\n");
		} catch (FileNotFoundException ignored) {
		} finally {
			if (fileReader != null) {
				fileReader.close();
			}
		}
		return null;
	}

	private static class PatchInfo {
		public boolean exposeInners = false;
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		List<FieldInfo> fields = new ArrayList<FieldInfo>();
		boolean makePublic = false;
		String shortClassName;
	}
}
