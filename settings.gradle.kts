rootProject.name = "sugar-cubes-cloner"

include("jdk8")
include("jdk9")
include("sugar-cubes-cloner")

if (file("incubator").isDirectory) {
    // place to test new ideas, out of version control
    include("incubator")
}
