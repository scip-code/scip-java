package org.scip_code.scip_java.kotlinc

import java.io.PrintWriter
import java.io.Writer
import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.scip_code.scip.Document
import org.scip_code.scip_java.shared.ScipShardPaths
import org.scip_code.scip_java.shared.ScipShardWriter

/**
 * Writes per-source SCIP shards once the FIR checkers have finished and the IR phase begins.
 *
 * <p>For each source file [AnalyzerCheckers] registered a [ScipVisitor] for, this builds the file's
 * [Document] and serializes it under `<targetRoot>/META-INF/scip/<relative-path>.scip`. Files
 * outside the source root are skipped with a stderr warning.
 */
class PostAnalysisExtension(
    private val configuration: CompilerConfiguration,
    private val sourceRoot: Path,
    private val targetRoot: Path,
    private val callback: (Document) -> Unit,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        try {
            for ((ktSourceFile, visitor) in AnalyzerCheckers.visitors) {
                try {
                    val document = visitor.build()
                    scipShardPathForFile(ktSourceFile)?.let { outPath ->
                        ScipShardWriter.writeShard(outPath, document)
                    }
                    callback(document)
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        } catch (e: Exception) {
            handleException(e)
        }
        AnalyzerCheckers.visitors.clear()
    }

    private fun scipShardPathForFile(file: KtSourceFile): Path? {
        val normalizedPath = Paths.get(file.path).normalize()
        val outPath = ScipShardPaths.shardPath(targetRoot, sourceRoot, normalizedPath)
        if (outPath.isPresent) {
            return outPath.get()
        }
        System.err.println(
            "given file is not under the sourceroot.\n\tSourceroot: $sourceRoot\n\tFile path: ${file.path}\n\tNormalized file path: $normalizedPath"
        )
        return null
    }

    private val messageCollector =
        configuration.get(
            CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false),
        )

    private fun handleException(e: Exception) {
        val writer =
            PrintWriter(
                object : Writer() {
                    val buf = StringBuffer()

                    override fun close() =
                        messageCollector.report(CompilerMessageSeverity.WARNING, buf.toString())

                    override fun flush() = Unit

                    override fun write(data: CharArray, offset: Int, len: Int) {
                        buf.append(data, offset, len)
                    }
                },
                false,
            )
        writer.println("Exception in scip-kotlin compiler plugin:")
        e.printStackTrace(writer)
        writer.println(
            "Please report a bug to https://github.com/scip-code/scip-java with the stack trace above."
        )
        writer.close()
    }
}
