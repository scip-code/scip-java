package org.scip_code.scip_java.kotlinc

import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.directOverriddenSymbolsSafe
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.analysis.getChild
import org.jetbrains.kotlin.fir.renderer.*
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.impl.FirImplicitAnyTypeRef
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.text
import org.scip_code.scip.Document
import org.scip_code.scip.Occurrence
import org.scip_code.scip.SymbolInformation
import org.scip_code.scip.SymbolRole
import org.scip_code.scip.relationship
import org.scip_code.scip.signature
import org.scip_code.scip.symbolInformation
import org.scip_code.scip_java.shared.ScipDocumentBuilder
import org.scip_code.scip_java.shared.ScipRange
import org.scip_code.scip_java.shared.ScipShardPaths

/** Builds a SCIP [Document] for a single Kotlin source file. */
class ScipTextDocumentBuilder(
    private val sourceroot: Path,
    private val file: KtSourceFile,
    private val lineMap: LineMap,
    private val cache: SymbolsCache,
) {
    private val documentBuilder = ScipDocumentBuilder()
    private val fileText = file.getContentsAsStream().reader().readText()

    fun build(): Document = documentBuilder.build("kotlin", relativePath(), fileText)

    context(context: CheckerContext)
    fun emitScipData(
        firBasedSymbol: FirBasedSymbol<*>?,
        symbol: Symbol,
        element: KtSourceElement,
        isDefinition: Boolean,
        enclosingSource: KtSourceElement? = null,
    ) {
        documentBuilder.addOccurrence(occurrence(symbol, element, isDefinition, enclosingSource))
        if (isDefinition) {
            documentBuilder.addSymbol(symbolInformation(firBasedSymbol, symbol, element))
        }
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext)
    private fun symbolInformation(
        firBasedSymbol: FirBasedSymbol<*>?,
        symbol: Symbol,
        element: KtSourceElement,
    ): SymbolInformation {
        val supers =
            when (firBasedSymbol) {
                is FirClassSymbol ->
                    firBasedSymbol.resolvedSuperTypeRefs
                        .filter { it !is FirImplicitAnyTypeRef }
                        .mapNotNull { it.toClassLikeSymbol(firBasedSymbol.moduleData.session) }
                        .flatMap { cache[it] }
                is FirFunctionSymbol<*> ->
                    firBasedSymbol.directOverriddenSymbolsSafe().flatMap { cache[it] }
                else -> emptyList()
            }
        return symbolInformation {
            this.symbol = symbol.toString()
            this.displayName =
                if (firBasedSymbol != null) displayName(firBasedSymbol) else element.text.toString()
            if (firBasedSymbol != null) {
                renderSignature(firBasedSymbol.fir)?.let { rendered ->
                    signatureDocumentation = signature {
                        language = "kotlin"
                        text = rendered
                    }
                }
                docComment(firBasedSymbol.fir)?.let { documentation += it }
            }
            for (parent in supers) {
                relationships += relationship {
                    this.symbol = parent.toString()
                    isImplementation = true
                }
            }
        }
    }

    private fun occurrence(
        symbol: Symbol,
        element: KtSourceElement,
        isDefinition: Boolean,
        enclosingSource: KtSourceElement?,
    ): Occurrence {
        val builder = Occurrence.newBuilder().setSymbol(symbol.toString())
        if (isDefinition) builder.setSymbolRoles(SymbolRole.Definition.number)
        val range = range(element)
        if (range.isSingleLine) builder.singleLineRange = range.toSingleLineRange()
        else builder.multiLineRange = range.toMultiLineRange()
        if (enclosingSource != null) {
            val enclosingRange = enclosingRange(enclosingSource)
            if (enclosingRange.isSingleLine) {
                builder.singleLineEnclosingRange = enclosingRange.toSingleLineRange()
            } else {
                builder.multiLineEnclosingRange = enclosingRange.toMultiLineRange()
            }
        }
        return builder.build()
    }

    private fun range(element: KtSourceElement): ScipRange {
        val line = lineMap.lineNumber(element) - 1
        val startCol = lineMap.startCharacter(element)
        val endCol = lineMap.endCharacter(element)
        return ScipRange.singleLine(line, startCol, endCol)
    }

    private fun enclosingRange(element: KtSourceElement): ScipRange {
        val startLine = lineMap.lineNumber(element) - 1
        val startCol = lineMap.startCharacter(element)
        val endLine = lineMap.lineNumberForOffset(element.endOffset) - 1
        val endCol = lineMap.columnForOffset(element.endOffset)
        return ScipRange.range(startLine, startCol, endLine, endCol)
    }

    private fun relativePath(): String =
        ScipShardPaths.relativePath(sourceroot, Paths.get(file.path))

    /**
     * Renders [element] as a Kotlin signature using [FirRenderer]'s readability preset, with kdoc
     * stripped (kdoc is exposed separately via [SymbolInformation.documentation]).
     */
    private fun renderSignature(element: FirElement): String? {
        val renderer =
            FirRenderer(
                typeRenderer = ConeTypeRenderer(),
                idRenderer = ConeIdShortRenderer(),
                classMemberRenderer = FirNoClassMemberRenderer(),
                bodyRenderer = null,
                propertyAccessorRenderer = null,
                callArgumentsRenderer = FirCallNoArgumentsRenderer(),
                modifierRenderer = FirAllModifierRenderer(),
                callableSignatureRenderer = FirCallableSignatureRendererForReadability(),
                declarationRenderer = FirDeclarationRenderer("local "),
            )
        val rendered = renderer.renderElementAsString(element)
        return if (rendered.isEmpty()) null else rendered
    }

    private fun docComment(element: FirElement): String? {
        val kdoc = element.source?.getChild(KtTokens.DOC_COMMENT)?.text?.toString() ?: return null
        return stripKdoc(kdoc).ifEmpty { null }
    }

    /** Strips the `/**`, leading `*`s, and `*/` from a kdoc block, returning just the body text. */
    private fun stripKdoc(kdoc: String): String {
        if (kdoc.isEmpty()) return kdoc
        val out = StringBuilder()
        var first = true
        kdoc.lineSequence().forEach { line ->
            if (line.isEmpty()) return@forEach
            var start = 0
            while (start < line.length && line[start].isWhitespace()) start++
            if (start < line.length && line[start] == '/') start++
            while (start < line.length && line[start] == '*') start++
            var end = line.length - 1
            if (end > start && line[end] == '/') end--
            while (end > start && line[end] == '*') end--
            while (end > start && line[end].isWhitespace()) end--
            start = minOf(start, line.length - 1)
            if (end > start) end++
            if (!first) out.append('\n')
            out.append(line, start, end)
            first = false
        }
        return out.toString().trim()
    }

    companion object {
        @OptIn(SymbolInternals::class, RenderingInternals::class)
        private fun displayName(firBasedSymbol: FirBasedSymbol<*>): String =
            when (firBasedSymbol) {
                is FirClassSymbol -> firBasedSymbol.classId.shortClassName.asString()
                is FirPropertyAccessorSymbol -> firBasedSymbol.fir.propertySymbol.name.asString()
                is FirFunctionSymbol -> firBasedSymbol.callableId.callableName.asString()
                is FirPropertySymbol ->
                    firBasedSymbol.callableIdForRendering.callableName.asString()
                is FirVariableSymbol -> firBasedSymbol.name.asString()
                else -> firBasedSymbol.toString()
            }
    }
}
