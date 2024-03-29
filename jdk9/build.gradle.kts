plugins {
    id("java-conventions")
    id("optional-dependencies")
    id("checkstyle-conventions")
    id("build-utils")
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {

    compileOnly("org.objenesis:objenesis:3.3")
    implementation(project(":jdk8"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    jmh("com.esotericsoftware:kryo:5.5.0")
    jmh("io.github.kostaskougios:cloning:1.10.3")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(9))
    }
}

jmh {
    jvmArgs.addAll(utils.modulesJvmArgs("--add-opens"))
}
