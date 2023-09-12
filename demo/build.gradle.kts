
val currentVersion = file("../version.txt").readText().trim()

group = "io.github.sugar-cubes.cloner-demo"
version = currentVersion

allprojects {
    val clonerVersion by extra(currentVersion)
}
