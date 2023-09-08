plugins {
    id("java")
}

dependencies {
    compileOnly("org.objenesis:objenesis:3.3")
}

tasks.withType<JavaCompile> {
    targetCompatibility = JavaVersion.VERSION_1_9.toString()
    sourceCompatibility = JavaVersion.VERSION_1_9.toString()
    options.encoding = "utf-8"
}
