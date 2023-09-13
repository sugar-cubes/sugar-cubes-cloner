
plugins {
    id("java")
}

repositories {
    maven("file:${rootDir}/../repo")
    mavenCentral()
}

dependencies {
    implementation("io.github.sugar-cubes:sugar-cubes-cloner:1.2.4-SNAPSHOT")
    implementation("org.objenesis:objenesis:3.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "utf-8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
}
