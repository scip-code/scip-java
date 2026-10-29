package org.scip_code.scip_java.buildtools

import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import org.scip_code.scip_java.Embedded
import org.scip_code.scip_java.commands.IndexCommand
import org.scip_code.scip_java.kotlin_analysis.KotlinAnalysisIndexer

/**
 * A custom build tool that is specifically made for scip-java.
 *
 * The purpose of this build tool is to SCIP-index source code defined by an external JSON config
 * (`scip-java.json`) instead of by Maven/Gradle/Bazel. Callers are expected to pre-resolve
 * dependencies and pass the resulting classpath via the `classpath` field. `scip-java` does not
 * fetch anything from the network.
 */
class ScipBuildTool(index: IndexCommand) : BuildTool("SCIP", index) {

    private val javaPattern = FileSystems.getDefault().getPathMatcher("glob:**.java")
    private val kotlinPattern = FileSystems.getDefault().getPathMatcher("glob:**.kt")
    private val allPatterns = FileSystems.getDefault().getPathMatcher("glob:**.{java,kt}")
    private val moduleInfo: Path = Paths.get("module-info.java")

    override fun usedInCurrentDirectory(): Boolean = configFiles().any { Files.isRegularFile(it) }

    override val isHidden: Boolean = true

    override fun generateScip(): Int {
        return generateScipFromTargetroot(
            runBuild(),
            index.finalTargetroot(defaultTargetroot),
            index,
        )
    }

    private val targetroot: Path
        get() = index.finalTargetroot(defaultTargetroot)

    private val defaultTargetroot: Path = Paths.get("target")

    private fun configFiles(): List<Path> {
        val list = ArrayList<Path>()
        index.scipConfig?.let { list.add(it) }
        ConfigFileNames.forEach { list.add(index.workingDirectory.resolve(it)) }
        return list
    }

    private fun runBuild(): ProcessResult {
        val config =
            try {
                parseConfig()
            } catch (e: Exception) {
                index.app.error(e.message ?: e.toString())
                return ProcessResult(1)
            }
        if (index.cleanup) clean()
        return try {
            compile(config)
        } catch (e: Exception) {
            e.printStackTrace(index.app.env.standardOutput)
            ProcessResult(1)
        }
    }

    /** Parses the lsif-java.json file into a Config object. */
    private fun parseConfig(): Config {
        val configFile =
            configFiles().firstOrNull { Files.isRegularFile(it) }
                ?: throw IOException(
                    "no config file found. To fix this problem, create a config file in the path '${configFiles().first()}'"
                )
        val raw = Files.readString(configFile)
        val element = Json.parseToJsonElement(raw)
        if (element !is JsonObject) {
            throw IOException("expected JSON object at $configFile, got $element")
        }
        return Config.fromJson(element)
    }

    /**
     * Shells out to "javac" (and runs `kotlinc` in-process) to compile the sources with the SCIP
     * compiler plugin enabled.
     */
    private fun compile(config: Config): ProcessResult {
        if (config.dependencies.isNotEmpty()) {
            index.app.error(
                "scip-java no longer resolves Maven coordinates from the 'dependencies' field " +
                    "of scip-java.json. Pre-resolve dependencies and populate the 'classpath' " +
                    "field with absolute JAR paths instead."
            )
            return ProcessResult(1)
        }
        val tmp = Files.createTempDirectory("scip-java")
        Files.createDirectories(targetroot)
        val sourceroot = index.workingDirectory
        if (!Files.isDirectory(sourceroot)) throw NoSuchFileException(sourceroot.toString())
        val allSourceFiles = collectAllSourceFiles(config, sourceroot)
        val javaFiles = allSourceFiles.filter { javaPattern.matches(it) }
        val kotlinFiles = allSourceFiles.filter { kotlinPattern.matches(it) }
        if (javaFiles.isEmpty() && kotlinFiles.isEmpty()) {
            if (config.reportWarningOnEmptyIndex) {
                index.app.warning(
                    "doing nothing, no files matching pattern '$sourceroot/**.{java,kt}'"
                )
            }
            return ProcessResult(0)
        }

        val errors = mutableListOf<Throwable>()
        compileJavaFiles(tmp, config, javaFiles)?.let { errors += it }
        indexKotlinFiles(config, kotlinFiles)?.let { errors += it }

        if (index.cleanup) {
            tmp.toFile().deleteRecursively()
        }
        val isScipGenerated = Files.isDirectory(targetroot.resolve("META-INF"))
        return if (errors.isNotEmpty() && (index.strictCompilation || !isScipGenerated)) {
            for (error in errors) index.app.reporter.error(error)
            ProcessResult(1)
        } else {
            if (errors.isNotEmpty() && isScipGenerated) {
                index.app.reporter.info(
                    "Some SCIP files got generated even if there were compile errors. " +
                        "In most cases, this means that scip-java managed to index everything except " +
                        "the locations that had compile errors and you can ignore the compile errors."
                )
                for (error in errors) {
                    index.app.reporter.info(error.message ?: error.toString())
                }
            }
            ProcessResult(0)
        }
    }

