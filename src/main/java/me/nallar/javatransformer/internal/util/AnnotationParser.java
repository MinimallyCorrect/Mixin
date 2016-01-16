package me.nallar.javatransformer.internal.util;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.*;
import lombok.val;
import me.nallar.javatransformer.api.Annotation;
import me.nallar.javatransformer.internal.description.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;
import java.util.stream.*;

public class AnnotationParser {
	public static List<Annotation> parseAnnotations(byte[] bytes) {
		ClassReader cr = new ClassReader(bytes);
		AnnotationVisitor cv = new AnnotationVisitor();
		cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

		return cv.annotations.stream().map(AnnotationParser::annotationFromAnnotationNode).collect(Collectors.toList());
	}

	public static Annotation annotationFromAnnotationNode(AnnotationNode annotationNode) {
		val values = new HashMap<String, Object>();
		for (int i = 0; i < annotationNode.values.size(); i += 2) {
			values.put((String) annotationNode.values.get(i), annotationNode.values.get(i + 1));
		}
		return Annotation.of(new Type(annotationNode.desc), values);
	}

	public static Annotation annotationFromAnnotationExpr(AnnotationExpr annotationExpr, Iterable<ImportDeclaration> imports) {
		Type t = Type.resolve(annotationExpr.getName().getName(), imports);
		if (annotationExpr instanceof SingleMemberAnnotationExpr) {
			return Annotation.of(t, expressionToValue(((SingleMemberAnnotationExpr) annotationExpr).getMemberValue()));
		} else if (annotationExpr instanceof NormalAnnotationExpr) {
			val map = new HashMap<String, Object>();
			for (MemberValuePair memberValuePair : ((NormalAnnotationExpr) annotationExpr).getPairs()) {
				map.put(memberValuePair.getName(), expressionToValue(memberValuePair.getValue()));
			}
			return Annotation.of(t, map);
		} else if (annotationExpr instanceof MarkerAnnotationExpr) {
			return Annotation.of(t);
		}
		throw new RuntimeException("Unknown annotation type: " + annotationExpr.getClass().getCanonicalName());
	}

	private static Object expressionToValue(Expression e) {
		if (e instanceof StringLiteralExpr) {
			return ((StringLiteralExpr) e).getValue();
		}
		throw new RuntimeException("Unknown value: " + e);
	}

	private static class AnnotationVisitor extends ClassVisitor {
		public final List<AnnotationNode> annotations = new ArrayList<>();

		public AnnotationVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public org.objectweb.asm.AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
			AnnotationNode an = new AnnotationNode(desc);
			annotations.add(an);
			return an;
		}
	}
}
