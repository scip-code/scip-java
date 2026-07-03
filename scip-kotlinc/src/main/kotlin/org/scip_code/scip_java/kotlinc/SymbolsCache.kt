package org.scip_code.scip_java.kotlinc

import java.lang.System.err
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.isLocalMember
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingSymbol
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.utils.memberDeclarationNameOrNull
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.resolve.getContainingDeclaration
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.FqName
import org.scip_code.scip_java.kotlinc.ScipSymbolDescriptor.Kind
import org.scip_code.scip_java.shared.LocalSymbolsCache as SharedLocalSymbolsCache

class GlobalSymbolsCache(testing: Boolean = false) : Iterable<Symbol> {
    private val globals =
        if (testing) LinkedHashMap<FirBasedSymbol<*>, Symbol>()
        else HashMap<FirBasedSymbol<*>, Symbol>()
    private val packages =
        if (testing) LinkedHashMap<FqName, Symbol>() else HashMap<FqName, Symbol>()

    operator fun get(symbol: FirBasedSymbol<*>, locals: LocalSymbolsCache): Sequence<Symbol> =
        sequence {
            emitSymbols(symbol, locals)
        }

    operator fun get(symbol: FqName): Sequence<Symbol> = sequence { emitSymbols(symbol) }

    /**
     * called whenever a new symbol should be yielded in the sequence e.g. for properties we also
     * want to yield for every implicit getter/setter, but wouldn't want to yield for e.g. the
     * package symbol parts that a class symbol is composed of.
     */
    @OptIn(SymbolInternals::class)
    private suspend fun SequenceScope<Symbol>.emitSymbols(
        symbol: FirBasedSymbol<*>,
        locals: LocalSymbolsCache,
    ) {
        yield(getSymbol(symbol, locals))
        if (symbol is FirPropertySymbol) {
            if (symbol.fir.getter?.origin is FirDeclarationOrigin.Synthetic)
                emitSymbols(symbol.fir.getter!!.symbol, locals)
            if (symbol.fir.setter?.origin is FirDeclarationOrigin.Synthetic)
                emitSymbols(symbol.fir.setter!!.symbol, locals)
        }
    }

    private suspend fun SequenceScope<Symbol>.emitSymbols(symbol: FqName) {
        yield(getSymbol(symbol))
    }

    /**
     * Entrypoint for building or looking-up a symbol without yielding a value in the sequence.
     * Called recursively for every part of a symbol, unless a cached result short circuits.
     */
    private fun getSymbol(symbol: FirBasedSymbol<*>, locals: LocalSymbolsCache): Symbol {
        globals[symbol]?.let {
            return it
        }
        locals[symbol]?.let {
            return it
        }
        return uncachedSymbol(symbol, locals).also { if (it.isGlobal()) globals[symbol] = it }
    }

    private fun getSymbol(symbol: FqName): Symbol {
        packages[symbol]?.let {
            return it
        }
        return uncachedSymbol(symbol).also { if (it.isGlobal()) packages[symbol] = it }
    }

    @OptIn(SymbolInternals::class)
    private fun uncachedSymbol(symbol: FirBasedSymbol<*>?, locals: LocalSymbolsCache): Symbol {
        if (symbol == null || symbol is FirAnonymousFunctionSymbol) return Symbol.NONE

        if (symbol.fir.isLocalMember) return locals + symbol

        val owner = getParentSymbol(symbol, locals)

        if (owner.isLocal() || owner == Symbol.NONE) return locals + symbol

        val scipDescriptor = scipDescriptor(symbol)

        return Symbol.createGlobal(owner, scipDescriptor)
    }

    private fun uncachedSymbol(symbol: FqName): Symbol {
        if (symbol.isRoot) return Symbol.ROOT_PACKAGE

        val owner = this.getSymbol(symbol.parent())
        return Symbol.createGlobal(
            owner,
            ScipSymbolDescriptor(Kind.PACKAGE, symbol.shortName().asString()),
        )
    }

