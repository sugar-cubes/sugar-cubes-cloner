plugins {
    id("com.github.hierynomus.license") version "0.16.1"
}

val currentVersion = file("version.txt").readText().trim()

allprojects {
    group = "io.github.sugar-cubes"
    version = currentVersion
    description = "Java deep cloning library"

    repositories {
        mavenCentral()
    }

    apply(plugin = "com.github.hierynomus.license")

    license {
        header = rootProject.file("HEADER.txt")
        useDefaultMappings = true
        mapping("java", "SLASHSTAR_STYLE")
        strictCheck = true
    }
}
