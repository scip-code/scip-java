package tests

import java.nio.file.Files
import java.nio.file.StandardOpenOption
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

private const val GRADLE_VERSION = "9.1.0"

// Gradle 9.1.0 runs on JDK 17 through 25.
// See https://docs.gradle.org/current/userguide/compatibility.html
private const val GRADLE_MAX_JDK = 25

class GradleBuildToolTest : BuildToolHarness() {

    private fun checkGradleBuild(
        name: String,
        fixture: String,
        expectedScipFiles: Int = 0,
        expectedPackages: String = "",
        extraArguments: List<String> = emptyList(),
        substitutions: Map<String, String> = emptyMap(),
    ): DynamicTest =
        checkBuild(
            "$name-gradle-$GRADLE_VERSION",
            fixture,
            expectedScipFiles = expectedScipFiles,
            expectedPackages = expectedPackages,
            extraArguments = extraArguments,
            substitutions = substitutions,
            initCommand = { workingDirectory ->
                // Gradle needs a build script present to generate the wrapper;
                // the real one is laid down afterwards from the fixture.
                val script = workingDirectory.resolve("build.gradle")
                Files.createDirectories(script.parent)
                Files.write(
                    script,
                    ByteArray(0),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                )
                listOf("gradle", "wrapper", "--gradle-version", GRADLE_VERSION)
            },
            maxJdk = GRADLE_MAX_JDK,
        )

    @TestFactory
    fun tests(): List<DynamicTest> = buildList {
        add(
            checkGradleBuild(
                "annotation-path",
                "gradle/annotation-path",
                // An immutable version is generated alongside the original class,
                // so two SCIP shards are produced.
                expectedScipFiles = 2,
            )
        )
        // Regression test: annotation processors declared only for the test source
        // set (testAnnotationProcessor) make Gradle pass -processorpath to
        // compileTestJava, so the SCIP javac plugin must be added to that same
        // configuration or plugin discovery fails with "plug-in not found: scip".
        add(
            checkGradleBuild(
                "test-annotation-path",
                "gradle/test-annotation-path",
                // The generated immutable class is compiled alongside the original
                // test source, so two SCIP shards are produced.
                expectedScipFiles = 2,
            )
        )
        // Regression test: processors inherited from another configuration (as
        // with the Error Prone Gradle plugin) also make Gradle pass -processorpath.
        add(
            checkGradleBuild(
                "inherited-annotation-path",
                "gradle/inherited-annotation-path",
                expectedScipFiles = 2,
            )
        )
        add(
            checkGradleBuild("build-with-Werror", "gradle/build-with-Werror", expectedScipFiles = 2)
        )
        // Regression test: Groovy string interpolation puts GString elements
        // (not java.lang.String) into compilerArgs, which used to crash the
        // plugin with a ClassCastException.
        // https://github.com/scip-code/scip-java/issues/801
        add(
            checkGradleBuild(
                "gstring-compiler-args",
                "gradle/gstring-compiler-args",
                expectedScipFiles = 1,
            )
        )
        // Regression test: a build that replaces compilerArgs after the plugin
        // ran must still load the SCIP javac plugin.
        add(
            checkGradleBuild(
                "replaced-compiler-args",
                "gradle/replaced-compiler-args",
                expectedScipFiles = 1,
            )
        )
        // Regression test: source sets other than main and test need the SCIP
        // javac plugin on their classpath too.
        add(
            checkGradleBuild("custom-source-set", "gradle/custom-source-set", expectedScipFiles = 2)
        )
        add(
            checkGradleBuild(
                "publishing",
                "gradle/publishing",
                expectedScipFiles = 1,
                expectedPackages = "maven:org.scip-code:example-library:1.1",
            )
        )
        add(checkGradleBuild("basic", "gradle/basic", expectedScipFiles = 2))
        // Regression test: FAIL_ON_PROJECT_REPOS rejects project-level
        // repositories, so the plugin must not inject Maven Central/local.
        // https://github.com/scip-code/scip-java/issues/847
        add(
            checkGradleBuild("centralized-repos", "gradle/centralized-repos", expectedScipFiles = 1)
        )
        add(
            checkGradleBuild(
                "toolchains",
                "gradle/toolchains",
                expectedScipFiles = 1,
                substitutions = mapOf("@JAVA@" to "17"),
            )
        )
        add(
            checkGradleBuild(
                "protobuf-generator",
                "gradle/protobuf-generator",
                expectedScipFiles = 2,
            )
        )
        add(
            checkGradleBuild(
                "explicit",
                "gradle/explicit",
                expectedScipFiles = 2,
                extraArguments = listOf("--build-tool", "gradle"),
            )
        )
        add(
            checkGradleBuild(
                "build-command",
                "gradle/build-command",
                expectedScipFiles = 1,
                extraArguments = listOf("--", "compileJava"),
            )
        )
        add(checkGradleBuild("kotlin2", "gradle/kotlin2", expectedScipFiles = 4))
        add(
            checkGradleBuild(
                "implementation-deps",
                "gradle/implementation-deps",
                expectedScipFiles = 1,
                expectedPackages =
                    """
                    |maven:com.fasterxml.jackson.core:jackson-annotations:2.14.1
                    |maven:com.fasterxml.jackson.core:jackson-core:2.14.1
                    |maven:com.fasterxml.jackson.core:jackson-databind:2.14.1
                    |maven:com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.1
                    |maven:com.marklogic:marklogic-client-api:6.1.0
                    |maven:com.squareup.okhttp3:logging-interceptor:4.10.0
                    |maven:com.squareup.okhttp3:okhttp:4.10.0
                    |maven:com.squareup.okio:okio-jvm:3.0.0
                    |maven:com.sun.mail:javax.mail:1.6.2
                    |maven:io.github.rburgst:okhttp-digest:2.7
                    |maven:javax.activation:activation:1.1
                    |maven:javax.ws.rs:javax.ws.rs-api:2.1.1
                    |maven:org.jetbrains.kotlin:kotlin-stdlib-common:1.6.20
                    |maven:org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10
                    |maven:org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10
                    |maven:org.jetbrains.kotlin:kotlin-stdlib:1.6.20
                    |maven:org.jetbrains:annotations:13.0
                    |maven:org.slf4j:slf4j-api:1.7.36
                    |"""
                        .trimMargin(),
            )
        )
        add(
            checkGradleBuild(
                "kotlin-jvm-toolchains-jdk17",
                "gradle/kotlin-jvm-toolchains",
                expectedScipFiles = 1,
                substitutions = mapOf("@JAVA@" to "17"),
            )
        )
        // Regression test: projects that lazily register custom source sets (e.g.
        // intTest) with a Java toolchain used to fail because the eager `.all {}`
        // API finalized the javaCompiler property before Gradle finished
        // configuring the task.
        add(
            checkGradleBuild(
                "lazy-sourceset-with-toolchain",
                "gradle/lazy-sourceset-with-toolchain",
                expectedScipFiles = 1,
            )
        )
    }
}
