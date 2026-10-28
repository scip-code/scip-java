package org.scip_code.scip_java.kotlin_analysis

import com.intellij.psi.PsiElement
import java.lang.System.err
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeAliasSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
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
import org.scip_code.scip_java.kotlin_analysis.ScipSymbolDescriptor.Kind
import org.scip_code.scip_java.shared.LocalSymbolsCache as SharedLocalSymbolsCache

/**
 * Computes SCIP symbol strings for Kotlin declarations and resolved references.
 *
 * Declarations with Kotlin source are classified from their PSI structure, which keeps the
 * version-sensitive Analysis API surface minimal; only extension receivers are resolved. References
 * that resolve to declarations without Kotlin source (classpath or JDK symbols) fall back to
 * `ClassId`/`CallableId`-derived symbols.
 *
 * Instantiate one cache per indexed file: local symbols are numbered per SCIP document.
 */
class SymbolsCache(private val session: KaSession) {
    private val globals = HashMap<PsiElement, Symbol>()
    private val packages = HashMap<FqName, Symbol>()
    private val locals = SharedLocalSymbolsCache<Any, Symbol>(HashMap()) { Symbol.createLocal(it) }

    /** Synthetic members of locals need their own IDs, not global descriptor suffixes. */
    private fun memberSymbol(owner: Symbol, descriptor: ScipSymbolDescriptor): Symbol {
        if (!owner.isLocal()) return Symbol.createGlobal(owner, descriptor)
        val key = owner to descriptor
        return locals.get(key) ?: locals.put(key)
    }

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
     * its own. Explicit constructors go through [symbolForDeclaration]. Local classes get a local
     * constructor, keyed by their name or `object` keyword.
     */
    fun implicitConstructorSymbol(classOrObject: KtClassOrObject): Symbol {
        val owner = symbolForDeclaration(classOrObject)
        if (owner.isLocal()) {
            return syntheticLocalSymbol(
                classOrObject.nameIdentifier
                    ?: (classOrObject as? KtObjectDeclaration)?.getObjectKeyword()
                    ?: classOrObject
            )
        }
        return Symbol.createGlobal(owner, ScipSymbolDescriptor(Kind.METHOD, "<init>"))
    }

    /**
     * The anonymous class an enum entry body declares, which owns the body's members, as in FIR.
     * Keyed by the body.
     */
    fun enumEntryBodySymbol(entry: KtEnumEntry): Symbol = syntheticLocalSymbol(entry.body ?: entry)

    /** The local constructor of the anonymous class of an enum entry body. */
    fun enumEntryBodyConstructorSymbol(entry: KtEnumEntry): Symbol =
        syntheticLocalSymbol(entry.nameIdentifier ?: entry)

    /**
     * The synthetic getter/setter of a property (or `val`/`var` constructor parameter) without
     * explicit accessor PSI (`Class#banana.get().`).
     */
    fun syntheticAccessorSymbol(property: KtDeclaration, setter: Boolean): Symbol {
        val propertySymbol =
            if (property is KtParameter) parameterPropertySymbol(property)
            else symbolForDeclaration(property)
        return accessorSymbol(propertySymbol, setter)
    }

    /** The getter or setter of [property] (`Class#banana.get().`, `Class#banana.set().`). */
    fun accessorSymbol(property: Symbol, setter: Boolean): Symbol =
        memberSymbol(property, accessorDescriptor(setter))

    /** The property symbol declared by a `val`/`var` constructor parameter (`Class#banana.`). */
    fun parameterPropertySymbol(parameter: KtParameter): Symbol =
        memberSymbol(
            propertyOwnerSymbol(parameter),
            ScipSymbolDescriptor(Kind.TERM, parameter.name.orEmpty()),
        )

