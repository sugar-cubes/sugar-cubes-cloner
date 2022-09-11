plugins {
    id("java")
    id("checkstyle")
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {

    implementation(project(":jdk8"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    jmh("com.esotericsoftware:kryo:5.3.0")
    jmh("io.github.kostaskougios:cloning:1.10.3")

}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_9.toString()
    targetCompatibility = JavaVersion.VERSION_1_9.toString()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "8.14"
    configFile = rootProject.file("checkstyle/checkstyle.xml")
    sourceSets = listOf(project.java.sourceSets["main"])
}
