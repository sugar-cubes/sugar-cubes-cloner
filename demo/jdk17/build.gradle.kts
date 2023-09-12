import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("demo-project")
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("commons-io:commons-io:2.13.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("io.github.sugarcubes.cloner.demo.jdk17.ClonerDemoJdk17Application")
}

tasks.named<BootRun>("bootRun") {
    val agent = File("${rootDir}/../repo/io/github/sugar-cubes/sugar-cubes-cloner/${extra["clonerVersion"]}/sugar-cubes-cloner-${extra["clonerVersion"]}.jar")
    jvmArgs = listOf(
        "-javaagent:${agent.canonicalPath}",
    )
}
