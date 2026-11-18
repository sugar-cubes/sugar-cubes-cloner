plugins {
    id("java-conventions")
    id("optional-dependencies")
    id("checkstyle-conventions")
    id("java-test-fixtures")
}

dependencies {

    testFixturesImplementation("org.hamcrest:hamcrest:2.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes(
        mapOf(
            "Premain-Class" to "io.github.sugarcubes.cloner.reflection.ClonerAgent",
            "Agent-Class" to "io.github.sugarcubes.cloner.reflection.ClonerAgent",
        )
    )
}
