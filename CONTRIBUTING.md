# Contributing guide

This page documents tips and tricks for contributing to the
[scip-code/scip-java](https://github.com/scip-code/scip-java) codebase.

## System dependencies

The recommended way to get a working development environment is via
[Nix](https://nixos.org/download) and the provided [flake](flake.nix):

```sh
nix develop              # default shell (JDK 17)
nix develop .#jdk17      # JDK 17
nix develop .#jdk21      # JDK 21
nix develop .#jdk25      # JDK 25
```

This drops you into a shell with `gradle`, `maven`, `bazelisk`, `nodejs`,
`yarn`, `git`, `jq`, etc. all pinned to the versions used in CI.

If you'd rather install tools manually, you'll need at least:

- `java`: JDK 17 or newer
- `git`: any version should work
- `gradle`: `brew install gradle`, or see
  [general installation guide](https://gradle.org/install/).
- `mvn`: `brew install maven`, or see
  [general installation guide](https://www.baeldung.com/install-maven-on-windows-linux-mac).

## Project structure

These are the main components of the project.

- `scip-javac/src/main/java`: the Java compiler plugin that creates
  SCIP files.
- `tests/minimized`: minimized Java source files that reproduce interesting test
  cases.
- `tests/unit`: fast running unit tests that are helpful for local edit-and-test
  workflows.
- `tests/snapshots`: slow running
  ["snapshot tests"](https://jestjs.io/docs/en/snapshot-testing) that index a
  corpus of published Java libraries.
- `scip-java/src/main/kotlin`: implementation of the `scip-java` command-line
  interface.
- `scip-java/src/test`: build-tool integration tests and fixtures for the
  `scip-java` command-line interface.
- `settings.gradle.kts`: Gradle project layout.
- `build.gradle.kts`: Gradle build definition.
- `gradle/libs.versions.toml`: dependency and plugin versions.

## Helpful commands

| Command                                                 | Where    | Description                                                             |
| ------------------------------------------------------- | -------- | ----------------------------------------------------------------------- |
| `gradle test --no-daemon`                               | terminal | Run all Gradle tests.                                                    |
| `gradle :scip-java:test --no-daemon`                    | terminal | Run CLI build-tool integration tests (Gradle, Maven, SCIP config).       |
| `gradle :scip-kotlin-analysis:test --no-daemon`                 | terminal | Run Kotlin indexer tests.                                        |
| `gradle :scip-snapshots:test --no-daemon`               | terminal | Compare Java and Kotlin snapshot goldens.                                |
| `gradle :scip-snapshots:saveSnapshots --no-daemon`      | terminal | Regenerate Java and Kotlin snapshot goldens.                             |
| `gradle :scip-java:installDist --no-daemon`             | terminal | Build a local `scip-java` distribution under `scip-java/build/install/`. |
| `gradle :scip-java:run --args='--cwd DIRECTORY'`        | terminal | Run `scip-java` against a given Gradle/Maven build.                      |
| `gradle spotlessApply`                                  | terminal | Format Java and Kotlin sources with Spotless.                            |
| `gradle spotlessCheck`                                  | terminal | Check Java and Kotlin formatting. Enforced by GitHub Actions.            |

## Import the project into IntelliJ

It's recommended to use IntelliJ when editing code in this codebase.

First, install the
[IntelliJ Community Edition](https://www.jetbrains.com/idea/download/). The
community edition is
[open source](https://github.com/JetBrains/intellij-community) and free to use.

Next, install the following IntelliJ plugins:

- Google Java Format

Next, follow
[these instructions](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse)
here to configure the Google Java formatter.

Finally, run "File > Project From Existing Sources" to import the Gradle build
into IntelliJ. Select the "Gradle" option if it asks you to choose a build
model.

It's best to run tests from Gradle, not from the IntelliJ UI.

## Tests are written in Java with JUnit 5

The Java tests use [JUnit 5](https://junit.org/junit5/) through Gradle's JUnit
Platform support. The snapshot suite is a JUnit `@TestFactory` that emits one
dynamic test per generated document, comparing it against the committed goldens
under `scip-snapshots/expected`. Build-tool tests (`scip-java/src/test`) are
written in Kotlin.
