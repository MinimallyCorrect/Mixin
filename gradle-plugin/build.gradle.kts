plugins {
	id("groovy")
	id("java")
	id("java-library")
	id("maven-publish")
	id("java-gradle-plugin")
	id("com.github.johnrengelman.shadow")
}

apply(from = "groovy.gradle")

dependencies {
	testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
	testImplementation("org.spockframework:spock-core")
	testImplementation("org.spockframework:spock-junit4")
	testImplementation("junit:junit:4.13.2")

	val lombok = "org.projectlombok:lombok:1.18.34"
	compileOnly(lombok)
	testCompileOnly(lombok)
	annotationProcessor(lombok)
	testAnnotationProcessor(lombok)
}

tasks.withType<Test>().configureEach {
	// Using JUnitPlatform for running tests
	useJUnitPlatform()
}

configurations.all {
	resolutionStrategy {
		force("org.jetbrains:annotations:23.1.0")
		force("org.ow2.asm:asm:9.7.1")
	}
}
