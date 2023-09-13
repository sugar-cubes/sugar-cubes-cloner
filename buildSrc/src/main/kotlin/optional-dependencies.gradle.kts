plugins {
    id("java")
}

dependencies {
    compileOnly("org.objenesis:objenesis:3.3")

    compileOnly("io.github.toolfactory:narcissus:1.0.7")
    compileOnly("io.github.toolfactory:jvm-driver:9.4.3")

    compileOnly("org.burningwave:core:12.63.0")
    compileOnly("org.burningwave:jvm-driver:8.14.1")
    compileOnly("org.burningwave:reflection:3.5.5")
}
