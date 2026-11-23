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
    implementation(project(":jdk16"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.ow2.asm:asm:9.11.1")

}

java {
    toolchain {
        // the ClassFile API is final since JDK 24
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}
