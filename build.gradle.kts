import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "8.11.0" apply false
    // version from the Git tags
    id("git-version")
    // publishing of the modules to Maven Central through the Central Portal
    id("com.gradleup.nmcp.aggregation")
}

nmcpAggregation {
    centralPortal {
        // a user token of https://central.sonatype.com, ORG_GRADLE_PROJECT_centralPortalUsername and
        // ORG_GRADLE_PROJECT_centralPortalPassword in CI
        username = providers.gradleProperty("centralPortalUsername")
        password = providers.gradleProperty("centralPortalPassword")
        // a release is checked and published by hand in the portal
        publishingType = "USER_MANAGED"
    }
}

dependencies {
    nmcpAggregation(project(":sugar-cubes-cloner"))
    nmcpAggregation(project(":sugar-cubes-cloner-tools"))
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
