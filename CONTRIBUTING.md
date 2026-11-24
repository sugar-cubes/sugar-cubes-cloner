# Contributing

## Commit messages

Commit messages follow [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/):

```
<type>(<scope>)!: <description>

<body>

<footer>
```

- The subject line is at most 72 characters long.
- The description is in the imperative mood ("add", not "added" or "adds"), starts with a lowercase letter (unless it
  starts with an identifier) and has no period at the end.
- The scope is optional, see the list below.
- `!` after the type or the scope marks a breaking change; the body or a `BREAKING CHANGE:` footer explains what
  changes for the users.
- The body is optional, wrapped at 72 characters, and tells why the change is made, not how.

### Types

| Type       | Changes                                                       | In the changelog |
|------------|---------------------------------------------------------------|------------------|
| `feat`     | new behavior of the library or the tools                      | Added, Changed   |
| `fix`      | fixed behavior                                                | Fixed            |
| `perf`     | faster or smaller, the same behavior                          | Changed          |
| `refactor` | code structure, the same behavior                             | no               |
| `test`     | tests and benchmarks                                          | no               |
| `docs`     | documentation, Javadoc, the site                              | if notable       |
| `build`    | the build, dependencies, toolchains                           | if notable       |
| `ci`       | GitHub Actions workflows                                      | no               |
| `chore`    | other changes which affect neither the code nor the build     | no               |
| `style`    | formatting                                                    | no               |
| `revert`   | reverts a commit, the body names the reverted commit          | if released      |

### Scopes

| Scope        | Part of the project                                                                  |
|--------------|--------------------------------------------------------------------------------------|
| `api`        | the exported packages `io.github.sugarcubes.cloner` and `io.github.sugarcubes.cloner.spi` |
| `engine`     | copying: contexts, slots, copiers, policies                                          |
| `toolset`    | low-level mechanisms: allocation, field access, reflection, `Unsafe`, module opening |
| `platform`   | JDK specific parts and the multi-release jar                                         |
| `generation` | generated copiers                                                                    |
| `agent`      | the Java agent                                                                       |
| `tools`      | the module `sugar-cubes-cloner-tools`: annotation processor, Maven and Gradle plugins |
| `bench`      | JMH benchmarks                                                                       |
| `site`       | the documentation site                                                               |

### Examples

```
feat(toolset): choose default mechanisms by the running JVM
fix(engine): store the value before the key in IdentitySlotMap
feat(api)!: rename Cloners.reflection() to defaults()
build: upgrade Gradle to 9.9.1
```

## Changelog

[CHANGELOG.md](CHANGELOG.md) follows [Common Changelog](https://common-changelog.org): it is written for the users
by hand when a version is released, the commits are its source, not its content. Changes are grouped into Changed,
Added, Removed and Fixed, breaking changes are prefixed with **Breaking:**.

## Versions

The version comes from the Git tags `v<major>.<minor>.<patch>` ([Semantic Versioning](https://semver.org)): a build of
a tagged commit without local changes has the version of the tag, e.g. `2.0.0`; any other build is a snapshot with
the number of commits since the tag, e.g. `2.0.0-5-SNAPSHOT`. The commit hash is in the `Implementation-Build`
attribute of the jar manifests.

## Publishing

The modules `sugar-cubes-cloner` and `sugar-cubes-cloner-tools` (with the marker of the Gradle plugin) are published
to Maven Central through the [Central Portal](https://central.sonatype.com) and to GitHub Packages:

- every push to `main` publishes a snapshot (`build` workflow): `<tag>-<commits since the tag>-SNAPSHOT` to
  GitHub Packages and to `https://central.sonatype.com/repository/maven-snapshots/`;
- a tag `v<version>` publishes a release (`release` workflow): signed artifacts to GitHub Packages and a deployment
  to the Central Portal, which is published there by hand, and a GitHub release with the notes from CHANGELOG.md.

The secrets of the repository:

| Secret                    | Value                                                                  |
|---------------------------|------------------------------------------------------------------------|
| `CENTRAL_PORTAL_USERNAME` | user token name of the Central Portal                                  |
| `CENTRAL_PORTAL_PASSWORD` | user token password of the Central Portal                              |
| `SIGNING_KEY`             | ASCII-armored secret PGP key, its public key on a public key server     |
| `SIGNING_PASSWORD`        | passphrase of the key                                                  |

Locally the same values are the Gradle properties `centralPortalUsername`, `centralPortalPassword`, `signingKey` and
`signingPassword`. `./gradlew nmcpZipAggregation nmcpCheckAggregationFiles` builds and checks the deployment without
uploading it, `publishAllPublicationsToProjectLocalRepository` publishes to the directory `repo`.

### Releasing

1. Set the release date in the section of the version in CHANGELOG.md, commit it to `main`.
2. Tag the commit: `git tag v2.0.0 && git push origin v2.0.0`.
3. Check the deployment in the Central Portal and publish it.
