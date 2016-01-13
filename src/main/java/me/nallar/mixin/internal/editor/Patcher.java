package me.nallar.mixin.internal.editor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import me.nallar.mixin.internal.description.*;
import me.nallar.mixin.internal.editor.asm.ByteCodeEditor;
import me.nallar.mixin.internal.util.JVMUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class Patcher {
	private final Map<String, PatchInfo> patchClasses = new HashMap<>();

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

		patch(new ByteCodeEditor(node), name, patchInfo);

		ClassWriter classWriter = new ClassWriter(reader, 0);
		node.accept(classWriter);
		return classWriter.toByteArray();
	}

	public void patch(ClassEditor editor, String name, PatchInfo patchInfo) {
		editor.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(true));

		// TODO patchInfo.exposeInners - add methods for inner classes
		editor.getFields().forEach(d -> modifyDeclarations(d, patchInfo));
		editor.getMethods().forEach(d -> modifyDeclarations(d, patchInfo));

		patchInfo.fields.forEach(editor::add);

		patchInfo.methods.forEach(editor::add);
	}

	private static void modifyDeclarations(DeclarationInfo declarationInfo, PatchInfo patchInfo) {
		declarationInfo.accessFlags((f) -> f.without(AccessFlags.ACC_FINAL).makeAccessible(patchInfo.makePublic));
	}

	public String patchSource(String inputSource, String inputClassName) {
		PatchInfo patchInfo = getPatchInfo(inputClassName);
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
