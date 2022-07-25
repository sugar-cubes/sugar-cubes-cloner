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

    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.springframework:spring-core:5.3.21")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

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
    batchSize.set(2)
    fork.set(2)
    warmup.set("1s")
    warmupBatchSize.set(1)
    warmupForks.set(0)
    warmupIterations.set(1)
}
