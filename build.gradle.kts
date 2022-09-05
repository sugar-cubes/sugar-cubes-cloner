import com.mycila.maven.plugin.license.*
import com.mycila.maven.plugin.license.header.HeaderDefinition
import nl.javadude.gradle.plugins.license.maven.*
import nl.javadude.gradle.plugins.license.maven.AbstractLicenseMojo
import nl.javadude.gradle.plugins.license.maven.LicenseFormatMojo
import java.lang.reflect.Method
import java.net.URI
import java.util.Collections

plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
    id("checkstyle")
}

buildscript {
    dependencies {
        classpath("gradle.plugin.com.hierynomus.gradle.plugins:license-gradle-plugin:0.15.0")
    }
}

group = "org.sugarcubes"
version = "1.0.0"
description = "Java Reflection Cloner library"

repositories {
    mavenCentral()
}

dependencies {

    api("org.objenesis:objenesis:3.2")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    testImplementation("org.apache.commons:commons-collections4:4.4")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.apache.commons:commons-math3:3.6.1")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.springframework:spring-core:5.3.22")

    jmh("com.esotericsoftware:kryo:5.3.0")
    jmh("io.github.kostaskougios:cloning:1.10.3")

}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
        attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
        attributes["Import-Package"] = "sun.misc;resolution:=optional,org.objenesis;resolution:=optional"
        attributes["Export-Package"] = "sun.misc;resolution:=optional,org.objenesis;resolution:=optional"
    }
}

checkstyle {
    toolVersion = "8.14"
    configFile = file("checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

tasks.named<Checkstyle>("checkstyleTest") {
    isIgnoreFailures = true
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    ignoreFailures = true
}

tasks.register<Copy>("exportJavadocs") {
    from(layout.buildDirectory.file("docs"))
    into(layout.projectDirectory.file("docs"))
}

class LicenseMojo(
    validHeaders: MutableCollection<File>?,
    rootDir: File?,
    initial: MutableMap<String, String>?,
    dryRun: Boolean,
    skipExistingHeaders: Boolean,
    useDefaultMappings: Boolean,
    strictCheck: Boolean,
    header: URI?,
    source: FileCollection?,
    mapping: MutableMap<String, String>?,
    encoding: String?,
    headerDefinitions: MutableList<HeaderDefinition>?
) : AbstractLicenseMojo(
    validHeaders,
    rootDir,
    initial,
    dryRun,
    skipExistingHeaders,
    useDefaultMappings,
    strictCheck,
    header,
    source,
    mapping,
    encoding,
    headerDefinitions
) {

    public override fun execute(callback: Callback?) {
        super.execute(callback)
    }

}

tasks.create("licenseFormat") {
    doLast {
        val callback: CallbackWithFailure = LicenseFormatMojo(project.rootDir, false, false)
        val license = LicenseMojo(
            Collections.emptySet(),
            project.rootDir,
            Collections.emptyMap(),
            false,
            false,
            true,
            true,
            file("HEADER.txt").toURI(),
            fileTree("src/main/java"),
            Collections.emptyMap(),
            "utf-8",
            Collections.emptyList()
        )
        license.execute(callback)
    }
}
