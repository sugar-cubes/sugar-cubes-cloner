plugins {
    id("java-conventions")
    id("optional-dependencies")
    id("checkstyle-conventions")
    id("build-utils")
}

dependencies {

    implementation(project(":jdk8"))
    implementation(project(":jdk9"))
    implementation(project(":jdk15"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    testImplementation("org.jeasy:easy-random:5.0.0")
    testImplementation("org.jeasy:easy-random-core:5.0.0")


}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

// cloning must work without any --add-opens option
val noOpensCheck by tasks.registering(JavaExec::class) {
    description = "Clones common JDK types and own classes on JDK 17 without --add-opens."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.sugarcubes.cloner.check.NoOpensCheck")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
}

tasks.named("check") {
    dependsOn(noOpensCheck)
}
