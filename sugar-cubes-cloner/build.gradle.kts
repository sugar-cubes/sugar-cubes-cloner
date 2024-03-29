import java.net.URI

plugins {
    id("java-conventions")
    id("java-library")
    id("maven-publish")
    id("signing")
}

val clonerModules = listOf(
    project(":jdk8"),
    project(":jdk9"),
    project(":jdk9-module"),
    project(":jdk15"),
    project(":jdk16"),
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
        from(project(":jdk9").sourceSets.main.get().output)
        from(project(":jdk9-module").sourceSets.main.get().output) {
            // exclude placeholder
            exclude("**/$$$.*")
        }
    }

    into("META-INF/versions/15") {
        from(project(":jdk15").sourceSets.main.get().output)
    }

    into("META-INF/versions/16") {
        from(project(":jdk16").sourceSets.main.get().output)
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
        source(module.sourceSets.main.get().allSource)
        modulePaths.addAll(module.configurations.compileClasspath.get())
    }
    modulePaths.removeIf { file ->
        file.path.startsWith(project.rootDir.path)
    }
    opts.modulePath(modulePaths.toCollection(mutableListOf()))

//    opts.addBooleanOption("J--add-exports=java.base/jdk.internal.misc=io.github.sugarcubes.cloner", true)

    // exclude placeholder
    exclude("**/$$$.*")

    exclude("**/JdkInternalMiscUnsafeBridge.*")
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
    repositories {
        maven {
            name = "ProjectLocal"
            url = URI("file:${rootDir}/repo")
        }
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/sugar-cubes/sugar-cubes-cloner")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
        maven {
            name = "Sonatype"
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: project.findProperty("ossrhUsername") as String?
                password = System.getenv("OSSRH_PASSWORD") ?: project.findProperty("ossrhPassword") as String?
            }
        }
    }
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
            pom {
                name.set("Java deep cloning library")
                description.set("Deep cloning of any objects")
                url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
                properties.set(
                    mapOf(
                    )
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("mbutov")
                        name.set("Maxim Butov")
                        email.set("mbutov(at)gmail.com")
                        url.set("https://github.com/mbutov")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sugar-cubes/sugar-cubes-cloner.git")
                    developerConnection.set("scm:git:ssh://github.com/sugar-cubes/sugar-cubes-cloner.git")
                    url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
                    issueManagement {
                        url.set("https://github.com/sugar-cubes/sugar-cubes-cloner/issues")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["library"])
}
