package me.nallar.javatransformer.api;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.val;
import me.nallar.javatransformer.internal.editor.asm.ByteCodeInfo;
import me.nallar.javatransformer.internal.editor.javaparser.SourceInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.*;
import java.util.zip.*;

public class JavaTransformer {
	private final List<Transformer> transformers = new ArrayList<>();
	private final SimpleMultiMap<String, Transformer> classTransformers = new SimpleMultiMap<>();

	private static byte[] readFully(InputStream is) {
		byte[] output = {};
		int pos = 0;
		while (true) {
			int bytesToRead;
			if (pos >= output.length) {
				bytesToRead = output.length + 4096;
				if (output.length < pos + bytesToRead) {
					output = Arrays.copyOf(output, pos + bytesToRead);
				}
			} else {
				bytesToRead = output.length - pos;
			}
			int cc = 0;
			try {
				cc = is.read(output, pos, bytesToRead);
			} catch (IOException e) {
				throw new IOError(e);
			}
			if (cc < 0) {
				if (output.length != pos) {
					output = Arrays.copyOf(output, pos);
				}
				break;
			}
			pos += cc;
		}
		return output;
	}

	public void addTransformer(String s, Transformer t) {
		if (!t.shouldTransform(s)) {
			throw new IllegalArgumentException("Transformer " + t + " must transform class of name " + s);
		}
		if (classTransformers.get(s).contains(t)) {
			throw new IllegalArgumentException("Transformer " + t + " has already been added for class " + s);
		}
		classTransformers.put(s, t);
	}

	public void addTransformer(Transformer t) {
		if (transformers.contains(t)) {
			throw new IllegalArgumentException("Transformer " + t + " has already been added");
		}
		transformers.add(t);
	}

	public byte[] transformJava(byte[] data, String name) {
		if (!shouldTransform(name))
			return data;

		CompilationUnit cu;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(data));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		String packageName = cu.getPackage().getName().getName();
		for (TypeDeclaration typeDeclaration : cu.getTypes()) {
			String shortClassName = typeDeclaration.getName();
			if ((packageName + '.' + shortClassName).equalsIgnoreCase(name)) {
				transformJar(new SourceInfo(typeDeclaration, cu.getPackage(), cu.getImports()));
			}
		}

		return cu.toString().getBytes(Charset.forName("UTF-8"));
	}

	public byte[] transformClass(byte[] data, String name) {
		if (!shouldTransform(name))
			return data;

		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(data);
		reader.accept(node, ClassReader.EXPAND_FRAMES);

		transformJar(new ByteCodeInfo(node));

		ClassWriter classWriter = new ClassWriter(reader, 0);
		node.accept(classWriter);
		return classWriter.toByteArray();
	}

	public void transformJar(File input, File output) {
		try {
			transformJar(new ZipInputStream(new BufferedInputStream(new FileInputStream(input))), new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output))));
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		}
	}

	public void parseJar(File input) {
		try {
			transformJar(new ZipInputStream(new BufferedInputStream(new FileInputStream(input))), null);
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		}
	}

	public void parseFolder(Path input) {
		try {
			Files.walkFileTree(input, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Supplier<byte[]> data = transformBytes(input.relativize(file).toString(), () -> {
						try {
							return Files.readAllBytes(file);
						} catch (IOException e) {
							throw new IOError(e);
						}
					});

					// TODO implement transformFolder -> use data

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	private void transformJar(ClassInfo editor) {
		val name = editor.getName();
		transformers.forEach((x) -> {
			if (x.shouldTransform(name)) {
				x.transform(editor);
			}
		});
		classTransformers.get(name).forEach((it) -> it.transform(editor));
	}

	private boolean shouldTransform(String className) {
		if (!classTransformers.get(className).isEmpty())
			return true;

		for (Transformer transformer : transformers) {
			if (transformer.shouldTransform(className))
				return true;
		}

		return false;
	}

	private void transformJar(ZipInputStream is, ZipOutputStream os) {
		ZipEntry entry;
		try {
			while ((entry = is.getNextEntry()) != null) {
				Supplier<byte[]> data = transformBytes(entry.getName(), () -> readFully(is));

				if (os != null) {
					os.putNextEntry(new ZipEntry(entry));
					os.write(data.get());
				}
			}
		} catch (IOException e) {
			throw new IOError(e);
		} finally {
			try {
				is.close();
			} catch (IOException ignored) {
			}
			try {
				if (os != null)
					os.close();
			} catch (IOException ignored) {
			}
		}
	}

	private Supplier<byte[]> transformBytes(String relativeName, Supplier<byte[]> dataSupplier) {
		if (relativeName.endsWith(".java")) {
			String clazzName = relativeName.substring(0, relativeName.length() - 5).replace('/', '.');
			val bytes = transformJava(dataSupplier.get(), clazzName);
			return () -> bytes;
		} else if (relativeName.endsWith(".class")) {
			String clazzName = relativeName.substring(0, relativeName.length() - 6).replace('/', '.');
			val bytes = transformClass(dataSupplier.get(), clazzName);
			return () -> bytes;
		}
		return dataSupplier;
	}

	private static class SimpleMultiMap<K, T> {
		private final Map<K, List<T>> map = new HashMap<>();

		public void put(K key, T value) {
			List<T> values = map.get(key);
			if (values == null) {
				values = new ArrayList<>();
				map.put(key, values);
			}
			values.add(value);
		}

		public List<T> get(K key) {
			List<T> values = map.get(key);
			return values == null ? Collections.emptyList() : values;
		}
	}
}
