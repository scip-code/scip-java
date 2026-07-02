package org.scip_code.scip_java.kotlin_analysis

import com.intellij.psi.PsiElement
import java.lang.System.err
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
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
 * This is a PSI-driven port of the FIR-based `SymbolsCache` in scip-kotlinc. Declarations with
 * Kotlin source are classified from their PSI structure, which keeps the version-sensitive Analysis
 * API surface minimal. References that resolve to declarations without Kotlin source (classpath or
 * JDK symbols) fall back to `ClassId`/`CallableId`-derived symbols.
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

    /** The SCIP symbol for the target of a resolved reference. */
    fun symbolForReference(target: KaSymbol): Symbol {
        val psi = target.psi
        if (psi is KtDeclaration) {
            // A constructor symbol whose PSI is the class itself is an implicit
            // primary constructor.
            if (target is KaConstructorSymbol && psi is KtClassOrObject) {
                return Symbol.createGlobal(
                    symbolForDeclaration(psi),
                    ScipSymbolDescriptor(Kind.METHOD, "<init>"),
                )
            }
            return symbolForDeclaration(psi)
        }
        return when (target) {
            is KaPackageSymbol -> packageSymbol(target.fqName)
            is KaConstructorSymbol ->
                target.containingClassId?.let { classId ->
                    Symbol.createGlobal(
                        classIdSymbol(classId),
                        ScipSymbolDescriptor(Kind.METHOD, "<init>"),
                    )
                } ?: Symbol.NONE
            is KaClassLikeSymbol -> target.classId?.let(::classIdSymbol) ?: Symbol.NONE
            is KaCallableSymbol -> externalCallableSymbol(target)
            else -> Symbol.NONE
        }
    }

    private fun uncachedSymbol(declaration: KtDeclaration): Symbol {
        // Anonymous functions and lambdas have no symbol of their own, mirroring
        // the FIR indexer which returned NONE for FirAnonymousFunctionSymbol.
        if (declaration is KtFunctionLiteral) return Symbol.NONE
        if (declaration is KtNamedFunction && declaration.name == null) return Symbol.NONE

        if (isLocalMember(declaration)) return locals.put(declaration)

        val owner = ownerSymbol(declaration)
        if (owner.isLocal() || owner == Symbol.NONE) return locals.put(declaration)

        return Symbol.createGlobal(owner, scipDescriptor(declaration))
    }

    // Port of FIR's `isLocalMember`: only functions, variables and classes can be
    // local members; everything else is classified through its owner.
    private fun isLocalMember(declaration: KtDeclaration): Boolean =
        when (declaration) {
            is KtNamedFunction,
            is KtVariableDeclaration,
            is KtClassOrObject ->
                declaration.parents
                    .takeWhile { it !is KtFile }
                    .any { it !is KtClassBody && it !is KtClassOrObject }
            else -> false
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
                ScipSymbolDescriptor(
                    Kind.METHOD,
                    "set" + declaration.property.name.orEmpty().capitalizeAsciiOnly(),
                )
            declaration is KtPropertyAccessor ->
                ScipSymbolDescriptor(
                    Kind.METHOD,
                    "get" + declaration.property.name.orEmpty().capitalizeAsciiOnly(),
                )
            declaration is KtConstructor<*> ->
                ScipSymbolDescriptor(Kind.METHOD, "<init>", constructorDisambiguator(declaration))
            declaration is KtNamedFunction ->
                ScipSymbolDescriptor(
                    Kind.METHOD,
                    declaration.name.orEmpty(),
                    methodDisambiguator(declaration),
                )
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

    private fun externalCallableSymbol(target: KaCallableSymbol): Symbol {
        val callableId = target.callableId ?: return Symbol.NONE
        val owner =
            callableId.classId?.let(::classIdSymbol) ?: packageSymbol(callableId.packageName)
        val name = callableId.callableName.asString()
        val descriptor =
            when (target) {
                is KaFunctionSymbol -> ScipSymbolDescriptor(Kind.METHOD, name)
                else -> ScipSymbolDescriptor(Kind.TERM, name)
            }
        return Symbol.createGlobal(owner, descriptor)
    }

    private fun classIdSymbol(classId: ClassId): Symbol {
        var symbol = packageSymbol(classId.packageFqName)
        for (segment in classId.relativeClassName.pathSegments()) {
            symbol =
                Symbol.createGlobal(symbol, ScipSymbolDescriptor(Kind.TYPE, segment.asString()))
        }
        return symbol
    }
}
