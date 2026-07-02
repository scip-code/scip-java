package org.scip_code.scip_java

import java.io.PrintStream
import kotlin.system.exitProcess

/**
 * Public entry point for the scip-java CLI. The single [app] instance is shared across test suites
 * and reset between invocations.
 */
object ScipJava {

    @JvmField val app: ScipJavaApp = ScipJavaApp()

    val version: String by lazy {
        ScipJava::class.java.`package`?.implementationVersion ?: "0.0.0-SNAPSHOT"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        app.runAndExitIfNonZero(args.toList())
        // The Analysis API indexer leaves non-daemon threads behind; exit explicitly.
        exitProcess(0)
    }

    fun printHelp(out: PrintStream) {
        out.println("```text")
        out.println("$ scip-java index --help")
        val replacement =
            ScipJavaApp().apply { env = env.withStandardOutput(out).withStandardError(out) }
        replacement.run(listOf("index", "--help"))
        out.println("```")
    }
}
