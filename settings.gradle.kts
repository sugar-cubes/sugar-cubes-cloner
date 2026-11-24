plugins {
    // downloads JDK toolchains (8, 9, 15, 16, 17) which are not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// not the name of the published module sugar-cubes-cloner: project names must be unique for nmcp
rootProject.name = "sugar-cubes-cloner-root"

include("jdk8")
include("jdk9")
include("jdk9-module")
include("jdk15")
include("jdk16")
include("jdk24")
include("sugar-cubes-cloner")
include("sugar-cubes-cloner-tools")
