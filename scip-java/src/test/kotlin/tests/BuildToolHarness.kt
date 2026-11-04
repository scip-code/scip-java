package tests

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.scip_code.scip_java.CliEnvironment
import org.scip_code.scip_java.ScipJavaApp
import org.scip_code.scip_java.buildtools.ClasspathEntry

/**
 * Base class for build-tool integration tests. Each `check*` helper returns a JUnit 5
 * [DynamicTest]; suites expose them from a `@TestFactory` method.
 */
abstract class BuildToolHarness {

    /** Run `scip-java` in-process with stdout/stderr redirected into a buffer. */
    private fun runScipJava(workingDirectory: Path, arguments: List<String>): Pair<Int, String> {
        val buffer = ByteArrayOutputStream()
        val stream = PrintStream(buffer, true, StandardCharsets.UTF_8.name())
        val app = ScipJavaApp()
        app.env =
            CliEnvironment(
                workingDirectory = workingDirectory,
                standardOutput = stream,
                standardError = stream,
            )
        val exit = app.run(arguments)
        return exit to buffer.toString(StandardCharsets.UTF_8.name())
    }

    private fun listScipShards(targetroot: Path): List<Path> {
        if (!Files.isDirectory(targetroot)) return emptyList()
        Files.walk(targetroot).use { stream ->
            return stream.filter { it.toString().endsWith(".scip") }.collect(Collectors.toList())
        }
    }

    /**
     * Fresh temp directory for one test, with symlinks resolved. `toRealPath` matters on macOS
     * where the system temp dir lives under `/var` (a symlink to `/private/var`): build tools write
     * canonical paths into their output, so the sourceroot must be canonical too or path prefix
     * checks fail.
     */
    private fun newTempBase(): Path = Files.createTempDirectory("buildtools").toRealPath()

    /**
     * Materialize a test project from the `fixtures/<name>` directory on the test classpath into
     * [target], overwriting existing files (e.g. a stub `build.gradle` left behind by wrapper
     * generation). When [substitutions] is non-empty, each token is textually replaced in every
     * file, which lets a single fixture cover parametrized variants (e.g. the JDK in a toolchain).
     */
    protected fun copyFixture(
        fixture: String,
        target: Path,
        substitutions: Map<String, String> = emptyMap(),
    ) {
        val resource = "/fixtures/$fixture"
        val url =
            BuildToolHarness::class.java.getResource(resource)
                ?: error("Fixture not found on test classpath: $resource")
        val source = Paths.get(url.toURI())
        Files.walk(source).use { stream ->
            stream.forEach { path ->
                val dest = target.resolve(source.relativize(path).toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest)
                } else {
                    Files.createDirectories(dest.parent)
                    if (substitutions.isEmpty()) {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING)
                    } else {
                        var text = Files.readString(path)
                        for ((token, value) in substitutions) {
                            text = text.replace(token, value)
                        }
                        Files.writeString(dest, text)
                    }
                }
            }
        }
    }

    /** Compare two strings ignoring trailing whitespace. */
    protected fun assertNoDiff(obtained: String, expected: String) {
        if (obtained.trimEnd() != expected.trimEnd()) {
            fail(
                "diff:\n--- obtained ---\n${obtained.trimEnd()}\n" +
                    "--- expected ---\n${expected.trimEnd()}"
            )
        }
    }

    protected fun checkBuild(
        name: String,
        fixture: String,
        expectedScipFiles: Int = 0,
        extraArguments: List<String> = emptyList(),
        expectedError: ((String) -> Unit)? = null,
        expectedPackages: String = "",
        initCommand: (Path) -> List<String> = { emptyList() },
        prepare: (Path) -> Unit = {},
        targetRoot: String? = null,
        maxJdk: Int? = null,
        substitutions: Map<String, String> = emptyMap(),
        runs: Int = 1,
    ): DynamicTest =
        dynamicTest(name) {
            // Some build tools cap the JDK they support; skip rather than fail
            // when the JDK running the test is too new.
            assumeTrue(
                maxJdk == null || currentJavaVersion <= maxJdk,
                "Test $name ignored: JDK $currentJavaVersion exceeds max $maxJdk",
            )

            val base = newTempBase()
            try {
                val workingDirectory = Files.createDirectories(base.resolve("workingDirectory"))
                val cacheDirectory = Files.createDirectories(base.resolve("cache"))

                val init = initCommand(workingDirectory)
                if (init.isNotEmpty()) exec(init, workingDirectory)

                copyFixture(fixture, workingDirectory, substitutions)
                prepare(workingDirectory)

                val targetroot = workingDirectory.resolve(targetRoot ?: "targetroot")
                val arguments =
                    listOf(
                        "index",
                        "--temporary-directory",
                        cacheDirectory.toString(),
                        "--targetroot",
                        targetroot.toString(),
                    ) + extraArguments

                // Later runs reuse the checkout, so they cover state left by
                // earlier builds (up-to-date tasks, build cache entries).
                for (run in 1..runs) {
                    val (exit, output) = runScipJava(workingDirectory, arguments)

                    if (expectedError != null) {
                        assertNotEquals(0, exit, "expected failure, output:\n$output")
                        expectedError(output)
                    } else {
                        assertEquals(0, exit, "expected success, output:\n$output")
                    }

                    val shards = listScipShards(targetroot)
                    if (shards.size != expectedScipFiles) {
                        fail(
                            "Run $run: expected $expectedScipFiles SCIP shard(s), got ${shards.size}: " +
                                "$shards\noutput:\n$output"
                        )
                    }
                }

                if (expectedPackages.isNotEmpty()) {
                    val obtained =
                        ClasspathEntry.fromTargetroot(targetroot, workingDirectory)
                            .map { it.mavenCoordinate() }
                            .sorted()
                            .distinct()
                            .joinToString("\n")
                    assertNoDiff(obtained, expectedPackages)
                }
            } finally {
                base.toFile().deleteRecursively()
            }
        }

    protected fun checkErrorOutput(
        name: String,
        arguments: List<String>,
        expectedOutput: String,
        fixture: String? = null,
    ): DynamicTest =
        dynamicTest(name) {
            val base = newTempBase()
            try {
                val workingDirectory = Files.createDirectories(base.resolve("workingDirectory"))
                if (fixture != null) {
                    copyFixture(fixture, workingDirectory, emptyMap())
                }
                val (exit, rawOutput) = runScipJava(workingDirectory, arguments)
                val output = rawOutput.replace(base.toString(), "")
                assertEquals(1, exit, "output:\n$output")
                assertNoDiff(output, expectedOutput)
            } finally {
                base.toFile().deleteRecursively()
            }
        }

    companion object {
        /** Major version of the JVM running these tests (e.g. 17, 21). */
        private val currentJavaVersion: Int = Runtime.version().feature()

        /** Run an external command, inheriting stdio. Throws if it exits non-zero. */
        internal fun exec(command: List<String>, cwd: Path) {
            val exit = ProcessBuilder(command).directory(cwd.toFile()).inheritIO().start().waitFor()
            check(exit == 0) { "Command failed ($exit): ${command.joinToString(" ")}" }
        }
    }
}
