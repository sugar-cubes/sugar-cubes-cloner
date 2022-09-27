apply(from = "licenseFormat.gradle.kts")

extra["version"] = file("version.txt").readText()

allprojects {
    group = "io.github.sugar-cubes"
    version = rootProject.extra["version"] as String
    description = "Java Reflection Cloner library"

    repositories {
        mavenCentral()
    }
}