    private fun indexKotlinFiles(config: Config, allKotlinFiles: List<Path>): Throwable? {
        if (allKotlinFiles.isEmpty()) return null
        val sourceroot = index.workingDirectory
        val classpath = config.classpath.map { sourceroot.resolve(it).normalize() }
        val jdkHome =
            config.javaHome?.let { Paths.get(it) } ?: Paths.get(System.getProperty("java.home"))
        // The Analysis API indexer does not compile the sources: unresolved code
        // degrades individual occurrences instead of failing the build, so only
        // indexer crashes surface as errors.
        return try {
            KotlinAnalysisIndexer(
                    sourceroot = sourceroot,
                    targetroot = targetroot,
                    sourceRoots = allKotlinFiles,
                    classpath = classpath,
                    jdkHome = jdkHome,
                )
                .run()
            null
        } catch (e: Exception) {
            e
        }
    }

    private fun compileJavaFiles(tmp: Path, config: Config, allJavaFiles: List<Path>): Throwable? {
        val (moduleInfos, javaFiles) = allJavaFiles.partition { it.endsWith(moduleInfo) }
        if (javaFiles.isEmpty()) return null
        val sourceroot = index.workingDirectory
        val scipJar = Embedded.scipJar(tmp)
        val classpath = mutableListOf<String>()
        classpath += scipJar.toString()
        classpath += config.classpath.map { index.workingDirectory.resolve(it).toString() }
        val argsfile = targetroot.resolve("javacopts.txt")
        val arguments = mutableListOf<String>()
        arguments += "-encoding"
        arguments += "utf8"
        arguments += "-nowarn"
        arguments += "-d"
        arguments += generatedDir(tmp, "d")
        arguments += "-s"
        arguments += generatedDir(tmp, "s")
        arguments += "-h"
        arguments += generatedDir(tmp, "h")
        arguments += "-classpath"
        arguments += classpath.joinToString(File.pathSeparator)
        arguments += "-Xplugin:scip -targetroot:$targetroot -sourceroot:$sourceroot"
        if (config.processorpath.isNotEmpty()) {
            arguments += "-processorpath"
            val processorpath =
                listOf(scipJar.toString()) +
                    config.processorpath.mapNotNull {
                        guessBazelJar(it, index.workingDirectory)?.toString()
                    }
            arguments += processorpath.joinToString(File.pathSeparator)
        }
        val isIgnoredAnnotationProcessor =
            isIgnoredAnnotationProcessor + index.scipIgnoredAnnotationProcessors.toSet()
        val processors = config.processors.filterNot { it in isIgnoredAnnotationProcessor }
        if (processors.isNotEmpty()) {
            arguments += "-processor"
            arguments += processors.joinToString(",")
        }
        arguments += fixJavacOptions(config.javacOptions)
        if (config.kind == "jdk" && moduleInfos.isNotEmpty()) {
            for (module in moduleInfos) {
                arguments += "--module"
                arguments += module.parent.fileName.toString()
            }
            arguments += "--module-source-path"
            arguments += sourceroot.toString()
        } else {
            arguments += javaFiles.map { it.toString() }
        }
        val quotedArguments = arguments.map { "\"$it\"" }
        Files.write(argsfile, quotedArguments)
        if (javaFiles.size > 1) {
            index.app.reporter.info(String.format("Compiling %,d Java sources", javaFiles.size))
        }
        val javac = javacPath(config)
        index.app.reporter.info("$ $javac @$argsfile")
        val jvmOptions = config.jvmOptions.map { "-J$it" }

        val cmd = mutableListOf<String>()
        cmd += javac.toString()
        cmd += "@$argsfile"
        cmd += Embedded.javacLauncherJvmOptions
        cmd += jvmOptions
        val result =
            ProcessRunner.run(
                cmd,
                cwd = sourceroot,
                onStdout = { index.app.reporter.info(it) },
                onStderr = { index.app.reporter.info(it) },
            )
        return if (result.exitCode == 0) null
        else Exception("javac exited with code ${result.exitCode}")
    }

