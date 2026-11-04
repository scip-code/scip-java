package org.scip_code.scip_java

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties
import org.scip_code.scip_java.buildtools.ProcessResult

object Embedded {

    val javacLauncherJvmOptions: List<String> by lazy {
        val properties = Properties()
        val input =
            Embedded::class.java.getResourceAsStream("/javac-internals.properties")
                ?: error("missing embedded resource: /javac-internals.properties")
        input.use { properties.load(it) }
        val jvmOptions =
            properties.getProperty("javac.jvmOptions")
                ?: error("missing javac.jvmOptions in /javac-internals.properties")
        jvmOptions.split(',').map { it.trim() }.filter { it.isNotEmpty() }.map { "-J$it" }
    }

    fun scipJar(tmpDir: Path): Path = copyFile(tmpDir, "scip-plugin.jar")

    fun gradlePluginJar(tmpDir: Path): Path = copyFile(tmpDir, "gradle-plugin.jar")

    private fun javacErrorpath(tmp: Path): Path = tmp.resolve("errorpath.txt")

    data class CustomJavac(val executable: Path, val environment: Map<String, String>)

    fun customJavac(sourceroot: Path, targetroot: Path, tmp: Path): CustomJavac {
        val bin = tmp.resolve("bin")
        val javac = bin.resolve("javac")
        val java = bin.resolve("java")
        val pluginpath = scipJar(tmp)
        val errorpath = javacErrorpath(tmp)
        val javacopts = targetroot.resolve("javacopts.txt")
        Files.createDirectories(targetroot)
        Files.createDirectories(bin)
        copyResource(java, "scip-java/java-forwarder.sh")
        copyResource(javac, "scip-java/custom-javac.sh")
        javac.toFile().setExecutable(true)
        java.toFile().setExecutable(true)
        return CustomJavac(
            javac,
            mapOf(
                "SCIP_ERRORPATH" to errorpath.toString(),
                "SCIP_JAVAC_LAUNCHER_JVM_OPTIONS" to javacLauncherJvmOptions.joinToString("\n"),
                "SCIP_JAVAC_OPTIONS_PREFIX" to tmp.resolve("javac_newarguments").toString(),
                "SCIP_OLD_JAVAC_OPTS" to javacopts.toString(),
                "SCIP_PLUGINPATH" to pluginpath.toString(),
                "SCIP_SOURCEROOT" to sourceroot.toString(),
                "SCIP_TARGETROOT" to targetroot.toString(),
            ),
        )
    }

    /**
     * The custom javac wrapper reports errors to a specific file if unexpected errors happen. The
     * javac wrapper gets invoked by builds tools like Gradle/Maven, which hide the actual errors
     * from the script because they assume the standard output is from javac. This file is used a
     * side-channel to avoid relying on the error reporting from Gradle/Maven.
     */
    fun reportUnexpectedJavacErrors(reporter: CliReporter, tmp: Path): ProcessResult? {
        val errorpath = javacErrorpath(tmp)
        if (!Files.isRegularFile(errorpath)) return null
        reporter.error("unexpected javac compile errors")
        Files.readAllLines(errorpath).forEach { reporter.error(it) }
        return ProcessResult(1)
    }

    /** Returns the string contents of the scip_java.bzl file on disk. */
    fun bazelAspectFile(tmpDir: Path): String {
        val tmpFile = copyFile(tmpDir, "scip-java/scip_java.bzl")
        val contents = String(Files.readAllBytes(tmpFile), StandardCharsets.UTF_8)
        Files.deleteIfExists(tmpFile)
        return contents
    }

    private fun copyFile(tmpDir: Path, filename: String): Path {
        val out = tmpDir.resolve(filename)
        copyResource(out, filename)
        return out
    }

    private fun copyResource(out: Path, resource: String) {
        val input =
            Embedded::class.java.getResourceAsStream("/$resource")
                ?: error("missing embedded resource: /$resource")
        Files.createDirectories(out.parent)
        input.use { Files.copy(it, out, StandardCopyOption.REPLACE_EXISTING) }
    }
}
