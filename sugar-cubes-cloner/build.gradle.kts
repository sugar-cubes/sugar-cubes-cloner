import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

dependencies {
    api("org.objenesis:objenesis:3.3")
    testCompileOnly(project(":jdk8"))
    testCompileOnly(project(":jdk9"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_9.toString()
    targetCompatibility = JavaVersion.VERSION_1_9.toString()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

tasks.withType<Jar> {

    from(project(":jdk8").sourceSets.main.get().output)
    from(project(":jdk9").sourceSets.main.get().output)
    from(project(":sugar-cubes-cloner").sourceSets.main.get().output)

    exclude("org/sugarcubes/cloner/Placeholder.class")

    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
        attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
        attributes["Import-Package"] = "sun.misc;resolution:=optional,org.objenesis;resolution:=optional"
        attributes["Export-Package"] = "org.sugarcubes.cloner"
    }

}

tasks.withType<Javadoc> {
    val opts = options as StandardJavadocDocletOptions
    opts.links("https://docs.oracle.com/en/java/javase/11/docs/api/")
    source(project(":jdk8").sourceSets.main.get().allSource)
    source(project(":jdk9").sourceSets.main.get().allSource)
    source(project(":sugar-cubes-cloner").sourceSets.main.get().allSource)
    exclude("org/sugarcubes/cloner/Placeholder.java")
}

tasks.named<Jar>("sourcesJar") {
    from(project(":jdk8").sourceSets.main.get().allSource)
    from(project(":jdk9").sourceSets.main.get().allSource)
    from(project(":sugar-cubes-cloner").sourceSets.main.get().allSource)
    exclude("org/sugarcubes/cloner/Placeholder.java")
    exclude("**/*.class")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/sugar-cubes/sugar-cubes-cloner")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            pom {
                name.set("Java Cloner library")
                description.set("Deep cloning of any objects")
                url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
                properties.set(mapOf(
                ))
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
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["gpr"])
}