    private fun fixJavacOptions(options: List<String>): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < options.size) {
            val option = options[i]
            when {
                option == "--release" -> {
                    // Skip --release because it's not strictly needed for indexing,
                    // and it fails the build if -source/-target are also provided.
                    if (i + 1 < options.size) i++ // drop the value
                }
                option.startsWith("-Xep") || // ErrorProne flag, which fails the build
                    option.startsWith("-Xplugin:scip") || // Redundant SCIP
                    option.startsWith("-XD") || // unsure what this one does
                    index.scipIgnoredJavacOptionPrefixes.any { option.startsWith(it) } -> {
                    // skip
                }
                else -> out += option
            }
            i++
        }
        return out
    }

    private fun javacPath(config: Config): Path {
        val home =
            config.javaHome
                ?: index.app.env.environmentVariables["JAVA_HOME"]
                ?: throw RuntimeException(
                    "scip-java requires either the 'javaHome' field in scip-java.json or the " +
                        "JAVA_HOME environment variable to be set to a JDK installation."
                )
        return Paths.get(home, "bin", "javac")
    }

    private fun clean() {
        targetroot.toFile().deleteRecursively()
    }

    private fun collectAllSourceFiles(dir: Path): List<Path> {
        val out = ArrayList<Path>()
        Files.walkFileTree(
            dir,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    d: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult =
                    if (d == targetroot) FileVisitResult.SKIP_SUBTREE else FileVisitResult.CONTINUE

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (allPatterns.matches(file)) out.add(file)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult =
                    FileVisitResult.CONTINUE
            },
        )
        return out
    }

    private fun collectAllSourceFiles(config: Config, dir: Path): List<Path> {
        return if (config.sourceFiles.isNotEmpty()) {
            config.sourceFiles.flatMap { relativePath ->
                val path = dir.resolve(relativePath)
                when {
                    Files.isRegularFile(path) && allPatterns.matches(path) -> listOf(path)
                    Files.isDirectory(path) -> collectAllSourceFiles(path)
                    else -> emptyList()
                }
            }
        } else {
            collectAllSourceFiles(dir)
        }
    }

    /**
     * HACK: We haven't figured out a reliable way to get annotation processor jars on the
     * processorpath. The Bazel aspect sometimes says a NAME.jar file is on the processorpath when
     * it doesn't exist but processed_NAME.jar or header_NAME.jar exists.
     */
    private fun guessBazelJar(pathString: String, workingDirectory: Path): Path? {
        var path = workingDirectory.resolve(pathString)
        if (Files.isRegularFile(path)) return path

        if (!pathString.startsWith("bazel-bin") && !pathString.startsWith("bazel-out")) {
            path = workingDirectory.resolve(Paths.get("bazel-bin", pathString))
            if (Files.isRegularFile(path)) return path
        }

        val processed = path.resolveSibling("processed_${path.fileName}")
        if (Files.isRegularFile(processed)) return processed

        val header = path.resolveSibling("header_${path.fileName}")
        if (Files.isRegularFile(header)) return header

        index.app.warning("annotation processor jar does not exist: $path")
        return null
    }

    private fun generatedDir(tmp: Path, name: String): String =
        Files.createDirectory(tmp.resolve(name)).toString()

    /** Configuration parsed from `scip-java.json` / `lsif-java.json`. */
    private data class Config(
        val reportWarningOnEmptyIndex: Boolean = true,
        val javaHome: String? = null,
        val dependencies: List<String> = emptyList(),
        val sourceFiles: List<String> = emptyList(),
        val classpath: List<String> = emptyList(),
        val processorpath: List<String> = emptyList(),
        val processors: List<String> = emptyList(),
        val javacOptions: List<String> = emptyList(),
        val jvmOptions: List<String> = emptyList(),
        val kind: String = "",
    ) {
        companion object {
            fun fromJson(obj: JsonObject): Config {
                val defaults = Config()
                return Config(
                    reportWarningOnEmptyIndex =
                        obj.boolean(
                            "reportWarningOnEmptyIndex",
                            defaults.reportWarningOnEmptyIndex,
                        ),
                    javaHome = obj["javaHome"]?.stringOrNull(),
                    dependencies = obj.stringList("dependencies"),
                    sourceFiles = obj.stringList("sourceFiles"),
                    classpath = obj.stringList("classpath"),
                    processorpath = obj.stringList("processorpath"),
                    processors = obj.stringList("processors"),
                    javacOptions = obj.stringList("javacOptions"),
                    jvmOptions = obj.stringList("jvmOptions"),
                    kind = obj["kind"]?.stringOrNull() ?: defaults.kind,
                )
            }

            private fun JsonObject.boolean(key: String, default: Boolean): Boolean {
                val v = this[key] as? JsonPrimitive ?: return default
                return v.boolean
            }

            private fun JsonObject.stringList(key: String): List<String> {
                val v = this[key] ?: return emptyList()
                return v.jsonArray.mapNotNull {
                    when (it) {
                        is JsonPrimitive -> it.contentOrNull
                        is JsonObject -> {
                            // The dependencies field used to accept either a "g:a:v"
                            // string or an object form. We don't support either now;
                            // just stringify so the caller can detect it.
                            it.toString()
                        }
                        else -> null
                    }
                }
            }

            private fun JsonElement.stringOrNull(): String? =
                (this as? JsonPrimitive)?.contentOrNull
        }
    }

    companion object {
        // This file is named "lsif-java.json" instead of "scip-java.json" in
        // order to preserve compatibility with legacy "JVM dependencies" repos.
        // If we rename to "scip-java.json" then the git commit SHAs of these repos
        // change and old canonical URLs will become 404 links.
        val ConfigFileNames = listOf("scip-java.json", "lsif-java.json")
        val isIgnoredAnnotationProcessor: Set<String> =
            setOf("org.openjdk.jmh.generators.BenchmarkProcessor")
    }
}
