plugins {
    id("build-logic.build-commons")
}

tasks.withType<JavaCompile> {
    targetCompatibility = JavaVersion.VERSION_1_9.toString()
    sourceCompatibility = JavaVersion.VERSION_1_9.toString()

    options.compilerArgs.addAll(utils.modulesJvmArgs("--add-exports"))
}

tasks.named<Jar>("jar") {
    manifest.attributes(
        mapOf(
            "Premain-Class" to "io.github.sugarcubes.cloner.ModuleOpeningAgent",
            "Agent-Class" to "io.github.sugarcubes.cloner.ModuleOpeningAgent",
        )
    )
}
