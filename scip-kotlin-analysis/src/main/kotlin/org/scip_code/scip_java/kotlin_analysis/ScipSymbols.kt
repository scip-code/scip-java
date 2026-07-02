package org.scip_code.scip_java.kotlin_analysis

import org.scip_code.scip_java.shared.ScipSymbols as SharedSymbols

// Ported from scip-kotlinc's ScipSymbols.kt so that the Analysis-API-based indexer
// produces byte-identical SCIP symbol strings. scip-kotlinc is deleted once this
// indexer reaches parity, at which point this file is the only copy.
@JvmInline
value class Symbol(private val symbol: String) {
    companion object {
        val NONE = Symbol(SharedSymbols.NONE)
        val ROOT_PACKAGE = Symbol(SharedSymbols.ROOT_PACKAGE)

        // Note: this intentionally diverges from `SharedSymbols.global` when
        // `desc == NONE` — Java returns `NONE`, Kotlin returns the owner.
        // SymbolsCache relies on this behavior; do not delegate without first
        // updating those call sites.
        fun createGlobal(owner: Symbol, desc: ScipSymbolDescriptor): Symbol =
            when {
                desc == ScipSymbolDescriptor.NONE -> owner
                owner != ROOT_PACKAGE -> Symbol(owner.symbol + desc.encode().symbol)
                else -> desc.encode()
            }

        fun createLocal(i: Int) = Symbol(SharedSymbols.local(i))
    }

    fun isGlobal() = SharedSymbols.isGlobal(symbol)

    fun isLocal() = SharedSymbols.isLocal(symbol)

    override fun toString(): String = symbol
}

fun String.symbol(): Symbol = Symbol(this)

data class ScipSymbolDescriptor(
    val kind: Kind,
    val name: String,
    // Default differs from `SharedSymbols.Descriptor` (which is "") because
    // Kotlin call sites — getters/setters in particular — rely on the no-arg
    // overload producing `name().` rather than `name.` for METHOD kinds.
    val disambiguator: String = "()",
) {
    companion object {
        val NONE = ScipSymbolDescriptor(Kind.NONE, "")
    }

    enum class Kind {
        NONE,
        TERM,
        METHOD,
        TYPE,
        PACKAGE,
        PARAMETER,
        TYPE_PARAMETER;

        internal fun toSharedKind(): SharedSymbols.Descriptor.Kind =
            when (this) {
                NONE -> SharedSymbols.Descriptor.Kind.None
                TERM -> SharedSymbols.Descriptor.Kind.Term
                METHOD -> SharedSymbols.Descriptor.Kind.Method
                TYPE -> SharedSymbols.Descriptor.Kind.Type
                PACKAGE -> SharedSymbols.Descriptor.Kind.Package
                PARAMETER -> SharedSymbols.Descriptor.Kind.Parameter
                TYPE_PARAMETER -> SharedSymbols.Descriptor.Kind.TypeParameter
            }
    }

    fun encode(): Symbol =
        if (kind == Kind.NONE) Symbol(SharedSymbols.NONE)
        else Symbol(SharedSymbols.Descriptor(kind.toSharedKind(), name, disambiguator).encode())
}
