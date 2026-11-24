plugins {
    id("java-conventions")
    id("java-library")
    id("publishing-conventions")
}

val clonerModules = listOf(
    project(":jdk8"),
    project(":jdk9"),
    project(":jdk9-module"),
    project(":jdk15"),
    project(":jdk16"),
    project(":jdk24"),
)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

val jarTasks = listOf(
    tasks.named<Jar>("jar"),
    tasks.named<Jar>("sourcesJar"),
)

jarTasks.forEach {
    it.configure {

        manifest.attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Build" to project.extra["gitHash"],
                "Multi-Release" to true,
                "Automatic-Module-Name" to "io.github.sugarcubes.cloner",
                "Created-By" to
                        System.getProperty("java.version") + " " + System.getProperty("java.specification.vendor"),
            )
        )

        clonerModules.forEach {
            manifest {
                attributes(it.tasks.jar.get().manifest.attributes)
            }
        }

    }
}

tasks.named<Jar>("jar") {

    from(project(":jdk8").sourceSets.main.get().output)

    into("META-INF/versions/9") {
        from(project(":jdk9-module").sourceSets.main.get().output) {
            include("module-info.class")
        }
        from(project(":jdk9").sourceSets.main.get().output)
    }

    into("META-INF/versions/15") {
        from(project(":jdk15").sourceSets.main.get().output)
    }

    into("META-INF/versions/16") {
        from(project(":jdk16").sourceSets.main.get().output)
    }

    into("META-INF/versions/24") {
        from(project(":jdk24").sourceSets.main.get().output)
    }

    clonerModules.forEach {
        dependsOn(it.tasks.named<Jar>("jar"))
        manifest {
            attributes(it.tasks.jar.get().manifest.attributes)
        }
    }

}

tasks.withType<Javadoc> {
    javadocTool.set(
        javaToolchains.javadocToolFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    )
    enabled = true
    val opts = options as StandardJavadocDocletOptions
    opts.links(
        "https://docs.oracle.com/en/java/javase/17/docs/api/",
    )

    val modulePaths = mutableSetOf<File>()
    clonerModules.forEach { module ->
        source(module.sourceSets.main.get().allJava)
        modulePaths.addAll(module.configurations.compileClasspath.get())
    }
    modulePaths.removeIf { file ->
        file.path.startsWith(project.rootDir.path)
    }
    opts.modulePath(modulePaths.toCollection(mutableListOf()))

//    opts.addBooleanOption("J--add-exports=java.base/jdk.internal.misc=io.github.sugarcubes.cloner", true)

    // exclude placeholder
    exclude("**/$$$.*")

    exclude("**/JdkInternalMiscUnsafeBridge*")
    // only exported packages are documented, the others are only read to resolve references
    opts.addPathOption("sourcepath").value = clonerModules.flatMap { it.sourceSets.main.get().java.srcDirs }
    exclude("io/github/sugarcubes/cloner/agent/**")
    exclude("io/github/sugarcubes/cloner/allocation/**")
    exclude("io/github/sugarcubes/cloner/engine/**")
    exclude("io/github/sugarcubes/cloner/fields/**")
    exclude("io/github/sugarcubes/cloner/generation/**")
    exclude("io/github/sugarcubes/cloner/platform/**")
    exclude("io/github/sugarcubes/cloner/reflection/**")
    exclude("io/github/sugarcubes/cloner/unsafe/**")
    exclude("io/github/sugarcubes/cloner/util/**")
}

tasks.named<Jar>("sourcesJar") {
    clonerModules.forEach {
        from(it.sourceSets.main.get().allSource)
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // exclude placeholder
    exclude("**/$$$.*")
}

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
            pom {
                name.set("Java deep cloning library")
                description.set("Deep cloning of any objects")
            }
        }
    }
}
