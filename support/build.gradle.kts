plugins {
    id("java-conventions")
    id("optional-dependencies")
    id("checkstyle-conventions")
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