    /** The class symbol owning a constructor reference (`class Seagull : Bird()` → `Bird#`). */
    @OptIn(KaExperimentalApi::class)
    fun constructorOwnerSymbol(session: KaSession, constructor: KaConstructorSymbol): Symbol {
        val target = with(session) { constructor.originalConstructorIfTypeAliased } ?: constructor
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
        memberSymbol(symbolForDeclaration(owner), ScipSymbolDescriptor(Kind.METHOD, name))

    /** Symbol of a compiler-generated member property (enum `entries`). */
    fun memberTermSymbol(owner: KtClassOrObject, name: String): Symbol =
        memberSymbol(symbolForDeclaration(owner), ScipSymbolDescriptor(Kind.TERM, name))

    /** Symbol of a parameter of a compiler-generated method. */
    fun methodParameterSymbol(method: Symbol, name: String): Symbol =
        memberSymbol(method, ScipSymbolDescriptor(Kind.PARAMETER, name))

    /**
     * A per-document local symbol for a compiler-generated declaration without dedicated PSI
     * (implicit `it`, `<when-subject>`, `<destruct>`), keyed by the surrounding element.
     */
    fun syntheticLocalSymbol(key: PsiElement): Symbol = locals.get(key) ?: locals.put(key)

    private fun propertyOwnerSymbol(parameter: KtParameter): Symbol =
        (ownerDeclarationOfParameter(parameter) as? KtConstructor<*>)
            ?.containingClassOrObject
            ?.let { symbolForDeclaration(it) } ?: Symbol.NONE

    /** The `(value)` parameter symbol of a synthetic setter. */
    fun syntheticSetterValueSymbol(property: KtDeclaration): Symbol =
        memberSymbol(
            syntheticAccessorSymbol(property, setter = true),
            ScipSymbolDescriptor(Kind.PARAMETER, "value"),
        )

    /**
     * The implicit `it` parameter of a lambda, keyed by the function literal PSI — safe because
     * anonymous functions never cache a symbol of their own.
     */
    fun implicitItSymbol(literal: KtFunctionLiteral): Symbol = syntheticLocalSymbol(literal)

    /** The SCIP symbol for the target of a resolved reference. */
    @OptIn(KaExperimentalApi::class)
    fun symbolForReference(session: KaSession, target: KaSymbol): Symbol {
        // A constructor call through a type alias (`HashMap()`) references the aliased
        // class's constructor, like javac and scip-kotlinc.
        if (target is KaConstructorSymbol) {
            with(session) { target.originalConstructorIfTypeAliased }
                ?.let {
                    return symbolForReference(session, it)
                }
        }
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
                // Generated functions of local classes have no callable ID. Use
                // the same owner/name key as their synthetic definitions.
                if (target is KaNamedFunctionSymbol) {
                    val owner = with(session) { target.containingSymbol?.psi } as? KtClassOrObject
                    if (owner != null && symbolForDeclaration(owner).isLocal()) {
                        return memberMethodSymbol(owner, target.name.asString())
                    }
                }
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
                        ScipSymbolDescriptor(
                            Kind.METHOD,
                            "<init>",
                            externalConstructorDisambiguator(session, classId, target),
                        ),
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
        val property = externalCallableSymbol(session, target)
        if (property == Symbol.NONE) return Symbol.NONE
        return accessorSymbol(property, setter)
    }

    private fun accessorDescriptor(setter: Boolean): ScipSymbolDescriptor =
        ScipSymbolDescriptor(Kind.METHOD, if (setter) "set" else "get")

    private fun uncachedSymbol(declaration: KtDeclaration): Symbol {
        // Anonymous functions and lambdas have no symbol of their own, mirroring FIR.
        if (declaration is KtFunctionLiteral) return Symbol.NONE
        if (declaration is KtNamedFunction && declaration.name == null) return Symbol.NONE

        // Anonymous objects are local, like their members.
        if (declaration is KtObjectDeclaration && declaration.isObjectLiteral()) {
            return locals.put(declaration)
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
            is KtPropertyAccessor -> symbolForDeclaration(declaration.property)
            else ->
                declaration.containingClassOrObject?.let {
                    if (it is KtEnumEntry) enumEntryBodySymbol(it) else symbolForDeclaration(it)
                }
                    ?: sourceExtensionReceiver(declaration)?.let {
                        extensionOwnerSymbol(declaration.containingKtFile.packageFqName, it)
                    }
                    ?: packageSymbol(declaration.containingKtFile.packageFqName)
        }

    /**
     * Top-level extensions are owned by their receiver type, nested in the extension's own package
     * (`sample/String#foo().` for `fun String.foo()` in `sample`), like scip-kotlinc.
     */
    private fun extensionOwnerSymbol(packageFqName: FqName, receiver: ClassId): Symbol =
        Symbol.createGlobal(
            packageSymbol(packageFqName),
            ScipSymbolDescriptor(Kind.TYPE, receiver.shortClassName.asString()),
        )

    private fun sourceExtensionReceiver(declaration: KtDeclaration): ClassId? {
        if (declaration !is KtCallableDeclaration || declaration.receiverTypeReference == null) {
            return null
        }
        return with(session) { declaration.symbol as? KaCallableSymbol }?.let(::receiverClassId)
    }

    private fun receiverClassId(callable: KaCallableSymbol): ClassId? =
        with(session) { (callable.receiverParameter?.returnType as? KaClassType)?.classId }

    /**
     * The disambiguator of a top-level extension function: like scip-kotlinc, it counts the
     * same-named members of the receiver class (when it is in the extension's package) followed by
     * the same-named extensions on that receiver in the package.
     */
    private fun extensionDisambiguator(
        function: KaCallableSymbol,
        packageFqName: FqName,
        name: Name,
        receiver: ClassId,
    ): String {
        val extensions =
            with(session) {
                findTopLevelCallables(packageFqName, name)
                    .filter { it is KaNamedFunctionSymbol && receiverClassId(it) == receiver }
                    .toList()
            }
        val index = extensions.indexOf(function)
        if (index < 0) return "()"
        val members =
            if (receiver.packageFqName != packageFqName) 0
            else
                with(session) {
                    findClass(receiver)?.declaredMemberScope?.callables(name)?.count() ?: 0
                }
        val count = members + index
        return if (count == 0) "()" else "(+$count)"
    }

    private fun scipDescriptor(declaration: KtDeclaration): ScipSymbolDescriptor =
        when {
            declaration is KtEnumEntry ->
                ScipSymbolDescriptor(Kind.TERM, declaration.name.orEmpty())
            declaration is KtObjectDeclaration && declaration.isCompanion() ->
                ScipSymbolDescriptor(Kind.TYPE, declaration.name ?: "Companion")
            declaration is KtClassOrObject ->
                ScipSymbolDescriptor(Kind.TYPE, declaration.name.orEmpty())
            declaration is KtPropertyAccessor -> accessorDescriptor(declaration.isSetter)
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
     * Port of FIR's `methodDisambiguator`: the N-th preceding function with the same name in the
     * containing scope yields `(+N)`, the first one yields `()`. Classes and properties of the same
     * name don't count, as in [externalMethodDisambiguator] and the javac indexer.
     */
    private fun methodDisambiguator(function: KtNamedFunction): String {
        val name = function.name ?: return "()"
        if (function.containingClassOrObject == null) {
            sourceExtensionReceiver(function)?.let { receiver ->
                val symbol = with(session) { function.symbol }
                return extensionDisambiguator(
                    symbol,
                    function.containingKtFile.packageFqName,
                    Name.identifier(name),
                    receiver,
                )
            }
        }
        var count = 0
        var found = false
        for (sibling in siblingDeclarations(function)) {
            if (sibling == function) {
                found = true
                break
            }
            if (sibling is KtNamedFunction && sibling.name == name) count++
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

    private fun ownerDeclarationOfParameter(parameter: KtParameter): KtDeclaration? =
        (parameter.parent as? KtParameterList)?.parent as? KtDeclaration

    internal fun isDeclarationParameter(parameter: KtParameter): Boolean =
        ownerDeclarationOfParameter(parameter) != null

    /**
     * External (classpath/JDK) callables, disambiguated by their position among the same-named
     * callables of their container — mirroring FIR, which consulted the class's declared members or
     * the top-level symbol provider (e.g. `kotlin/collections/forEachIndexed(+9).`).
     */
    private fun externalCallableSymbol(session: KaSession, resolved: KaCallableSymbol): Symbol {
        // Inherited members must use their declaration's owner and overload number.
        val target = with(session) { resolved.fakeOverrideOriginal }
        val callableId = target.callableId ?: return Symbol.NONE
        val owner =
            callableId.classId?.let(::classSymbol)
                ?: receiverClassId(target)?.let { extensionOwnerSymbol(callableId.packageName, it) }
                ?: packageSymbol(callableId.packageName)
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

    /** Like javac: constructors in declaration order, private ones included. */
    private fun externalConstructorDisambiguator(
        session: KaSession,
        classId: ClassId,
        target: KaConstructorSymbol,
    ): String {
        val constructors =
            with(session) { findClass(classId)?.declaredMemberScope?.constructors?.toList() }
                ?: return "()"
        val index = constructors.indexOf(target)
        return if (index <= 0) "()" else "(+$index)"
    }

    private fun externalMethodDisambiguator(session: KaSession, target: KaCallableSymbol): String {
        val callableId = target.callableId ?: return "()"
        val overloads: List<KaCallableSymbol> =
            with(session) {
                val classId = callableId.classId
                if (classId == null) {
                    receiverClassId(target)?.let { receiver ->
                        return extensionDisambiguator(
                            target,
                            callableId.packageName,
                            callableId.callableName,
                            receiver,
                        )
                    }
                    findTopLevelCallables(callableId.packageName, callableId.callableName)
                        .filterIsInstance<KaFunctionSymbol>()
                        .toList()
                } else {
                    val classSymbol = findClass(classId) ?: return "()"
                    classSymbol.declaredMemberScope
                        .callables(callableId.callableName)
                        .filterIsInstance<KaFunctionSymbol>()
                        .toList()
                }
            }
        val index = overloads.indexOfFirst { it == target }
        // Source declaredMemberScope omits generated Any overrides. They follow
        // the explicit overloads in the compiled class, rather than sharing index 0.
        if (index < 0 && target.origin == KaSymbolOrigin.SOURCE_MEMBER_GENERATED) {
            return if (overloads.isEmpty()) "()" else "(+${overloads.size})"
        }
        return if (index <= 0) "()" else "(+$index)"
    }
}
