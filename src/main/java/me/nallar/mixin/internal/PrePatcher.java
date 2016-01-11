package me.nallar.mixin.internal;

import me.nallar.mixin.internal.description.FieldInfo;
import me.nallar.mixin.internal.description.MethodInfo;
import me.nallar.mixin.internal.util.Splitter;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

// The prepatcher adds method declarations in superclasses,
// so javac can compile the patch classes if they need to use a method/field they
// add on an instance other than this
class PrePatcher {
	private static final Logger log = Logger.getLogger("PatchLogger");
	private static final Pattern privatePattern = Pattern.compile("^(\\s+?)private", Pattern.MULTILINE);
	private static final Pattern extendsPattern = Pattern.compile("^public.*?\\s+?extends\\s+?([\\S^<]+?)(?:<(\\S+)>)?[\\s]+?(?:implements [^}]+?)?\\{", Pattern.MULTILINE);
	private static final Pattern declareMethodPattern = Pattern.compile("@Declare\\s+?(public\\s+?(?:(?:synchronized|static) )*(\\S*?)?\\s+?(\\S*?)\\s*?\\S+?\\s*?\\([^\\{]*\\)\\s*?\\{)", Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern declareFieldPattern = Pattern.compile("@Declare\\s+?(public [^;\r\n]+?)_?( = [^;\r\n]+?)?;", Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern packageFieldPattern = Pattern.compile("\n    ? ?([^ ]+  ? ?[^ ]+);");
	private static final Pattern innerClassPattern = Pattern.compile("[^\n]public (?:static )?class ([^ \n]+)[ \n]", Pattern.MULTILINE);
	private static final Pattern importPattern = Pattern.compile("\nimport ([^;]+?);", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern exposeInnerPattern = Pattern.compile("\n@ExposeInner\\(\"([^\"]+)\"\\)", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Splitter spaceSplitter = Splitter.on(' ').omitEmptyStrings();
	private static final Splitter commaSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
	private static final Map<String, PatchInfo> patchClasses = new HashMap<String, PatchInfo>();
	//private static final Pattern methodInfoPattern = Pattern.compile("(?:(public|private|protected) )?(static )?(?:([^ ]+?) )([^\\( ]+?) ?\\((.*?)\\) ?\\{", Pattern.DOTALL);
	private static final Pattern methodInfoPattern = Pattern.compile("^(.+) ?\\(([^\\(]*)\\) ?\\{", Pattern.DOTALL);
	private static final HashMap<String, String> classExtends = new HashMap<String, String>();

	public static void loadPatches(File patchDirectory) {
		recursiveSearch(patchDirectory);
	}

	private static void recursiveSearch(File patchDirectory) {
		for (File file : patchDirectory.listFiles()) {
			if (!file.getName().equals("annotation") && file.isDirectory()) {
				recursiveSearch(file);
				continue;
			}
			if (!file.getName().endsWith(".java")) {
				continue;
			}
			addPatches(file);
		}
	}

	// TODO - clean up this method. It works, but it's hardly pretty...
	private static void addPatches(File file) {
		String contents = readFile(file);
		if (contents == null) {
			log.log(Level.SEVERE, "Failed to read " + file);
			return;
		}
		Matcher extendsMatcher = extendsPattern.matcher(contents);
		if (!extendsMatcher.find()) {
			if (contents.contains(" extends")) {
				log.warning("Didn't match extends matcher for " + file);
			}
			return;
		}
		String shortClassName = extendsMatcher.group(1);
		String className = null;
		Matcher importMatcher = importPattern.matcher(contents);
		List<String> imports = new ArrayList<String>();
		while (importMatcher.find()) {
			imports.add(importMatcher.group(1));
		}
		for (String import_ : imports) {
			if (import_.endsWith('.' + shortClassName)) {
				className = import_;
			}
		}
		if (className == null) {
			log.warning("Unable to find class " + shortClassName + " for " + file);
			return;
		}
		PatchInfo patchInfo = getOrMakePatchInfo(className, shortClassName);
		Matcher exposeInnerMatcher = exposeInnerPattern.matcher(contents);
		while (exposeInnerMatcher.find()) {
			log.severe("Inner class name: " + className + "$" + exposeInnerMatcher.group(1));
			getOrMakePatchInfo(className + "$" + exposeInnerMatcher.group(1), shortClassName + "$" + exposeInnerMatcher.group(1)).makePublic = true;
			patchInfo.exposeInners = true;
		}
		Matcher matcher = declareMethodPattern.matcher(contents);
		while (matcher.find()) {
			Matcher methodInfoMatcher = methodInfoPattern.matcher(matcher.group(1));

			if (!methodInfoMatcher.find()) {
				log.warning("Failed to match method info matcher to method declaration " + matcher.group(1));
				continue;
			}


			MethodInfo methodInfo = new MethodInfo();
			patchInfo.methods.add(methodInfo);

			String accessAndNameString = methodInfoMatcher.group(1).replace(", ", ","); // Workaround for multiple argument generics
			String paramString = methodInfoMatcher.group(2);

			for (String parameter : commaSplitter.split(paramString)) {
				Iterator<String> iterator = spaceSplitter.split(parameter).iterator();
				String parameterType = null;
				while (parameterType == null) {
					parameterType = iterator.next();
					if (parameterType.equals("final")) {
						parameterType = null;
					}
				}
				methodInfo.parameterTypes.add(new Type(parameterType, imports));
			}

			LinkedList<String> accessAndNames = Lists.newLinkedList(spaceSplitter.split(accessAndNameString));

			methodInfo.name = accessAndNames.removeLast();
			String rawType = accessAndNames.removeLast();

			while (!accessAndNames.isEmpty()) {
				String thing = accessAndNames.removeLast();
				if (thing.equals("static")) {
					methodInfo.static_ = true;
				} else if (thing.equals("synchronized")) {
					methodInfo.synchronized_ = true;
				} else if (thing.equals("final")) {
					methodInfo.final_ = true;
				} else if (thing.startsWith("<")) {
					methodInfo.genericType = thing;
				} else {
					if (methodInfo.access != null) {
						log.severe("overwriting method access from " + methodInfo.access + " -> " + thing + " in " + matcher.group(1));
					}
					methodInfo.access = thing;
				}
			}

			String ret = "null";
			if ("static".equals(rawType)) {
				rawType = matcher.group(3);
			}
			methodInfo.returnType = new Type(rawType, imports);
			if ("boolean".equals(rawType)) {
				ret = "false";
			} else if ("void".equals(rawType)) {
				ret = "";
			} else if ("long".equals(rawType)) {
				ret = "0L";
			} else if ("int".equals(rawType)) {
				ret = "0";
			} else if ("float".equals(rawType)) {
				ret = "0f";
			} else if ("double".equals(rawType)) {
				ret = "0.0";
			}
			methodInfo.javaCode = matcher.group(1) + "return " + ret + ";}";
		}
		Matcher fieldMatcher = declareFieldPattern.matcher(contents);
		while (fieldMatcher.find()) {
			String var = fieldMatcher.group(1).replace(", ", ","); // Workaround for multiple argument generics
			FieldInfo fieldInfo = new FieldInfo();
			patchInfo.fields.add(fieldInfo);
			LinkedList<String> typeAndName = Lists.newLinkedList(spaceSplitter.split(var));

			fieldInfo.name = typeAndName.removeLast();
			fieldInfo.type = new Type(typeAndName.removeLast(), imports);

			while (!typeAndName.isEmpty()) {
				String thing = typeAndName.removeLast();
				if (thing.equals("static")) {
					fieldInfo.static_ = true;
				} else if (thing.equals("volatile")) {
					fieldInfo.volatile_ = true;
				} else if (thing.equals("final")) {
					fieldInfo.final_ = true;
				} else {
					if (fieldInfo.access != null) {
						log.severe("overwriting field access from " + fieldInfo.access + " -> " + thing + " in " + var);
					}
					fieldInfo.access = thing;
				}
			}
			fieldInfo.javaCode = var + ';';
		}
		if (contents.contains("\n@Public")) {
			patchInfo.makePublic = true;
		}
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
}
