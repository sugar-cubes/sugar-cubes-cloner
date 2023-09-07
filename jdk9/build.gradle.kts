import java.util.stream.Collectors
import java.util.stream.Stream

plugins {
    id("java")
    id("checkstyle")
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {

    compileOnly("org.objenesis:objenesis:3.3")
    implementation(project(":jdk8"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    jmh("com.esotericsoftware:kryo:5.5.0")
    jmh("io.github.kostaskougios:cloning:1.10.3")

}

fun modulesJvmArgs(option: String): List<String> {
    val modulesAndPackages = listOf(
        "java.base/java.lang",
        "java.base/java.lang.invoke",
        "java.base/java.util",
        "java.base/java.util.concurrent",
        "java.base/jdk.internal.misc",
    )
    return modulesAndPackages.stream()
        .flatMap {
            Stream.of(
                option, "${it}=ALL-UNNAMED",
                option, "${it}=org.sugarcubes.cloner",
            )
        }
        .collect(Collectors.toList())
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_9.toString()
    targetCompatibility = JavaVersion.VERSION_1_9.toString()

    options.compilerArgs.addAll(modulesJvmArgs("--add-exports"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
    jvmArgs = modulesJvmArgs("--add-opens")
}

jmh {
    jvmArgs.addAll(modulesJvmArgs("--add-opens"))
}

checkstyle {
    toolVersion = "8.14"
    configFile = rootProject.file("checkstyle/checkstyle.xml")
    sourceSets = listOf(project.java.sourceSets["main"])
}
