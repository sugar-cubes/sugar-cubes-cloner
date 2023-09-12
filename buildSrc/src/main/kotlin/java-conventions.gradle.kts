import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
}

repositories {
    mavenCentral()
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
