plugins {
    id("java")
    id("checkstyle")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

checkstyle {
    toolVersion = "8.14"
    configFile = rootProject.file("checkstyle/checkstyle.xml")
    sourceSets = listOf(project.java.sourceSets["main"])
}
