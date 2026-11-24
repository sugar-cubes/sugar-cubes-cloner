import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "8.11.0" apply false
    // version from the Git tags
    id("git-version")
}

// license header of Java sources, HEADER.txt wrapped into a block comment
val licenseHeader = rootProject.file("HEADER.txt").readLines()
    .joinToString("\n", "/*\n", "\n */\n") { " * $it".trimEnd() }

allprojects {
    group = "io.github.sugar-cubes"
    description = "Java deep cloning library"

    repositories {
        mavenCentral()
    }

    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        java {
            target("src/*/java/**/*.java")
            // the header ends before the package, imports, module or the javadoc of package-info and module-info
            licenseHeader(licenseHeader, "(package|import|module|/\\*\\*)")
        }
    }
}
