package org.scip_code.scip_java.kotlin_analysis

import org.scip_code.scip_java.shared.ScipSymbols as SharedSymbols

@JvmInline
value class Symbol(private val symbol: String) {
    companion object {
        val NONE = Symbol(SharedSymbols.NONE)
        val ROOT_PACKAGE = Symbol(SharedSymbols.ROOT_PACKAGE)

        // Diverges from `SharedSymbols.global` when `desc == NONE`: that returns
        // NONE, this returns the owner — SymbolsCache relies on it.
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

data class ScipSymbolDescriptor(
    val kind: Kind,
    val name: String,
    // Defaults to "()" (`SharedSymbols.Descriptor` defaults to ""): call sites
    // rely on the no-arg form producing `name().` for METHOD kinds.
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
