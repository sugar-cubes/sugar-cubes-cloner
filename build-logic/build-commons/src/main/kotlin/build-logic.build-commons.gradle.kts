import java.util.stream.Collectors
import java.util.stream.Stream

plugins {
    id("java")
    id("checkstyle")
}

tasks.withType<JavaCompile> {
    options.encoding = "utf-8"
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

class BuildUtils {

    fun modulesJvmArgs(option: String): List<String> {
        val modulesAndPackages = listOf(
            "java.base/java.lang",
            "java.base/java.lang.reflect",
            "java.base/java.lang.invoke",
            "java.base/java.util",
            "java.base/java.util.concurrent",
            "java.base/jdk.internal.misc",
        )
        return modulesAndPackages.stream()
            .flatMap {
                Stream.of(
                    option, "${it}=ALL-UNNAMED",
                    option, "${it}=io.github.sugarcubes.cloner",
                )
            }
            .collect(Collectors.toList())
    }

}

extensions.add("utils", BuildUtils())
