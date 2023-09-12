plugins {
    id("java-conventions")
}

dependencies {
    compileOnly("org.objenesis:objenesis:3.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(9))
    }
}
