plugins {
    id("java-conventions")
    id("optional-dependencies")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(9))
    }
}
