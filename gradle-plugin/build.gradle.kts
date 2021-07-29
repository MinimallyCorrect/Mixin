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
	testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
	testImplementation("org.spockframework:spock-core")
	testImplementation("org.spockframework:spock-junit4")
	testImplementation("junit:junit:4.13.2")

	val lombok = "org.projectlombok:lombok:1.18.20"
	compileOnly(lombok)
	testCompileOnly(lombok)
	annotationProcessor(lombok)
	testAnnotationProcessor(lombok)
}

tasks.withType<Test>().configureEach {
	// Using JUnitPlatform for running tests
	useJUnitPlatform()
}

