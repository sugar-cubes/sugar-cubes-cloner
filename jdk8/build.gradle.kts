plugins {
    id("java-conventions")
    id("checkstyle-conventions")
    id("java-test-fixtures")
}

dependencies {

    compileOnly("org.objenesis:objenesis:3.3")

    testFixturesImplementation("org.objenesis:objenesis:3.3")
    testFixturesImplementation("org.hamcrest:hamcrest:2.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    testImplementation("org.objenesis:objenesis:3.3")
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
            "Premain-Class" to "io.github.sugarcubes.cloner.ClonerAgent",
            "Agent-Class" to "io.github.sugarcubes.cloner.ClonerAgent",
        )
    )
}
