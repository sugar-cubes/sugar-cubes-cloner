
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // publishing to Maven Central through the Central Portal
    implementation("com.gradleup.nmcp:nmcp:1.6.2")
}