    /**
     * Returns the parent DeclarationDescriptor for a given DeclarationDescriptor. For most
     * descriptor types, this simply returns the 'containing' descriptor. For Module- or
     * PackageFragmentDescriptors, it returns the descriptor for the parent fqName of the current
     * descriptors fqName e.g. for the fqName `test.sample.main`, the parent fqName would be
     * `test.sample`.
     */
    @OptIn(SymbolInternals::class)
    private fun getParentSymbol(symbol: FirBasedSymbol<*>, locals: LocalSymbolsCache): Symbol {
        when (symbol) {
            is FirTypeParameterSymbol ->
                return getSymbol(symbol.containingDeclarationSymbol, locals)
            is FirValueParameterSymbol ->
                return getSymbol(symbol.containingDeclarationSymbol, locals)
            is FirPropertyAccessorSymbol -> return getSymbol(symbol.propertySymbol, locals)
            is FirCallableSymbol -> {
                val session = symbol.fir.moduleData.session
                return symbol.getContainingSymbol(session)?.let { getSymbol(it, locals) }
                    ?: getSymbol(symbol.packageFqName())
            }
            is FirClassLikeSymbol -> {
                val session = symbol.fir.moduleData.session
                return symbol.getContainingDeclaration(session)?.let { getSymbol(it, locals) }
                    ?: getSymbol(symbol.packageFqName())
            }
            is FirFileSymbol -> {
                return getSymbol(symbol.fir.packageFqName)
            }
            else -> return Symbol.NONE
        }
    }

    @OptIn(SymbolInternals::class)
    private fun scipDescriptor(symbol: FirBasedSymbol<*>): ScipSymbolDescriptor {
        return when {
            symbol is FirAnonymousObjectSymbol ->
                symbol.source?.let { source ->
                    ScipSymbolDescriptor(Kind.TYPE, "<anonymous object at ${source.startOffset}>")
                } ?: ScipSymbolDescriptor.NONE
            symbol is FirClassLikeSymbol ->
                ScipSymbolDescriptor(Kind.TYPE, symbol.classId.shortClassName.asString())
            symbol is FirPropertyAccessorSymbol && symbol.isSetter ->
                ScipSymbolDescriptor(Kind.METHOD, "set")
            symbol is FirPropertyAccessorSymbol && symbol.isGetter ->
                ScipSymbolDescriptor(Kind.METHOD, "get")
            symbol is FirConstructorSymbol ->
                ScipSymbolDescriptor(Kind.METHOD, "<init>", methodDisambiguator(symbol))
            symbol is FirFunctionSymbol ->
                ScipSymbolDescriptor(
                    Kind.METHOD,
                    symbol.name.toString(),
                    methodDisambiguator(symbol),
                )
            symbol is FirTypeParameterSymbol ->
                ScipSymbolDescriptor(Kind.TYPE_PARAMETER, symbol.name.toString())
            symbol is FirValueParameterSymbol ->
                ScipSymbolDescriptor(Kind.PARAMETER, symbol.name.toString())
            symbol is FirVariableSymbol -> ScipSymbolDescriptor(Kind.TERM, symbol.name.toString())
            symbol is FirFileSymbol -> ScipSymbolDescriptor.NONE
            else -> {
                err.println("unknown symbol kind ${symbol.javaClass.simpleName}")
                ScipSymbolDescriptor.NONE
            }
        }
    }

    @OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
    private fun methodDisambiguator(symbol: FirFunctionSymbol<*>): String {
        val session = symbol.moduleData.session

        val siblings =
            when (val containingSymbol = symbol.getContainingSymbol(session)) {
                is FirClassSymbol ->
                    (containingSymbol.fir as FirClass).declarations.map { it.symbol }
                is FirFileSymbol -> containingSymbol.fir.declarations.map { it.symbol }
                null ->
                    symbol.moduleData.session.symbolProvider.getTopLevelCallableSymbols(
                        symbol.packageFqName(),
                        symbol.name,
                    )
                else -> return "()"
            }

        var count = 0
        var found = false
        for (decl in siblings) {
            if (decl == symbol) {
                found = true
                break
            }

            if (decl.memberDeclarationNameOrNull?.equals(symbol.name) == true) {
                count++
            }
        }

        if (count == 0 || !found) return "()"
        return "(+${count})"
    }

    override fun iterator(): Iterator<Symbol> = globals.values.iterator()
}

typealias LocalSymbolsCache = SharedLocalSymbolsCache<FirBasedSymbol<*>, Symbol>

@Suppress("FunctionName")
fun LocalSymbolsCache(): LocalSymbolsCache =
    SharedLocalSymbolsCache(HashMap()) { Symbol.createLocal(it) }

operator fun LocalSymbolsCache.plus(symbol: FirBasedSymbol<*>): Symbol = put(symbol)

class SymbolsCache(private val globals: GlobalSymbolsCache, private val locals: LocalSymbolsCache) {
    operator fun get(symbol: FirBasedSymbol<*>) = globals[symbol, locals]

    operator fun get(symbol: FqName) = globals[symbol]
}
