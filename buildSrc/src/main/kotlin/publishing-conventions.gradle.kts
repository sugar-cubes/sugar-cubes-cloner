// Publishing of a module to GitHub Packages and Maven Central (Maven Central through the nmcp aggregation of the root project):
// the POM information which Maven Central requires, the repositories, the signing. The modules create their
// publications and set the name and the description of their POMs.

plugins {
    `maven-publish`
    signing
    // the publications go to Maven Central with the aggregation of the root project
    id("com.gradleup.nmcp")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sugar-cubes/sugar-cubes-cloner")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orElse(providers.gradleProperty("gpr.user"))
                    .orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orElse(providers.gradleProperty("gpr.key"))
                    .orNull
            }
        }
        maven {
            // for checks of the published files
            name = "ProjectLocal"
            url = rootProject.layout.projectDirectory.dir("repo").asFile.toURI()
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
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
                connection.set("scm:git:https://github.com/sugar-cubes/sugar-cubes-cloner.git")
                developerConnection.set("scm:git:ssh://git@github.com/sugar-cubes/sugar-cubes-cloner.git")
                url.set("https://github.com/sugar-cubes/sugar-cubes-cloner")
            }
            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/sugar-cubes/sugar-cubes-cloner/issues")
            }
        }
    }
}

// Maven Central requires signatures of releases; snapshots are signed only if a key is given.
// The key is an ASCII-armored secret key in the property signingKey (ORG_GRADLE_PROJECT_signingKey in CI).
val signingKey = providers.gradleProperty("signingKey").orNull
val release = !version.toString().endsWith("-SNAPSHOT")
signing {
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, providers.gradleProperty("signingPassword").orNull)
    }
    isRequired = release
    if (signingKey != null || release) {
        sign(publishing.publications)
    }
}
