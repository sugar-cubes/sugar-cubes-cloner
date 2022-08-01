import me.champeau.jmh.JMHTask

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

    api("de.ruedigermoeller:fst:2.56")
    api("com.esotericsoftware:kryo:5.3.0")
    api("io.github.kostaskougios:cloning:1.10.3")
    api("net.bytebuddy:byte-buddy:1.12.12")

    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.springframework:spring-core:5.3.21")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    jmh("io.github.kostaskougios:cloning:1.10.3")

    testImplementation("io.github.kostaskougios:cloning:1.10.3")
    testImplementation("net.bytebuddy:byte-buddy:1.12.12")

}

// configurations.jmh.get().extendsFrom(configurations.testCompileClasspath.get())

checkstyle {
    toolVersion = "8.14"
    configFile = file("checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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
