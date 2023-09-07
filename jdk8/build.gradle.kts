plugins {
    id("build-logic.build-commons")
    id("java-test-fixtures")
}

dependencies {

    compileOnly("org.objenesis:objenesis:3.3")

    testFixturesImplementation("org.objenesis:objenesis:3.3")
    testFixturesImplementation("org.hamcrest:hamcrest:2.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    testImplementation("org.objenesis:objenesis:3.3")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}
