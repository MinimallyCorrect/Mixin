package me.nallar.mixin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.val;
import me.nallar.mixin.internal.description.AccessFlags;
import me.nallar.mixin.internal.description.DeclarationInfo;
import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.editor.ClassEditor;
import me.nallar.mixin.internal.editor.asm.ByteCodeEditor;
import me.nallar.mixin.internal.editor.javaparser.SourceEditor;
import me.nallar.mixin.internal.util.JVMUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class MixinPrePatcher {
	private final Map<String, PatchInfo> patchClasses = new HashMap<>();

	public void patch(ClassEditor editor, PatchInfo patchInfo) {
		editor.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(true));

		// TODO patchInfo.exposeInners support - add methods for inner classes
		editor.getFields().forEach(d -> modifyDeclarations(d, patchInfo));
		editor.getMethods().forEach(d -> modifyDeclarations(d, patchInfo));

		patchInfo.fields.forEach(editor::add);

		patchInfo.methods.forEach(editor::add);
	}

	public String patch(String source, String name) {
		PatchInfo patchInfo = getPatchInfo(name);
		if (patchInfo == null)
			return source;

		CompilationUnit cu;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(source.getBytes(Charset.forName("UTF-8"))), "UTF-8");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		String packageName = cu.getPackage().getName().getName();
		for (TypeDeclaration typeDeclaration : cu.getTypes()) {
			String shortClassName = typeDeclaration.getName();
			if ((packageName + '.' + shortClassName).equalsIgnoreCase(name)) {
				val editor = new SourceEditor(typeDeclaration, cu.getImports());
				patch(editor, patchInfo);
			}
		}

		return cu.toString();
	}

	public byte[] patch(byte[] bytes, String name) {
		PatchInfo patchInfo = getPatchInfo(name);
		if (patchInfo == null)
			return bytes;

		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, ClassReader.EXPAND_FRAMES);

		patch(new ByteCodeEditor(node), patchInfo);

		ClassWriter classWriter = new ClassWriter(reader, 0);
		node.accept(classWriter);
		return classWriter.toByteArray();
	}

	private static void modifyDeclarations(DeclarationInfo declarationInfo, PatchInfo patchInfo) {
		declarationInfo.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(patchInfo.makePublic));
	}

	private PatchInfo getOrMakePatchInfo(String className, String shortClassName) {
		className = JVMUtil.fileNameToClassName(className);
		PatchInfo patchInfo = patchClasses.get(className);
		if (patchInfo == null) {
			patchInfo = new PatchInfo();
			patchClasses.put(className, patchInfo);
		}
		patchInfo.shortClassName = shortClassName;
		return patchInfo;
	}

	private PatchInfo getPatchInfo(String className) {
		className = JVMUtil.fileNameToClassName(className);
		return patchClasses.get(className);
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
		List<MethodInfo> methods = new ArrayList<>();
		List<FieldInfo> fields = new ArrayList<>();
		boolean makePublic = false;
		String shortClassName;
	}
}
