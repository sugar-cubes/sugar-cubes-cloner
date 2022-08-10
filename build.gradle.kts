plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
    id("checkstyle")
}

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
    testImplementation("com.esotericsoftware:kryo:5.3.0")
    testImplementation("io.github.kostaskougios:cloning:1.10.3")

}

// configurations.jmh.get().extendsFrom(configurations.testCompileClasspath.get())

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

jmh {
    iterations.set(2)
    batchSize.set(4)
    fork.set(4)

    warmup.set("1s")
    warmupBatchSize.set(1)
    warmupForks.set(0)
    warmupIterations.set(1)
}

tasks.register<Copy>("exportJavadocs") {
    from(layout.buildDirectory.file("docs"))
    into(layout.projectDirectory.file("docs"))
}
