import java.net.URI
import java.util.stream.Collectors

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

val projects = listOf(
    project(":jdk8"),
    project(":jdk9"),
    project(":jdk9-module"),
    project(":jdk16"),
)

dependencies {
    api("org.objenesis:objenesis:3.3")
    projects.forEach {
        testCompileOnly(it)
    }
}

afterEvaluate {
    configurations {
        setOf(apiElements, runtimeElements).forEach {
            it.configure {
                attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "utf-8"
}

tasks.withType<Jar> {

    manifest {
        mapOf(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to "io.github.sugarcubes.cloner",
            "Created-By" to
                    System.getProperty("java.version") + " " + System.getProperty("java.specification.vendor"),
            "Import-Package" to
                    listOf(
                        "jdk.internal.misc",
                        "org.objenesis",
                        "sun.misc",
                    )
                        .stream()
                        .map { "${it};resolution:=optional" }
                        .collect(Collectors.joining(",")),
            "Export-Package" to "io.github.sugarcubes.cloner",
        )
            .forEach { key, value ->
                attributes[key] = value
            }
    }

}

tasks.named<Jar>("jar") {

    projects.forEach {
        from(it.sourceSets.main.get().output)
    }

}

tasks.withType<Javadoc> {
    val opts = options as StandardJavadocDocletOptions
    opts.links("https://docs.oracle.com/en/java/javase/11/docs/api/")

    projects.forEach {
        source(it.sourceSets.main.get().allSource)
    }

    // exclude placeholder
    exclude("**/_*.*")

    exclude("**/JdkInternalMiscUnsafeBridge.*")
}

tasks.named<Jar>("sourcesJar") {
    projects.forEach {
        from(it.sourceSets.main.get().allSource)
    }

    // exclude placeholder
    exclude("**/_*.*")
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
