import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java-conventions")
    id("java-library")
    id("checkstyle-conventions")
    id("publishing-conventions")
}

description = "Build tools of the cloner: copy constructors by an annotation processor, a Maven or a Gradle plugin"

// the processor runs in javac 8+, the Maven goals on JDK 8+: compiled by JDK 8, which checks the API
val jdk8 = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(8)) }

dependencies {
    // the multi-release jar: the classes variant of the aggregating project is empty
    implementation(project(":sugar-cubes-cloner")) {
        attributes {
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        }
    }
    implementation("org.ow2.asm:asm:9.11.1")
    implementation("org.ow2.asm:asm-commons:9.11.1")
    compileOnly("org.apache.maven:maven-plugin-api:3.9.6")
    // javac API (com.sun.source) of JDK 8
    compileOnly(files(jdk8.map { it.metadata.installationPath.file("lib/tools.jar") }))
    // API of Gradle 8, compiled for Java 8 unlike the API of Gradle 9; the plugin works in Gradle 8 and 9
    compileOnly("dev.gradleplugins:gradle-api:8.11.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.apache.maven:maven-plugin-api:3.9.6")
    testImplementation(gradleTestKit())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

// tests: records among the samples, Gradle TestKit of Gradle 9 needs JDK 17
val jdk17 = JavaLanguageVersion.of(17)

tasks.named<JavaCompile>("compileTestJava") {
    javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(jdk17) })
}

tasks.named<ProcessResources>("processResources") {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("META-INF/maven/plugin.xml") {
        filter<ReplaceTokens>("tokens" to mapOf("version" to version))
    }
}

tasks.named<Test>("test") {
    // JDK 17 unless -PtestJdk is given, see java-conventions
    if (findProperty("testJdk") == null) {
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(jdk17) })
    }
    val samples = file("src/test/java")
    val processorPath = files(tasks.named("jar"), configurations.runtimeClasspath)
    inputs.files(processorPath).withPropertyName("processorPath")
    systemProperty("samples.sources", samples.absolutePath)
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Dprocessor.classpath=${processorPath.asPath}")
    })
}

tasks.named<Jar>("jar") {
    manifest.attributes(
        mapOf(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Build" to project.extra["gitHash"],
            "Automatic-Module-Name" to "io.github.sugarcubes.cloner.tools",
        )
    )
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val gradlePluginId = "io.github.sugar-cubes.cloner"

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
            pom {
                name.set("Java deep cloning library: build tools")
                description.set(project.description)
            }
        }
        // the marker of the Gradle plugin: plugins { id("io.github.sugar-cubes.cloner") version "..." } finds the
        // plugin in Maven Central by it
        register<MavenPublication>("pluginMarker") {
            groupId = gradlePluginId
            artifactId = "$gradlePluginId.gradle.plugin"
            pom {
                packaging = "pom"
                name.set("Java deep cloning library: Gradle plugin marker")
                description.set("Marker of the Gradle plugin $gradlePluginId in ${project.name}")
                withXml {
                    val dependency = asNode().appendNode("dependencies").appendNode("dependency")
                    dependency.appendNode("groupId", project.group.toString())
                    dependency.appendNode("artifactId", project.name)
                    dependency.appendNode("version", project.version.toString())
                }
            }
        }
    }
}
