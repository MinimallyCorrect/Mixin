package me.nallar.javatransformer;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public abstract class JarTransformer extends JavaTransformer {
	private static byte[] readFully(InputStream is)
		throws IOException {
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
			int cc = is.read(output, pos, bytesToRead);
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

	public void transform(File input, File output) {
		try {
			transform(new ZipInputStream(new BufferedInputStream(new FileInputStream(input))), new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output))));
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		}
	}

	public void transform(ZipInputStream is, ZipOutputStream os) {
		ZipEntry entry;
		try {
			while ((entry = is.getNextEntry()) != null) {
				byte[] data = readFully(is);

				String name = entry.getName();
				if (name.endsWith(".java")) {
					String clazzName = name.substring(0, name.length() - 5).replace('/', '.');
					data = transformJava(data, clazzName);
				} else if (name.endsWith(".class")) {
					String clazzName = name.substring(0, name.length() - 6).replace('/', '.');
					data = transformClass(data, clazzName);
				}

				os.putNextEntry(new ZipEntry(entry));
				os.write(data);
			}
		} catch (IOException e) {
			throw new IOError(e);
		} finally {
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
	}
}
