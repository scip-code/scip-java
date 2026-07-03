package org.scip_code.scip_java.kotlinc

import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

const val VAL_SOURCES = "sourceroot"
val KEY_SOURCES = CompilerConfigurationKey<Path>(VAL_SOURCES)

const val VAL_TARGET = "targetroot"
val KEY_TARGET = CompilerConfigurationKey<Path>(VAL_TARGET)

const val PLUGIN_ID = "scip-kotlinc"

@OptIn(ExperimentalCompilerApi::class)
class AnalyzerCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(
                VAL_SOURCES,
                "<path>",
                "the absolute path to the root of the Kotlin sources",
                required = true,
            ),
            CliOption(
                VAL_TARGET,
                "<path>",
                "the absolute path to the directory where to generate SCIP files.",
                required = true,
            ),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            VAL_SOURCES -> configuration.put(KEY_SOURCES, Paths.get(value))
            VAL_TARGET -> configuration.put(KEY_TARGET, Paths.get(value))
        }
    }
}
