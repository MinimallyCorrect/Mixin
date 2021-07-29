package dev.minco.test

import io.github.classgraph.ClassGraph
import org.gradle.api.JavaVersion
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import static org.gradle.testkit.runner.TaskOutcome.*

class BuildLogicFunctionalTest extends Specification {

	@Rule
	TemporaryFolder testProjectDir = new TemporaryFolder()
	File mixinJavaDir

	def setup() {
		mixinJavaDir = testProjectDir.newFolder("mixins", "src", "main", "java")
		def libs = testProjectDir.newFolder("libs")
		def mixinJar = new ClassGraph().getClasspathFiles().find {
			it.canonicalPath.replace('\\', '/').contains("libs/mixin")
		}
		Files.copy(mixinJar.toPath(), new File(libs, "mixin.jar").toPath(), StandardCopyOption.REPLACE_EXISTING)
	}

	@Unroll
	def "single mixin application works (gradle version #gradleVersion)"() {
		given:
		new AntBuilder().copy( todir:testProjectDir.root.canonicalFile ) {
			fileset( dir:'test-template' )
		}
		new AntBuilder().copy( todir:mixinJavaDir ) {
			fileset( dir:'src/test/java' )
		}

		when:
		def result = GradleRunner.create()
		if (gradleVersion != null) {
			result = result.withGradleVersion(gradleVersion)
		}
		if (maxJdkVersion != null) {
			Assume.assumeTrue("max JDK version is " + maxJdkVersion, JavaVersion.current() <= maxJdkVersion)
		}
		result = result
			.withProjectDir(testProjectDir.root)
			.withArguments('dependencies', 'build', '--stacktrace', '--info')
			.withPluginClasspath()
			.build()

		then:
		result.task(":test").outcome == SUCCESS

		where:
		gradleVersion | maxJdkVersion
		null | null
		// JDK 16
		"7.1.1" | JavaVersion.VERSION_16
		// first version with JDK 15 support
		"6.7.1" | JavaVersion.VERSION_15
		// first version with working @Nested in a managed type
		"5.6" | JavaVersion.VERSION_11
	}
}
