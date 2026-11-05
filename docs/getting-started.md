# Getting started

By following the instructions on this page, you should be able to generate a
[SCIP](https://github.com/scip-code/scip) index of your Java codebase using
Gradle, Maven, or Bazel. See
[Supported build tools](#supported-build-tools) for an overview of other build
tools that we're planning to support in the future.

## Install `scip-java`

The most common way to use `scip-java` is to run it from CI to upload SCIP
indexes after merging a pull request.

The easiest way to install `scip-java` is to use the Docker image.

<!-- The #docker-container span below is included for backwards compatibility
after we renamed the "Docker container" section into "Docker image". -->

<span id="docker-container"></span>

### Docker image

Use the `ghcr.io/scip-code/scip-java` Docker image to run `scip-java`.

```sh
$ docker run -v $(pwd):/sources --env JVM_VERSION=17 ghcr.io/scip-code/scip-java:latest scip-java index
$ src code-intel upload # (optional) upload index to Sourcegraph
```

If everything went OK, a `index.scip` file should exist after the command has
finished indexing the project.

> The `ghcr.io/scip-code/scip-java` Docker image is made available for convenience at
> the cost of performance. The `ghcr.io/scip-code/scip-java` image is a big download
> because it includes pre-installed versions of Java 17, Java 21, and Java 25.
> The `ghcr.io/scip-code/scip-java` image has slow performance because it needs to
> download all external dependencies of your codebase on every invocation.
>
> For better performance, we recommend using your own Docker image together with
> the [Java launcher](#java-launcher) option.

By default, the `ghcr.io/scip-code/scip-java` Docker image tries Java 21 and falls
back to Java 17. Use the following commands to select a specific JVM version:

```sh
# Java 17
docker run -v $(pwd):/sources --env JVM_VERSION=17 ghcr.io/scip-code/scip-java:latest scip-java index

# Java 21
docker run -v $(pwd):/sources --env JVM_VERSION=21 ghcr.io/scip-code/scip-java:latest scip-java index

# Java 25
docker run -v $(pwd):/sources --env JVM_VERSION=25 ghcr.io/scip-code/scip-java:latest scip-java index

```

### Java launcher

Use the Java launcher to install `scip-java` on your local computer or any
Docker image with a pre-installed Java version (JDK 17 or newer).

```sh
# macOS/Linux
curl -fLo coursier https://git.io/coursier-cli \
  && chmod +x coursier \
  && ./coursier launch org.scip-code:scip-java:@STABLE_VERSION@ -- --help

# macOS Homebrew
brew install coursier/formulas/coursier \
 && coursier launch org.scip-code:scip-java:@STABLE_VERSION@ -- --help
```

The Java launcher uses
[Coursier](https://get-coursier.io/docs/cli-installation.html) to download
`scip-java` artifacts from Maven Central and build an executable `scip-java`
binary. The jar files for `scip-java` are downloaded the first time you run the
`launch` command, and they are cached for subsequent runs.

Additional command-line flags can be passed to `scip-java` after the `--`
argument. For example, replace `--help` with `index` in the command above to run
the `index` subcommand.

### Java fat jar

Use the fat jar option to build a standalone `scip-java` executable. Note that
you still need a Java installation (JDK 17 or newer) to run the `scip-java`
binary.

```sh
# macOS/Linux
curl -fLo coursier https://git.io/coursier-cli \
  && chmod +x coursier \
  && ./coursier bootstrap --assembly -o scip-java org.scip-code:scip-java:@STABLE_VERSION@ --main org.scip_code.scip_java.ScipJava
./scip-java --help
```

The fat jar uses the [Coursier](https://get-coursier.io/docs/cli-installation)
`bootstrap` command to generate a binary. The fat jar binary includes all
dependencies and does not require further access to the internet after
installation.

### Java library

The `scip-java` command-line interface is published to Maven Central. You can
run the command-line interface as a library by directly invoking the `main()`
method on the `org.scip_code.scip_java.ScipJava` class.

[![](https://img.shields.io/maven-central/v/org.scip-code/scip-java)](https://repo1.maven.org/maven2/org/scip-code/scip-java/)

If you're using Gradle.

```groovy
implementation group: 'org.scip-code', name: 'scip-java', version: '@STABLE_VERSION@'
```

If you're using Maven.

```xml
<dependency>
    <groupId>org.scip-code</groupId>
    <artifactId>scip-java</artifactId>
    <version>@STABLE_VERSION@</version>
</dependency>
```

## Run `scip-java index`

> The `index` command is only supported for Gradle and Maven. See
> [Supported build tools](#supported-build-tools) for more details about other
> build tools.

> For cross-repository navigation, additional dependency metadata is required (for Gradle, enable `maven-publish`); see [Cross-Repository Navigation (Optional)](#cross-repository-navigation-optional).

Run the `scip-java index` command to generate a SCIP index for your codebase.
This command should automatically infer the structure of your codebase and
configure your build tool to generate SCIP.

```sh
# At the root of a Gradle or Maven codebase.
$ scip-java index
...
info: /path/to/index.scip
```

| Build tool | Default command                                            |
| ---------- | ---------------------------------------------------------- |
| Gradle     | `clean compileTestJava compileTestKotlin compileTestKotlinJvm` |
| Maven      | `--batch-mode clean verify -DskipTests`                    |

Customize the build command by passing additional arguments after
`scip-java index --`.

```sh
# Example: use `mvn package` instead of `mvn verify`
scip-java index -- --batch-mode -DskipTests package
```

The `index.scip` file contains the SCIP index and is ready to be used.

### Cross-Repository Navigation (Optional)

By default, `scip-java index` enables navigation within the local repository.
To enable cross-repository navigation, `scip-java` needs dependency metadata
from your build.

For Gradle builds, this requires enabling `maven-publish` and defining a Maven
publication (for example, from `components["java"]`) in `build.gradle` or
`build.gradle.kts`.

If this is not configured, indexing may still succeed, but cross-repository
definitions/references may not resolve.

See [Manual configuration: Step 4 (optional): Enable cross-repository navigation](manual-configuration.md#step-4-optional-enable-cross-repository-navigation)
for detailed setup options.

> Running `scip-java index` may perform side-effects on your build like cleaning
> the compile cache before indexing. This is done to make sure that all source
> files in the codebase get indexed.

Run `scip-java index --help` to learn more about the available command-line
options.

```scala
org.scip_code.scip_java.ScipJava.INSTANCE.printHelp(Console.out)
```

## Supported programming languages

| Programming language | Gradle | Maven | Tracking issue |
| -------------------- | ------ | ----- | -------------- |
| Java                 | ✅     | ✅    |                |
| Kotlin               | ✅     | ❌    |                |

### Java

The `scip-java` indexer is implemented as a Java compiler plugin that runs as
part of your regular compilation in the build tool. By using Java compiler APIs,
`scip-java` is able to generate accurate indexing information for a broad range
of Java versions. The project targets support for the three most recent Java LTS
releases.

| Java version | Support                      | Tracking issue |
| ------------ | ---------------------------- | -------------- |
| Java 7       | ❌                           |                |
| Java 8       | ❌                           |                |
| Java 11      | ❌                           |                |
| Java 17      | ✅, requires `--add-exports` |                |
| Java 21      | ✅, requires `--add-exports` |                |
| Java 25      | ✅, requires `--add-exports` |                |

For Java 17 and newer versions, the following JVM options are required:

```
--add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
```

### Kotlin

The Kotlin support in scip-java is less mature compared to the Java support.
Don't hesitate to report issues at https://github.com/scip-code/scip-java if
you encounter issues using the Kotlin support.

## Supported build tools

It's possible to use scip-java with any Java build tool. Some build tools can be
automatically configured by the `scip-java index` command while other build
tools may require [manual configuration](manual-configuration.md).

### Automatic configuration

Please open an issue if your build tool is not listed in the table below. Feel
free to subscribe to the tracking issues to receive updates on your build tool.

| Build tool | Java | Kotlin | Tracking issue                                                                   |
| ---------- | ---- | ------ | -------------------------------------------------------------------------------- |
| Maven      | ✅   | ❌     |                                                                                  |
| Gradle     | ✅   | ✅     |                                                                                  |
| Ant        | ❌   | ❌     | [scip-code/scip-java#305](https://github.com/scip-code/scip-java/issues/305) |
| Bazel      | ✅   | ❌     |                                                                                  |
| Buck       | ❌   | ❌     | [scip-code/scip-java#99](https://github.com/scip-code/scip-java/issues/99)   |

**✅**: automatic indexing is fully supported. Please report a bug if the
`scip-java index` command does not work on your codebase.

**❌**: automatic indexing is not supported but (!) you may still be able to use
`scip-java` by configuring it manually using the instructions
[here](manual-configuration.md).

### Manual configuration

See the [manual configuration](manual-configuration.md) guide for instructions
on how to configure scip-java to work with any build tool.

### Gradle

The `scip-java index` build should be able to automatically index most Gradle
projects. You Gradle build must contain one of the following files in the same
directory where `scip-java index` gets invoked.

- settings.gradle
- gradlew
- build.gradle
- build.gradle.kts

The following Gradle integrations are not yet supported:

| Integration | Supported | Tracking issue                                                                   |
| ----------- | --------- | -------------------------------------------------------------------------------- |
| Android     | ❌        | [scip-code/scip-java#177](https://github.com/scip-code/scip-java/issues/177) |
| Kotlin      | ✅        |                                                                                  |

### Maven

The `scip-java index` build should be able to automatically index most Maven
projects. Your Maven build must contain a `pom.xml` file in the same directory
where `scip-java index` gets invoked.

The following Maven integrations are not yet supported:

| Integration         | Supported | Tracking issue                                                                   |
| ------------------- | --------- | -------------------------------------------------------------------------------- |
| kotlin-maven-plugin | ❌        | [scip-code/scip-java#304](https://github.com/scip-code/scip-java/issues/304) |

### Bazel

There are two approaches to index Bazel/Java codebases: automatic and manual.

Don't hesitate to open an issue in the
[scip-java repository](https://github.com/scip-code/scip-java) if you have any
questions about using scip-java with Bazel builds.

#### Automatic - `aspect`

Since scip-java v0.8.24, it's possible to automatically index Bazel/Java
codebases via `scip-java index`.

```sh
scip-java index "--bazel-scip-java-binary=$(which scip-java)"
```

When using this approach, indexing happens mostly inside the Bazel action graph,
benefitting from parallel compilation and the Bazel build cache.

The `--bazel-scip-java-binary` argument is required due to implementation
details, scip-java runs an [aspect](https://bazel.build/extending/aspects) that
requires the absolute path to the `scip-java` binary.

#### Manual - `select`

It's possible to index Bazel codebases by integrating scip-java directly into
the build configuration. To achieve this, scip-java must be configured in
`MODULE.bazel` and `BUILD` files. The scip-java repository contains an example for
how to configure everything, including how to build scip-java itself from
source.

- [MODULE.bazel](../examples/bazel-example/MODULE.bazel):
  adds the required dependencies to be able to run scip-java itself.
- [BUILD](../examples/bazel-example/src/main/java/example/BUILD):
  configured `java_library` and `java_binary` targets to be indexed with
  scip-java.

Once configured, build the codebase with the SCIP compiler plugin.

```sh
bazel build //... --@scip_java//scip-javac:enabled=true
```

Next, run the following command to generate the SCIP index (`index.scip`).

```
bazel run @scip_java//scip-aggregator:bazel -- --sourceroot $PWD

# (optional) Validate that SCIP files were generated.
# The command below works for the `examples/bazel-example` directory in the scip-code/scip-java repository.
❯ jar tf bazel-bin/src/main/java/example/libexample.jar | grep scip$
META-INF/scip/src/main/java/example/Example.java.scip
```

Finally, run the following commands to upload the SCIP index to Sourcegraph.

```
# 1. Install src
npm install -g @sourcegraph/src # Or yarn global add @sourcegraph/src

# 2. Authenticate with Sourcegraph
export SRC_ACCESS_TOKEN=sgp_YOUR_ACCESS_TOKEN
export SRC_ENDPOINT=https://sourcegraph.example.com
src login # validate the token authenticates correctly

# 3. Upload SCIP index to Sourcegraph
src code-intel upload # requires index.scip file to exist
```
