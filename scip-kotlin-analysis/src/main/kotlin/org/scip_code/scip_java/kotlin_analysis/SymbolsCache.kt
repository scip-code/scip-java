package org.scip_code.scip_java.kotlin_analysis

import com.intellij.psi.PsiElement
import java.lang.System.err
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeAliasSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.scip_code.scip_java.kotlin_analysis.ScipSymbolDescriptor.Kind
import org.scip_code.scip_java.shared.LocalSymbolsCache as SharedLocalSymbolsCache

/**
 * Computes SCIP symbol strings for Kotlin declarations and resolved references.
 *
 * Declarations with Kotlin source are classified from their PSI structure, which keeps the
 * version-sensitive Analysis API surface minimal. References that resolve to declarations without
 * Kotlin source (classpath or JDK symbols) fall back to `ClassId`/`CallableId`-derived symbols.
 *
 * Instantiate one cache per indexed file: local symbols are numbered per SCIP document.
 */
class SymbolsCache {
    private val globals = HashMap<PsiElement, Symbol>()
    private val packages = HashMap<FqName, Symbol>()
    private val locals =
        SharedLocalSymbolsCache<PsiElement, Symbol>(HashMap()) { Symbol.createLocal(it) }

    fun packageSymbol(fqName: FqName): Symbol {
        if (fqName.isRoot) return Symbol.ROOT_PACKAGE
        return packages.getOrPut(fqName) {
            Symbol.createGlobal(
                packageSymbol(fqName.parent()),
                ScipSymbolDescriptor(Kind.PACKAGE, fqName.shortName().asString()),
            )
        }
    }

    /** The SCIP symbol for a declaration in Kotlin source. */
    fun symbolForDeclaration(declaration: KtDeclaration): Symbol {
        globals[declaration]?.let {
            return it
        }
        locals.get(declaration)?.let {
            return it
        }
        val symbol = uncachedSymbol(declaration)
        if (symbol.isGlobal()) globals[declaration] = symbol
        return symbol
    }

    /**
     * The SCIP symbol of the implicit primary constructor of [classOrObject], which has no PSI of
     * its own. Explicit constructors go through [symbolForDeclaration].
     */
    fun implicitConstructorSymbol(classOrObject: KtClassOrObject): Symbol =
        Symbol.createGlobal(
            symbolForDeclaration(classOrObject),
            ScipSymbolDescriptor(Kind.METHOD, "<init>"),
        )

    /**
     * The synthetic getter/setter of a property (or `val`/`var` constructor parameter) without
     * explicit accessor PSI. Owned by the property's container (`Class#getBanana().`), not by the
     * constructor declaring the parameter.
     */
    fun syntheticAccessorSymbol(property: KtDeclaration, setter: Boolean): Symbol {
        val name = (property as? KtNamedDeclaration)?.name.orEmpty()
        return Symbol.createGlobal(propertyOwnerSymbol(property), accessorDescriptor(name, setter))
    }

    /** The property symbol declared by a `val`/`var` constructor parameter (`Class#banana.`). */
    fun parameterPropertySymbol(parameter: KtParameter): Symbol =
        Symbol.createGlobal(
            propertyOwnerSymbol(parameter),
            ScipSymbolDescriptor(Kind.TERM, parameter.name.orEmpty()),
        )

    /** The class symbol owning a constructor reference (`class Seagull : Bird()` → `Bird#`). */
    fun constructorOwnerSymbol(session: KaSession, target: KaConstructorSymbol): Symbol {
        when (val psi = target.psi) {
            is KtClassOrObject -> return symbolForDeclaration(psi)
            is KtConstructor<*> ->
                psi.containingClassOrObject?.let {
                    return symbolForDeclaration(it)
                }
        }
        return target.containingClassId?.let(::classSymbol) ?: Symbol.NONE
    }

    /** The SCIP symbol of a class referenced by its (expanded) [ClassId]. */
    fun classSymbol(classId: ClassId): Symbol {
        var symbol = packageSymbol(classId.packageFqName)
        for (segment in classId.relativeClassName.pathSegments()) {
            symbol =
                Symbol.createGlobal(symbol, ScipSymbolDescriptor(Kind.TYPE, segment.asString()))
        }
        return symbol
    }

