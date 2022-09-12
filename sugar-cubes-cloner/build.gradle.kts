import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

dependencies {
    compileOnly("org.objenesis:objenesis:3.2")
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
    (options as StandardJavadocDocletOptions).links("https://docs.oracle.com/javase/8/docs/api/")
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
            url = URI("https://maven.pkg.github.com/mbutov/sugar-cubes-cloner")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications["gpr"])
}
