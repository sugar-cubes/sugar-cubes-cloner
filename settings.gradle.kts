plugins {
    // downloads JDK toolchains (8, 9, 15, 16, 17) which are not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "sugar-cubes-cloner"

include("jdk8")
include("jdk9")
include("jdk9-module")
include("jdk15")
include("jdk16")
include("sugar-cubes-cloner")

if (file("incubator").isDirectory) {
    // place to test new ideas, out of version control
    include("incubator")
}
