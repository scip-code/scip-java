package org.scip_code.scip_java.buildtools

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import org.scip_code.scip_java.Embedded
import org.scip_code.scip_java.commands.IndexCommand

class BazelBuildTool(index: IndexCommand) : BuildTool("Bazel", index) {

    override val isHidden: Boolean = true

    override fun usedInCurrentDirectory(): Boolean {
        val cwd = index.workingDirectory
        return listOf("MODULE.bazel", "WORKSPACE", "WORKSPACE.bazel").any {
            Files.isRegularFile(cwd.resolve(it))
        }
    }

    private val targetSpecs: List<String>
        get() = if (index.buildCommand.isEmpty()) listOf("//...") else index.buildCommand

    /**
     * Prefer `bazelisk` over `bazel` when both are available: `bazelisk` respects the project's
     * `.bazelversion`, while `bazel` may be a pinned system version.
     */
    private val bazelExecutable: String
        get() {
            val pathDirs = (System.getenv("PATH") ?: "").split(File.pathSeparator)
            return listOf("bazelisk", "bazel").firstOrNull { name ->
                pathDirs.any { dir -> Files.isExecutable(Paths.get(dir, name)) }
            } ?: "bazel"
        }

    override fun generateScip(): Int {
        val aspectLabel = generateAspectFile() ?: return 1

        val scipJavaBinary = index.bazelScipJavaBinary.orEmpty()
        if (scipJavaBinary.isEmpty()) {
            index.app.error(
                "the flag --bazel-scip-java-binary is required to index Bazel codebases. " +
                    "To fix this problem, run scip-java index again with the flag --scip-java-binary=/path/to/scip-java"
            )
            return 1
        }

        val javaHome = index.app.env.environmentVariables.getOrDefault("JAVA_HOME", "")
        if (javaHome.isEmpty()) {
            index.app.error(
                "environment variable JAVA_HOME is not set. " +
                    "To fix this problem run `export JAVA_HOME=/path/to/java` and run scip-java index again."
            )
            return 1
        }

        val buildCommand =
            listOf(
                bazelExecutable,
                "build",
                "--noshow_progress",
                "--aspects",
                "$aspectLabel%scip_java_aspect",
                "--output_groups=scip",
                "--define=java_home=$javaHome",
                "--define=scip_java_binary=$scipJavaBinary",
                "--verbose_failures",
            ) + targetSpecs

        val buildExitCode = runBazelBuild(buildCommand)
        return if (buildExitCode != 0) buildExitCode
        else {
            aggregateScipFiles()
            0
        }
    }

    private fun runBazelBuild(buildCommand: List<String>): Int {
        val sandbox = SandboxCommandExtractor()
        index.app.info(buildCommand.joinToString(" "))
        val result =
            ProcessRunner.run(
                buildCommand,
                cwd = index.workingDirectory,
                onStdout = { index.app.env.standardOutput.println(it) },
                onStderr = { sandbox.accept(it) },
            )
        if (result.exitCode == 0) return 0
        if (index.bazelAutorunSandboxCommand && sandbox.commandLines().isNotEmpty()) {
            index.app.info("Automatically re-running sandbox command to help debug the problem.")
            ProcessBuilder("bash", "-c", sandbox.commandLines().joinToString("\n"))
                .directory(index.workingDirectory.toFile())
                .inheritIO()
                .start()
                .waitFor()
        }
        index.app.error(
            """To reproduce the failed Bazel command without scip-java, run the following command:

  bazel build ${targetSpecs.joinToString(" ")}

To narrow the set of targets to index or pass additional flags to Bazel, include extra arguments index after -- like below:

  scip-java index --bazel-scip-java-binary=... -- //custom/target --sandbox_debug
"""
        )
        return result.exitCode
    }