    /** Symbol of a compiler-generated member method (enum `values()`, data-class `copy()`, …). */
    fun memberMethodSymbol(owner: KtClassOrObject, name: String): Symbol =
        Symbol.createGlobal(symbolForDeclaration(owner), ScipSymbolDescriptor(Kind.METHOD, name))

    /** Symbol of a compiler-generated member property (enum `entries`). */
    fun memberTermSymbol(owner: KtClassOrObject, name: String): Symbol =
        Symbol.createGlobal(symbolForDeclaration(owner), ScipSymbolDescriptor(Kind.TERM, name))

    /** Symbol of a parameter of a compiler-generated method. */
    fun methodParameterSymbol(method: Symbol, name: String): Symbol =
        Symbol.createGlobal(method, ScipSymbolDescriptor(Kind.PARAMETER, name))

    /**
     * A per-document local symbol for a compiler-generated declaration without dedicated PSI
     * (implicit `it`, `<when-subject>`, `<destruct>`), keyed by the surrounding element.
     */
    fun syntheticLocalSymbol(key: PsiElement): Symbol = locals.get(key) ?: locals.put(key)

    private fun propertyOwnerSymbol(property: KtDeclaration): Symbol =
        when (property) {
            is KtParameter ->
                (ownerDeclarationOfParameter(property) as? KtConstructor<*>)
                    ?.containingClassOrObject
                    ?.let { symbolForDeclaration(it) } ?: Symbol.NONE
            else -> ownerSymbol(property)
        }

    /** The `(value)` parameter symbol of a synthetic setter. */
    fun syntheticSetterValueSymbol(property: KtDeclaration): Symbol =
        Symbol.createGlobal(
            syntheticAccessorSymbol(property, setter = true),
            ScipSymbolDescriptor(Kind.PARAMETER, "value"),
        )

    /**
     * The implicit `it` parameter of a lambda, keyed by the function literal PSI — safe because
     * anonymous functions never cache a symbol of their own.
     */
    fun implicitItSymbol(literal: KtFunctionLiteral): Symbol = syntheticLocalSymbol(literal)

    /** The SCIP symbol for the target of a resolved reference. */
    fun symbolForReference(session: KaSession, target: KaSymbol): Symbol {
        val psi = target.psi
        if (psi is KtDeclaration) {
            // A constructor symbol whose PSI is the class itself is an implicit primary.
            if (target is KaConstructorSymbol && psi is KtClassOrObject) {
                return implicitConstructorSymbol(psi)
            }
            // The implicit `it` parameter's PSI is the enclosing function literal.
            if (psi is KtFunctionLiteral && target !is KaFunctionSymbol) {
                return implicitItSymbol(psi)
            }
            // Source type aliases are expanded like library ones, matching scip-kotlinc.
            if (psi is KtTypeAlias && target is KaTypeAliasSymbol) {
                return with(session) { (target.expandedType as? KaClassType)?.classId }
                    ?.let(::classSymbol) ?: symbolForDeclaration(psi)
            }
            // Compiler-generated callables (data-class componentN/copy, enum
            // `entries`, …) resolve to PSI of the class or parameter they derive
            // from; their symbol comes from the callable id instead.
            if (
                target is KaFunctionSymbol &&
                    target !is KaConstructorSymbol &&
                    psi !is KtNamedFunction &&
                    psi !is KtPropertyAccessor &&
                    psi !is KtFunctionLiteral
            ) {
                return externalCallableSymbol(session, target)
            }
            if (target is KaCallableSymbol && psi is KtClassOrObject) {
                return externalCallableSymbol(session, target)
            }
            return symbolForDeclaration(psi)
        }
        return when (target) {
            is KaPackageSymbol -> packageSymbol(target.fqName)
            is KaConstructorSymbol ->
                target.containingClassId?.let { classId ->
                    Symbol.createGlobal(
                        classSymbol(classId),
                        ScipSymbolDescriptor(Kind.METHOD, "<init>"),
                    )
                } ?: Symbol.NONE
            // Library type aliases are expanded to the aliased class (`AutoCloseable`
            // → `java/lang/AutoCloseable#`), unifying Kotlin and Java references.
            is KaTypeAliasSymbol ->
                with(session) { (target.expandedType as? KaClassType)?.classId }?.let(::classSymbol)
                    ?: target.classId?.let(::classSymbol)
                    ?: Symbol.NONE
            is KaClassLikeSymbol -> target.classId?.let(::classSymbol) ?: Symbol.NONE
            is KaCallableSymbol -> externalCallableSymbol(session, target)
            else -> Symbol.NONE
        }
    }

