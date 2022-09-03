plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
    id("checkstyle")
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
