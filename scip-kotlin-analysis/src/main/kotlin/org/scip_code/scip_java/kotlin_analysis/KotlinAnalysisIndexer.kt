package org.scip_code.scip_java.kotlin_analysis

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.scip_code.scip.Document
import org.scip_code.scip.Occurrence
import org.scip_code.scip.SymbolRole
import org.scip_code.scip.symbolInformation
import org.scip_code.scip_java.shared.ScipDocumentBuilder
import org.scip_code.scip_java.shared.ScipRange
import org.scip_code.scip_java.shared.ScipShardPaths
import org.scip_code.scip_java.shared.ScipShardWriter

/**
 * Indexes Kotlin sources into SCIP documents using the Kotlin Analysis API in standalone mode.
 *
 * Unlike scip-kotlinc, this indexer does not run inside the user's kotlinc process: it bundles its
 * own analysis engine, so the Kotlin version of the indexed project only matters as a
 * language-version setting, never as a binary compatibility constraint.
 *
 * One SCIP shard is written per source file under
 * `<targetroot>/META-INF/scip/<relative-source-path>.scip`, matching the layout produced by the
 * scip-javac and scip-kotlinc compiler plugins.
 */
class KotlinAnalysisIndexer(
    private val sourceroot: Path,
    private val targetroot: Path,
    private val sourceRoots: List<Path>,
    private val classpath: List<Path> = emptyList(),
    private val jdkHome: Path? = Paths.get(System.getProperty("java.home")),
    private val callback: (Document) -> Unit = {},
) {
    fun run(): List<Document> {
        val disposable = Disposer.newDisposable("scip-kotlin-analysis")
        try {
            val session =
                buildStandaloneAnalysisAPISession(projectDisposable = disposable) {
                    buildKtModuleProvider {
                        platform = JvmPlatforms.defaultJvmPlatform
                        val dependencies = buildList {
                            jdkHome?.let { home ->
                                add(
                                    buildKtSdkModule {
                                        platform = JvmPlatforms.defaultJvmPlatform
                                        addBinaryRootsFromJdkHome(home, isJre = false)
                                        libraryName = "JDK"
                                    }
                                )
                            }
                            if (classpath.isNotEmpty()) {
                                add(
                                    buildKtLibraryModule {
                                        platform = JvmPlatforms.defaultJvmPlatform
                                        addBinaryRoots(classpath)
                                        libraryName = "classpath"
                                    }
                                )
                            }
                        }
                        addModule(
                            buildKtSourceModule {
                                platform = JvmPlatforms.defaultJvmPlatform
                                moduleName = "scip-kotlin-analysis"
                                addSourceRoots(sourceRoots)
                                dependencies.forEach { addRegularDependency(it) }
                            }
                        )
                    }
                }

            val documents = mutableListOf<Document>()
            val ktFiles = session.modulesWithFiles.values.flatten().filterIsInstance<KtFile>()
            for (ktFile in ktFiles) {
                val document = indexFile(ktFile)
                writeShard(ktFile, document)
                callback(document)
                documents.add(document)
            }
            return documents
        } finally {
            Disposer.dispose(disposable)
        }
    }

    private fun indexFile(ktFile: KtFile): Document {
        val cache = SymbolsCache()
        val text = ktFile.text
        val lines = LineIndex(text)
        val builder = ScipDocumentBuilder()
        analyze(ktFile) { ktFile.accept(IndexingVisitor(this, cache, lines, builder)) }
        val relativePath =
            ScipShardPaths.relativePath(sourceroot, Paths.get(ktFile.virtualFilePath))
        return builder.build("kotlin", relativePath, text)
    }

    private fun writeShard(ktFile: KtFile, document: Document) {
        val absolutePath = Paths.get(ktFile.virtualFilePath).normalize()
        val shardPath = ScipShardPaths.shardPath(targetroot, sourceroot, absolutePath)
        if (shardPath.isPresent) {
            ScipShardWriter.writeShard(shardPath.get(), document)
        } else {
            System.err.println(
                "given file is not under the sourceroot.\n\tSourceroot: $sourceroot\n\tFile path: $absolutePath"
            )
        }
    }

    private class IndexingVisitor(
        private val session: KaSession,
        private val cache: SymbolsCache,
        private val lines: LineIndex,
        private val out: ScipDocumentBuilder,
    ) : KtTreeVisitorVoid() {

        override fun visitDeclaration(dcl: KtDeclaration) {
            super.visitDeclaration(dcl)
            // Parameters that don't belong to a declaration (function types, catch
            // clauses, for loops) are not emitted as definitions; references to the
            // latter two still produce local symbols on first use.
            if (dcl is KtParameter && !cache.isDeclarationParameter(dcl)) return
            val range = definitionRange(dcl) ?: return
            val symbol = cache.symbolForDeclaration(dcl)
            if (symbol == Symbol.NONE) return
            addOccurrence(symbol, range, definition = true)
            out.addSymbol(
                symbolInformation {
                    this.symbol = symbol.toString()
                    this.displayName = displayName(dcl)
                }
            )
        }

        override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
            super.visitSimpleNameExpression(expression)
            if (expression.parents.any { it is KtImportDirective }) return
            val target = with(session) { expression.mainReference.resolveToSymbol() } ?: return
            val symbol = cache.symbolForReference(target)
            if (symbol == Symbol.NONE) return
            addOccurrence(
                symbol,
                expression.getReferencedNameElement().textRange,
                definition = false,
            )
        }

        private fun definitionRange(declaration: KtDeclaration): TextRange? =
            when (declaration) {
                // A primary constructor without the `constructor` keyword is linked
                // to the class identifier, matching the FIR indexer.
                is KtPrimaryConstructor ->
                    declaration.getConstructorKeyword()?.textRange
                        ?: declaration.containingClassOrObject?.nameIdentifier?.textRange
                is KtSecondaryConstructor -> declaration.getConstructorKeyword().textRange
                is KtPropertyAccessor -> declaration.namePlaceholder.textRange
                is KtNamedDeclaration -> declaration.nameIdentifier?.textRange
                else -> null
            }

        private fun displayName(declaration: KtDeclaration): String =
            when (declaration) {
                is KtPropertyAccessor -> declaration.property.name.orEmpty()
                is KtConstructor<*> -> "<init>"
                is KtNamedDeclaration -> declaration.name.orEmpty()
                else -> ""
            }

        private fun addOccurrence(symbol: Symbol, range: TextRange, definition: Boolean) {
            val scipRange = lines.scipRange(range) ?: return
            val builder = Occurrence.newBuilder().setSymbol(symbol.toString())
            if (definition) {
                builder.setSymbolRoles(SymbolRole.Definition.number)
            }
            if (scipRange.isSingleLine) {
                builder.setSingleLineRange(scipRange.toSingleLineRange())
            } else {
                builder.setMultiLineRange(scipRange.toMultiLineRange())
            }
            out.addOccurrence(builder.build())
        }
    }
}

/** Maps text offsets to 0-based line/character positions. */
internal class LineIndex(private val text: String) {
    private val lineStartOffsets: IntArray

    init {
        val offsets = ArrayList<Int>()
        offsets.add(0)
        for (index in text.indices) {
            if (text[index] == '\n') offsets.add(index + 1)
        }
        lineStartOffsets = offsets.toIntArray()
    }

    fun scipRange(range: TextRange): ScipRange? {
        if (range.startOffset > text.length || range.endOffset > text.length) return null
        val (startLine, startCharacter) = position(range.startOffset)
        val (endLine, endCharacter) = position(range.endOffset)
        return ScipRange.range(startLine, startCharacter, endLine, endCharacter)
    }

    private fun position(offset: Int): Pair<Int, Int> {
        var line = java.util.Arrays.binarySearch(lineStartOffsets, offset)
        if (line < 0) line = -line - 2
        return line to (offset - lineStartOffsets[line])
    }
}