    /** The getter/setter symbol of an external (classpath) property reference. */
    fun externalAccessorSymbol(
        session: KaSession,
        target: KaCallableSymbol,
        setter: Boolean,
    ): Symbol {
        val callableId = target.callableId ?: return Symbol.NONE
        val owner = callableId.classId?.let(::classSymbol) ?: packageSymbol(callableId.packageName)
        return Symbol.createGlobal(
            owner,
            accessorDescriptor(callableId.callableName.asString(), setter),
        )
    }

    private fun accessorDescriptor(propertyName: String, setter: Boolean): ScipSymbolDescriptor =
        ScipSymbolDescriptor(
            Kind.METHOD,
            (if (setter) "set" else "get") + propertyName.capitalizeAsciiOnly(),
        )

    private fun uncachedSymbol(declaration: KtDeclaration): Symbol {
        // Anonymous functions and lambdas have no symbol of their own, mirroring FIR.
        if (declaration is KtFunctionLiteral) return Symbol.NONE
        if (declaration is KtNamedFunction && declaration.name == null) return Symbol.NONE

        // Anonymous objects are global symbols owned by the file's package, mirroring FIR.
        if (declaration is KtObjectDeclaration && declaration.isObjectLiteral()) {
            return Symbol.createGlobal(
                packageSymbol(declaration.containingKtFile.packageFqName),
                scipDescriptor(declaration),
            )
        }

        if (isLocalMember(declaration)) return locals.put(declaration)

        val owner = ownerSymbol(declaration)
        if (owner.isLocal() || owner == Symbol.NONE) return locals.put(declaration)

        return Symbol.createGlobal(owner, scipDescriptor(declaration))
    }

    /**
     * Port of FIR's `isLocalMember`: functions, variables and classes declared in code bodies are
     * local. Members of any class-like container — including anonymous objects — are not: locality
     * is decided by the nearest scope owner, not by syntactic nesting.
     */
    private fun isLocalMember(declaration: KtDeclaration): Boolean {
        when (declaration) {
            is KtNamedFunction,
            is KtVariableDeclaration,
            is KtClassOrObject -> {}
            else -> return false
        }
        for (parent in declaration.parents) {
            when (parent) {
                is KtClassBody,
                is KtFile -> return false
                is KtBlockExpression,
                is KtFunction,
                is KtPropertyAccessor,
                is KtProperty,
                is KtAnonymousInitializer -> return true
            }
        }
        return false
    }

    private fun ownerSymbol(declaration: KtDeclaration): Symbol =
        when (declaration) {
            is KtTypeParameter ->
                declaration.getStrictParentOfType<KtTypeParameterListOwner>()?.let {
                    symbolForDeclaration(it)
                } ?: Symbol.NONE
            is KtParameter ->
                ownerDeclarationOfParameter(declaration)?.let { symbolForDeclaration(it) }
                    ?: Symbol.NONE
            is KtPropertyAccessor ->
                declaration.property.containingClassOrObject?.let { symbolForDeclaration(it) }
                    ?: packageSymbol(declaration.containingKtFile.packageFqName)
            else ->
                declaration.containingClassOrObject?.let { symbolForDeclaration(it) }
                    ?: packageSymbol(declaration.containingKtFile.packageFqName)
        }

