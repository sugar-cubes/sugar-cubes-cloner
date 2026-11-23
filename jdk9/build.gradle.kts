plugins {
    id("java-conventions")
    id("optional-dependencies")
    id("checkstyle-conventions")
    id("build-utils")
    id("me.champeau.jmh") version "0.7.4"
}

dependencies {

    implementation(project(":jdk8"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    jmh("com.esotericsoftware:kryo:5.5.0")
    jmh("io.github.kostaskougios:cloning:1.10.3")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(9))
    }
}

jmh {
    jvmArgs.addAll(utils.modulesJvmArgs("--add-opens"))
}

// identity hash codes of all objects are equal: lookups must compare the objects, not only the hash codes
val hashCollisionTest = tasks.register<Test>("hashCollisionTest") {
    description = "Runs IdentityHashCollisionTest with equal identity hash codes of all objects (HotSpot only)."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter.includeTestsMatching("*.IdentityHashCollisionTest")
    jvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:hashCode=2")
    systemProperty("cloner.test.hashCollisions", "true")
}

tasks.named("check") {
    dependsOn(hashCollisionTest)
}
