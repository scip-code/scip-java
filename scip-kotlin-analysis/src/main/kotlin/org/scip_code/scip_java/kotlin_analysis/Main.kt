package org.scip_code.scip_java.kotlin_analysis

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Command-line entry point, primarily used by the scip-snapshots harness. The scip-java CLI calls
 * [KotlinAnalysisIndexer] in-process instead.
 *
 * Usage: `--sourceroot <dir> --targetroot <dir> [--classpath <path-separated jars>] [--jdk-home
 * <dir>] <source file or directory>...`
 */
fun main(args: Array<String>) {
    var sourceroot: Path? = null
    var targetroot: Path? = null
    var classpath: List<Path> = emptyList()
    var jdkHome: Path? = Paths.get(System.getProperty("java.home"))
    val sourceRoots = mutableListOf<Path>()
    var i = 0
    while (i < args.size) {
        when (val arg = args[i]) {
            "--sourceroot" -> sourceroot = Paths.get(args[++i])
            "--targetroot" -> targetroot = Paths.get(args[++i])
            "--classpath" ->
                classpath =
                    args[++i]
                        .split(File.pathSeparator)
                        .filter { it.isNotEmpty() }
                        .map { Paths.get(it) }
            "--jdk-home" -> jdkHome = Paths.get(args[++i])
            else -> sourceRoots.add(Paths.get(arg))
        }
        i++
    }
    require(sourceroot != null) { "missing required flag: --sourceroot" }
    require(targetroot != null) { "missing required flag: --targetroot" }
    require(sourceRoots.isNotEmpty()) { "missing source files or directories" }
    KotlinAnalysisIndexer(
            sourceroot = sourceroot,
            targetroot = targetroot,
            sourceRoots = sourceRoots,
            classpath = classpath,
            jdkHome = jdkHome,
        )
        .run()
    // The Analysis API environment leaves non-daemon threads behind; exit
    // explicitly so callers are not left waiting on them.
    exitProcess(0)
}
