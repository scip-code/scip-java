package org.scip_code.scip_java.kotlinc

import java.nio.file.Path
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.name.FqName
import org.scip_code.scip.Document

/**
 * Per-file accumulator of SCIP occurrences and symbols. The FIR checkers in [AnalyzerCheckers] call
 * into this and the resulting [Document] is written as a `.scip` shard at the end of compilation.
 */
class ScipVisitor(
    sourceroot: Path,
    file: KtSourceFile,
    lineMap: LineMap,
    globals: GlobalSymbolsCache,
    locals: LocalSymbolsCache = LocalSymbolsCache(),
) {
    private val cache = SymbolsCache(globals, locals)
    private val documentBuilder = ScipTextDocumentBuilder(sourceroot, file, lineMap, cache)

    private data class SymbolDescriptorPair(
        val firBasedSymbol: FirBasedSymbol<*>?,
        val symbol: Symbol,
    )

    fun build(): Document = documentBuilder.build()

    context(context: CheckerContext)
    private fun Sequence<SymbolDescriptorPair>?.emitAll(
        element: KtSourceElement,
        isDefinition: Boolean,
        enclosingSource: KtSourceElement? = null,
    ): List<Symbol>? =
        this?.onEach { (firBasedSymbol, symbol) ->
                documentBuilder.emitScipData(
                    firBasedSymbol,
                    symbol,
                    element,
                    isDefinition,
                    enclosingSource,
                )
            }
            ?.map { it.symbol }
            ?.toList()

    private fun Sequence<Symbol>.with(firBasedSymbol: FirBasedSymbol<*>?) =
        this.map { SymbolDescriptorPair(firBasedSymbol, it) }

    context(context: CheckerContext)
    fun visitPackage(pkg: FqName, element: KtSourceElement) {
        cache[pkg].with(null).emitAll(element, isDefinition = false)
    }

    context(context: CheckerContext)
    fun visitClassReference(firClassSymbol: FirClassLikeSymbol<*>, element: KtSourceElement) {
        cache[firClassSymbol].with(firClassSymbol).emitAll(element, isDefinition = false)
    }

    context(context: CheckerContext)
    fun visitCallableReference(firClassSymbol: FirCallableSymbol<*>, element: KtSourceElement) {
        cache[firClassSymbol].with(firClassSymbol).emitAll(element, isDefinition = false)
    }

    context(context: CheckerContext)
    fun visitClassOrObject(
        firClass: FirClassLikeDeclaration,
        element: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firClass.symbol]
            .with(firClass.symbol)
            .emitAll(element, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitPrimaryConstructor(
        firConstructor: FirConstructor,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firConstructor.symbol]
            .with(firConstructor.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitSecondaryConstructor(
        firConstructor: FirConstructor,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firConstructor.symbol]
            .with(firConstructor.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitNamedFunction(
        firFunction: FirFunction,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firFunction.symbol]
            .with(firFunction.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitProperty(
        firProperty: FirProperty,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firProperty.symbol]
            .with(firProperty.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitParameter(
        firParameter: FirValueParameter,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firParameter.symbol]
            .with(firParameter.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitTypeParameter(
        firTypeParameter: FirTypeParameter,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firTypeParameter.symbol]
            .with(firTypeParameter.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitTypeAlias(
        firTypeAlias: FirTypeAlias,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firTypeAlias.symbol]
            .with(firTypeAlias.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitPropertyAccessor(
        firPropertyAccessor: FirPropertyAccessor,
        source: KtSourceElement,
        enclosingSource: KtSourceElement? = null,
    ) {
        cache[firPropertyAccessor.symbol]
            .with(firPropertyAccessor.symbol)
            .emitAll(source, isDefinition = true, enclosingSource)
    }

    context(context: CheckerContext)
    fun visitSimpleNameExpression(
        firResolvedNamedReference: FirResolvedNamedReference,
        source: KtSourceElement,
    ) {
        cache[firResolvedNamedReference.resolvedSymbol]
            .with(firResolvedNamedReference.resolvedSymbol)
            .emitAll(source, isDefinition = false)
    }
}
