import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java-conventions")
    id("java-library")
    id("java-gradle-plugin")
    id("checkstyle-conventions")
    id("maven-publish")
    id("signing")
}

description = "Build tools of the cloner: copy constructors by an annotation processor, a Maven or a Gradle plugin"

val asmVersion = "9.11.1"

dependencies {
    // the multi-release jar: the classes variant of the aggregating project is empty
    implementation(project(":sugar-cubes-cloner")) {
        attributes {
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        }
    }
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    compileOnly("org.apache.maven:maven-plugin-api:3.9.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.apache.maven:maven-plugin-api:3.9.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    // the processor runs in javac 8+, the plugins in Maven and Gradle on JDK 8+; --release 8 would hide the
    // javac API (com.sun.source), so the sources must not use newer APIs
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.compilerArgs.add("-Xlint:-options")
}

tasks.named<ProcessResources>("processResources") {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("META-INF/maven/plugin.xml") {
        filter<ReplaceTokens>("tokens" to mapOf("version" to version))
    }
}

gradlePlugin {
    // the plugin is published within the jar of this module, without a plugin marker
    isAutomatedPublishing = false
    plugins {
        create("cloner") {
            id = "io.github.sugar-cubes.cloner"
            implementationClass = "io.github.sugarcubes.cloner.tools.gradle.ClonerBuildPlugin"
        }
    }
}

tasks.named<Test>("test") {
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
            "Automatic-Module-Name" to "io.github.sugarcubes.cloner.tools",
        )
    )
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    repositories {
        maven {
            name = "ProjectLocal"
            url = uri("file:${rootDir}/repo")
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sugar-cubes/sugar-cubes-cloner")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
        maven {
            name = "Sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: project.findProperty("ossrhUsername") as String?
                password = System.getenv("OSSRH_PASSWORD") ?: project.findProperty("ossrhPassword") as String?
            }
        }
    }
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
            pom {
                name.set("Java deep cloning library: build tools")
                description.set(project.description)
                url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("mbutov")
                        name.set("Maxim Butov")
                        email.set("mbutov(at)gmail.com")
                        url.set("https://github.com/mbutov")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sugar-cubes/sugar-cubes-cloner.git")
                    developerConnection.set("scm:git:ssh://github.com/sugar-cubes/sugar-cubes-cloner.git")
                    url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["library"])
}