    private fun aggregateScipFiles() {
        // Final step after running the aspect: aggregate all the generated
        // `*.scip` files into a single index.scip file. We do this step
        // outside of Bazel because Bazel does not allow actions to generate
        // outputs outside of the bazel-out directory. Ideally we would
        // implement the aggregation step inside Bazel and only copy the
        // resulting index.scip file into the root of the workspace.
        Files.deleteIfExists(index.finalOutput)
        Files.createDirectories(index.finalOutput.parent)
        val scipPattern = FileSystems.getDefault().getPathMatcher("glob:**.scip")
        val bazelOut = index.workingDirectory.resolve("bazel-out")
        if (!Files.exists(bazelOut)) {
            index.app.error(
                "doing nothing, the directory $bazelOut does not exist. " +
                    "The most likely cause for this problem is that there are no Java targets in this Bazel workspace. " +
                    "Please report an issue to the scip-java issue tracker if the command " +
                    "`bazel query 'kind(java_*, //...)'` returns non-empty output."
            )
            return
        }
        val bazelOutLink = Files.readSymbolicLink(bazelOut)
        Files.walkFileTree(
            bazelOutLink,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    // The aspect declares a `<target>.scip-targetroot` directory that
                    // contains the intermediate per-source `*.scip` shards. Those shards
                    // are already aggregated into the sibling `<target>.scip` file, so
                    // including them here would duplicate every document in the index.
                    return if (dir.fileName.toString().endsWith(".scip-targetroot")) {
                        FileVisitResult.SKIP_SUBTREE
                    } else {
                        FileVisitResult.CONTINUE
                    }
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (scipPattern.matches(file)) {
                        val bytes = Files.readAllBytes(file)
                        Files.write(
                            index.finalOutput,
                            bytes,
                            StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE,
                        )
                    }
                    return super.visitFile(file, attrs)
                }
            },
        )
    }

    /**
     * Processes Bazel's stderr line-by-line to extract the sandbox command. When `--sandbox_debug`
     * is enabled Bazel prints the sandbox command that failed but it doesn't show the stdout/stderr
     * of that command. This extractor captures the command so it can be re-run after the fact.
     */
    private inner class SandboxCommandExtractor {
        private var isSandboxCommandPrinting = false
        private val lines = mutableListOf<String>()

        fun commandLines(): List<String> = lines.toList()

        fun accept(line: String) {
            if (
                !isSandboxCommandPrinting &&
                    line.startsWith("ERROR:") &&
                    line.contains("error executing command")
            ) {
                isSandboxCommandPrinting = true
            } else if (isSandboxCommandPrinting && !line.startsWith("  ")) {
                isSandboxCommandPrinting = false
            } else if (isSandboxCommandPrinting) {
                lines += line
            }
            index.app.env.standardError.println(line)
        }
    }

    /**
     * Reads the scip_java.bzl file from resources and writes it to the aspect/scip_java.bzl file
     * inside the Bazel workspace.
     */
    private fun generateAspectFile(): String? {
        val aspectPath = index.workingDirectory.resolve(index.bazelAspect)
        val aspectContents =
            TemporaryFiles.withDirectory(index) { tmp -> Embedded.bazelAspectFile(tmp) }
        if (index.bazelOverwriteAspectFile || !Files.exists(aspectPath)) {
            Files.deleteIfExists(aspectPath)
            Files.createDirectories(aspectPath.parent)
            Files.write(aspectPath, aspectContents.toByteArray(StandardCharsets.UTF_8))
        } else if (Files.isRegularFile(aspectPath)) {
            val existingContents = String(Files.readAllBytes(aspectPath))
            if (existingContents != aspectContents) {
                index.app.reporter.error(
                    "Outdated Bazel aspect file found at $aspectPath. To fix this problem, " +
                        "either run again with the flag --bazel-overwrite-aspect-file or update " +
                        "the contents of the file to the following:\n\n$aspectContents\n\n"
                )
                return null
            }
        } else if (Files.exists(aspectPath)) {
            index.app.reporter.error(
                "path $aspectPath already exists and is not a file. To fix this problem, remove this path and try again."
            )
            return null
        }
        return aspectLabel(aspectPath)
    }

    /** Returns the target name (aka. "label") to reference the given path. */
    private fun aspectLabel(aspectPath: Path): String {
        var parent: Path? = aspectPath.parent
        while (parent != null && parent.startsWith(index.workingDirectory)) {
            if (Files.isRegularFile(parent.resolve("BUILD"))) {
                val path = index.workingDirectory.relativize(parent)
                val name = parent.relativize(aspectPath)
                return "//$path:$name"
            }
            parent = parent.parent
        }
        Files.createFile(aspectPath.resolveSibling("BUILD"))
        return index.workingDirectory.relativize(aspectPath).toString()
    }
}
