apply(from = "licenseFormat.gradle.kts")

val currentVersion = file("version.txt").readText().trim()

allprojects {
    group = "io.github.sugar-cubes"
    version = currentVersion
    description = "Java Reflection Cloner library"

    repositories {
        mavenCentral()
    }
}
