// Version of all projects from the Git tags v<major>.<minor>.<patch>: 2.0.0 for a tagged commit without local
// changes, 2.0.0-5-SNAPSHOT for any other build, 5 commits after the tag; 0.0.0-SNAPSHOT without Git or tags.
// The abbreviated commit hash is the extra property gitHash of all projects, for the jar manifests.
// Applied to the root project.

val describe = providers.exec {
    workingDir = rootDir
    commandLine("git", "describe", "--tags", "--match", "v[0-9]*", "--long", "--dirty")
    isIgnoreExitValue = true
}

// v2.0.0-5-gb0e8c26-dirty: tag, commits since the tag, hash, local changes
val described = Regex("""v(.+)-(\d+)-g([0-9a-f]+)(-dirty)?""")
    .matchEntire(if (describe.result.get().exitValue == 0) describe.standardOutput.asText.get().trim() else "")

val gitVersion = when {
    described == null -> "0.0.0-SNAPSHOT"
    described.groupValues[2] == "0" && described.groupValues[4].isEmpty() -> described.groupValues[1]
    else -> "${described.groupValues[1]}-${described.groupValues[2]}-SNAPSHOT"
}
val gitHash = described?.groupValues?.get(3) ?: "unknown"

allprojects {
    // the receiver explicitly: extra in the lambda would be the extra of the root project
    val target = this
    target.version = gitVersion
    target.extensions.extraProperties["gitHash"] = gitHash
}
