import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import java.util.stream.Collectors
import java.util.stream.Stream

plugins {
    id("java")
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

val utils = BuildUtils()

extensions.add("utils", utils)

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(utils.modulesJvmArgs("--add-exports"))
}

tasks.withType<Test> {
    jvmArgs = jvmArgs + utils.modulesJvmArgs("--add-opens")
}
