plugins {
    id("build-logic.build-commons")
}

dependencies {

    implementation(project(":jdk8"))
    implementation(project(":jdk9"))

    testImplementation(testFixtures(project(":jdk8")))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

}

tasks.withType<JavaCompile> {
    targetCompatibility = JavaVersion.VERSION_16.toString()
    sourceCompatibility = JavaVersion.VERSION_16.toString()

    options.compilerArgs.addAll(utils.modulesJvmArgs("--add-exports"))
}

tasks.named<Test>("test") {
    jvmArgs = utils.modulesJvmArgs("--add-opens")
}
