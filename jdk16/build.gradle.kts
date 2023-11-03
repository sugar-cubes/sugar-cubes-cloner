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
