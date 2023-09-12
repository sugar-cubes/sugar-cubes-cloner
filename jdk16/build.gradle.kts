plugins {
    id("java-conventions")
    id("checkstyle-conventions")
    id("build-utils")
}

dependencies {

    implementation(project(":jdk8"))
    implementation(project(":jdk9"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}
