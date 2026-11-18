import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // Gradle 9 does not provide the launcher, the version comes from the JUnit BOM of junit-jupiter
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "utf-8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

tasks.withType<Javadoc> {
    enabled = false
}
