import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnly("org.objenesis:objenesis:3.2")
    testCompileOnly(project(":jdk8"))
    testCompileOnly(project(":jdk9"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Jar> {

    from(project(":jdk8").sourceSets["main"].output)
    from(project(":jdk9").sourceSets["main"].output)

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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/mbutov/sugar-cubes-cloner")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}
