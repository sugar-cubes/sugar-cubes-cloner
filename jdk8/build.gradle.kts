plugins {
    id("java")
    id("java-test-fixtures")
    id("checkstyle")
}

dependencies {

    compileOnly("org.objenesis:objenesis:3.3")

    testFixturesImplementation("org.objenesis:objenesis:3.3")
    testFixturesImplementation("org.hamcrest:hamcrest:2.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "8.14"
    configFile = rootProject.file("checkstyle/checkstyle.xml")
    sourceSets = listOf(project.java.sourceSets["main"])
}
