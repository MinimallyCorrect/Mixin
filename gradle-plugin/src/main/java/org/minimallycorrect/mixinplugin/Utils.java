package org.minimallycorrect.mixinplugin;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Utils {
	/**
	 * Wrapper for {@link TaskContainer#register(String, Class)} which falls back to create on older gradle versions
	 */
	public static <T extends Task> void registerTask(TaskContainer taskContainer, String name, Class<T> clazz) {
		try {
			taskContainer.register(name, clazz);
		} catch (NoSuchMethodError ignored) {
			taskContainer.create(name, clazz);
		}
	}

	@SneakyThrows
	public static void setPomRootVal(@NonNull File input, @NonNull File output, @NonNull String tag, @NonNull String value) {
		output.getParentFile().mkdirs();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		NodeList groups = doc.getDocumentElement().getChildNodes();
		boolean set = false;
		val length = groups.getLength();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				Node node = groups.item(i);
				if (node instanceof Element && tag.equals(((Element) node).getTagName())) {
					node.setTextContent(value);
					set = true;
					break;
				}
			}
		}

		if (!set) {
			Element elem = doc.createElement(tag);
			elem.setTextContent(value);
			doc.getDocumentElement().appendChild(elem);
		}

		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty("indent", "no");
		tf.setOutputProperty("method", "xml");
		DOMSource domSource = new DOMSource(doc);
		StreamResult sr = new StreamResult(output);
		tf.transform(domSource, sr);
	}

	public static String getId(Dependency dep) {
		return dep.getGroup() + ':' + dep.getName() + ':' + dep.getVersion();
	}
}
