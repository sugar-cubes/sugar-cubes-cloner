plugins {
    id("java")
    id("checkstyle")
}

checkstyle {
    toolVersion = "14.4.0"
    configFile = rootProject.file("checkstyle/checkstyle.xml")
    sourceSets = listOf(project.java.sourceSets["main"])
}

tasks.withType<Checkstyle>().configureEach {
    // Checkstyle 11+ runs on JDK 21+, the sources themselves target older JDKs
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
}