    private fun scipDescriptor(declaration: KtDeclaration): ScipSymbolDescriptor =
        when {
            declaration is KtObjectDeclaration && declaration.isObjectLiteral() ->
                ScipSymbolDescriptor(
                    Kind.TYPE,
                    "<anonymous object at ${declaration.textRange.startOffset}>",
                )
            declaration is KtEnumEntry ->
                ScipSymbolDescriptor(Kind.TERM, declaration.name.orEmpty())
            declaration is KtObjectDeclaration && declaration.isCompanion() ->
                ScipSymbolDescriptor(Kind.TYPE, declaration.name ?: "Companion")
            declaration is KtClassOrObject ->
                ScipSymbolDescriptor(Kind.TYPE, declaration.name.orEmpty())
            declaration is KtPropertyAccessor && declaration.isSetter ->
                accessorDescriptor(declaration.property.name.orEmpty(), setter = true)
            declaration is KtPropertyAccessor ->
                accessorDescriptor(declaration.property.name.orEmpty(), setter = false)
            declaration is KtConstructor<*> ->
                ScipSymbolDescriptor(Kind.METHOD, "<init>", constructorDisambiguator(declaration))
            declaration is KtNamedFunction ->
                ScipSymbolDescriptor(
                    Kind.METHOD,
                    declaration.name.orEmpty(),
                    methodDisambiguator(declaration),
                )
            declaration is KtTypeAlias ->
                ScipSymbolDescriptor(Kind.TYPE, declaration.name.orEmpty())
            declaration is KtTypeParameter ->
                ScipSymbolDescriptor(Kind.TYPE_PARAMETER, declaration.name.orEmpty())
            declaration is KtParameter ->
                ScipSymbolDescriptor(Kind.PARAMETER, declaration.name.orEmpty())
            declaration is KtVariableDeclaration ->
                ScipSymbolDescriptor(Kind.TERM, declaration.name.orEmpty())
            else -> {
                err.println("unknown declaration kind ${declaration.javaClass.simpleName}")
                ScipSymbolDescriptor.NONE
            }
        }

    /**
     * Port of FIR's `methodDisambiguator`: the N-th preceding declaration with the same name in the
     * containing scope yields `(+N)`, the first one yields `()`.
     */
    private fun methodDisambiguator(function: KtNamedFunction): String {
        val name = function.name ?: return "()"
        var count = 0
        var found = false
        for (sibling in siblingDeclarations(function)) {
            if (sibling == function) {
                found = true
                break
            }
            if (declaredName(sibling) == name) count++
        }
        if (count == 0 || !found) return "()"
        return "(+$count)"
    }

    private fun constructorDisambiguator(constructor: KtConstructor<*>): String {
        val containingClass = constructor.containingClassOrObject ?: return "()"
        val constructors =
            listOfNotNull(containingClass.primaryConstructor) +
                containingClass.declarations.filterIsInstance<KtSecondaryConstructor>()
        val index = constructors.indexOf(constructor)
        return if (index <= 0) "()" else "(+$index)"
    }

    private fun siblingDeclarations(declaration: KtDeclaration): List<KtDeclaration> =
        when (val containingClass = declaration.containingClassOrObject) {
            null -> declaration.containingKtFile.declarations
            else -> listOfNotNull(containingClass.primaryConstructor) + containingClass.declarations
        }

    private fun declaredName(declaration: KtDeclaration): String? =
        when (declaration) {
            is KtConstructor<*> -> "<init>"
            is KtNamedDeclaration -> declaration.name
            else -> null
        }

    private fun ownerDeclarationOfParameter(parameter: KtParameter): KtDeclaration? =
        (parameter.parent as? KtParameterList)?.parent as? KtDeclaration

    internal fun isDeclarationParameter(parameter: KtParameter): Boolean =
        ownerDeclarationOfParameter(parameter) != null

    /**
     * External (classpath/JDK) callables, disambiguated by their position among the same-named
     * callables of their container — mirroring FIR, which consulted the class's declared members or
     * the top-level symbol provider (e.g. `kotlin/collections/forEachIndexed(+9).`).
     */
    private fun externalCallableSymbol(session: KaSession, target: KaCallableSymbol): Symbol {
        val callableId = target.callableId ?: return Symbol.NONE
        val owner = callableId.classId?.let(::classSymbol) ?: packageSymbol(callableId.packageName)
        val name = callableId.callableName.asString()
        val descriptor =
            when (target) {
                is KaFunctionSymbol ->
                    ScipSymbolDescriptor(
                        Kind.METHOD,
                        name,
                        externalMethodDisambiguator(session, target),
                    )
                else -> ScipSymbolDescriptor(Kind.TERM, name)
            }
        return Symbol.createGlobal(owner, descriptor)
    }

    private fun externalMethodDisambiguator(session: KaSession, target: KaCallableSymbol): String {
        val callableId = target.callableId ?: return "()"
        val overloads: List<KaCallableSymbol> =
            with(session) {
                val classId = callableId.classId
                if (classId == null) {
                    findTopLevelCallables(callableId.packageName, callableId.callableName).toList()
                } else {
                    val classSymbol = findClass(classId) ?: return "()"
                    classSymbol.declaredMemberScope.callables(callableId.callableName).toList()
                }
            }
        val index = overloads.indexOfFirst { it == target }
        return if (index <= 0) "()" else "(+$index)"
    }
}
