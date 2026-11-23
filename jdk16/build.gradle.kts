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
    testImplementation("org.ow2.asm:asm:9.11.1")


}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

// cloning must work without any --add-opens option
val noOpensCheck = tasks.register<JavaExec>("noOpensCheck") {
    description = "Clones common JDK types and own classes on JDK 17 without --add-opens."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.sugarcubes.cloner.check.NoOpensCheck")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
}

tasks.named("check") {
    dependsOn(noOpensCheck)
}

// module opening and field access mechanisms, tested on JDK 17 without any --add-opens option
val moduleOpeningTest = sourceSets.create("moduleOpeningTest") {
    val main = sourceSets.main.get()
    // own classes first: they replace the version-specific aliases of the dependencies
    compileClasspath = main.output + configurations[compileClasspathConfigurationName]
    runtimeClasspath = output + main.output + configurations[runtimeClasspathConfigurationName]
}

configurations[moduleOpeningTest.implementationConfigurationName].extendsFrom(configurations.implementation.get())

// the cloner jar with the agent manifest
val agent = configurations.create("agent") {
    isTransitive = false
}

dependencies {
    agent(project(":jdk8"))
    "moduleOpeningTestImplementation"("org.junit.jupiter:junit-jupiter:5.10.0")
    "moduleOpeningTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    "moduleOpeningTestImplementation"("io.github.toolfactory:narcissus:1.0.14")
}

fun Test.moduleOpeningTest() {
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = moduleOpeningTest.output.classesDirs
    classpath = moduleOpeningTest.runtimeClasspath
    // JDK 17 unless -PtestJdk is given, see java-conventions
    if (findProperty("testJdk") == null) {
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    }
    // build-utils adds --add-opens to every test task
    jvmArgs = listOf()
    // module opening is global: a fresh JVM for every test class
    setForkEvery(1)
}

val noOpensTest = tasks.register<Test>("noOpensTest") {
    description = "Tests module opening mechanisms on JDK 17 without --add-opens."
    moduleOpeningTest()
    filter.excludeTestsMatching("*.AgentTest")
}

val agentTest = tasks.register<Test>("agentTest") {
    description = "Tests module opening by the cloner agent on JDK 17 without --add-opens."
    moduleOpeningTest()
    filter.includeTestsMatching("*.AgentTest")
    inputs.files(agent)
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-javaagent:${agent.singleFile}") })
}

tasks.named("check") {
    dependsOn(noOpensTest, agentTest)
}
