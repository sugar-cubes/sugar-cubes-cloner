import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    // Gradle 9 does not provide the launcher, the version comes from the JUnit BOM of junit-jupiter
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "utf-8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

// tests on another JDK: -PtestJdk=21, another vendor: -PtestJdkVendor=ibm (IBM Semeru, OpenJ9)
val testJdk = (findProperty("testJdk") as String?)?.toInt()
val testJdkVendor = findProperty("testJdkVendor") as String?

if (testJdk != null) {
    tasks.withType<Test>().configureEach {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(testJdk))
            if (testJdkVendor != null) {
                vendor.set(if (testJdkVendor.equals("ibm", true)) JvmVendorSpec.IBM else JvmVendorSpec.matching(testJdkVendor))
            }
        })
    }
}

tasks.withType<Javadoc> {
    enabled = false
}
