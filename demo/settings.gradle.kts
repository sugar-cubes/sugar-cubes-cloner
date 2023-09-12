rootProject.name = "cloner-demo"

fun includeAs(path: String, name: String) {
    include(path)
    project(":${path}").name = name
}

includeAs("jdk8", "cloner-demo-jdk8")
includeAs("jdk11", "cloner-demo-jdk11")
includeAs("jdk17", "cloner-demo-jdk17")
