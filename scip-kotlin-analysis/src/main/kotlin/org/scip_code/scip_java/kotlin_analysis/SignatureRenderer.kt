package org.scip_code.scip_java.kotlin_analysis

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.types.Variance

/**
 * Renders hover signatures in the exact textual format that scip-kotlinc's FirRenderer setup
 * produced (see the goldens under scip-snapshots/expected/kotlin), so that migrating the indexer
 * does not churn signature documentation. Explicitly written type annotations are taken verbatim
 * from source; inferred types are rendered with short names via the Analysis API.
 */
@OptIn(KaExperimentalApi::class)
internal class SignatureRenderer(private val session: KaSession) {

    fun classSignature(declaration: KtClassOrObject): String {
        if (declaration is KtObjectDeclaration && declaration.isObjectLiteral()) {
            return "object : ${superTypesText(declaration)}"
        }
        val keyword =
            when {
                declaration is KtObjectDeclaration && declaration.isCompanion() ->
                    "companion object"
                declaration is KtObjectDeclaration -> "object"
                declaration is KtClass && declaration.isInterface() -> "interface"
                declaration is KtClass && declaration.isEnum() -> "enum class"
                else -> "class"
            }
        val name = declaration.name ?: "Companion"
        return "${visibility(declaration)} ${classModality(declaration)} $keyword $name : " +
            superTypesText(declaration)
    }

    fun constructorSignature(constructor: KtConstructor<*>): String {
        val owner = constructor.containingClassOrObject
        val visibility = explicitVisibility(constructor) ?: "public"
        return "$visibility constructor(${parametersText(constructor.valueParameters)}): " +
            ownerName(owner)
    }

    fun implicitConstructorSignature(declaration: KtClassOrObject): String {
        val visibility = if (declaration is KtObjectDeclaration) "private" else "public"
        return "$visibility constructor(): ${ownerName(declaration)}"
    }

    fun functionSignature(declaration: KtNamedFunction): String {
        val returnType =
            declaration.typeReference?.text ?: renderedReturnType(declaration) ?: "Unit"
        val signature =
            "${visibility(declaration)} ${memberModality(declaration)}fun " +
                "${declaration.name.orEmpty()}(${parametersText(declaration.valueParameters)}): " +
                returnType
        // FirRenderer emitted a trailing newline for functions without a body
        // (see the Animal#sound() golden).
        return if (declaration.hasBody()) signature else signature + "\n"
    }

    fun propertySignature(declaration: KtDeclaration): String {
        val name = (declaration as? org.jetbrains.kotlin.psi.KtNamedDeclaration)?.name.orEmpty()
        val valOrVar = if (isVar(declaration)) "var" else "val"
        val type = explicitPropertyType(declaration) ?: renderedReturnType(declaration) ?: "Any"
        return "${visibility(declaration)} ${memberModality(declaration)}$valOrVar $name: $type"
    }

    fun localVariableSignature(declaration: KtVariableDeclaration): String {
        val valOrVar = if (declaration.isVar) "var" else "val"
        val type = declaration.typeReference?.text ?: renderedReturnType(declaration) ?: "Any"
        return "local $valOrVar ${declaration.name.orEmpty()}: $type"
    }

    fun accessorSignature(accessor: KtPropertyAccessor): String {
        val property = accessor.property
        val visibility = explicitVisibility(accessor) ?: visibility(property)
        val type = explicitPropertyType(property) ?: renderedReturnType(property) ?: "Any"
        return if (accessor.isSetter) {
            val parameterName = accessor.parameter?.name ?: "value"
            "$visibility set($parameterName: $type): Unit"
        } else {
            "$visibility get(): $type"
        }
    }

    fun syntheticAccessorSignature(property: KtDeclaration, setter: Boolean): String {
        val visibility = visibility(property)
        val type = explicitPropertyType(property) ?: renderedReturnType(property) ?: "Any"
        return if (setter) "$visibility set(value: $type): Unit" else "$visibility get(): $type"
    }

    fun setterValueSignature(property: KtDeclaration): String {
        val type = explicitPropertyType(property) ?: renderedReturnType(property) ?: "Any"
        return "value: $type"
    }

    fun parameterSignature(parameter: KtParameter): String {
        val vararg = if (parameter.hasModifier(KtTokens.VARARG_KEYWORD)) "vararg " else ""
        val type = parameter.typeReference?.text ?: renderedReturnType(parameter) ?: "Any"
        val default = if (parameter.hasDefaultValue()) " = ..." else ""
        return "$vararg${parameter.name.orEmpty()}: $type$default"
    }

    fun implicitItSignature(target: KaCallableSymbol): String {
        val type =
            with(session) {
                target.returnType.render(
                    KaTypeRendererForSource.WITH_SHORT_NAMES,
                    position = Variance.INVARIANT,
                )
            }
        return "it: $type"
    }

    private fun ownerName(declaration: KtClassOrObject?): String {
        if (declaration == null) return "<anonymous>"
        if (declaration is KtObjectDeclaration && declaration.isObjectLiteral()) {
            return "<anonymous>"
        }
        val names = mutableListOf<String>()
        var current: KtClassOrObject? = declaration
        while (current != null) {
            names.add(current.name ?: "Companion")
            current = current.containingClassOrObject
        }
        return names.reversed().joinToString(".")
    }

    private fun superTypesText(declaration: KtClassOrObject): String {
        val entries = declaration.superTypeListEntries.mapNotNull { it.typeReference?.text }
        return if (entries.isEmpty()) "Any" else entries.joinToString(", ")
    }

    private fun parametersText(parameters: List<KtParameter>): String =
        parameters.joinToString(", ") { parameter ->
            val type = parameter.typeReference?.text ?: "Any"
            val default = if (parameter.hasDefaultValue()) " = ..." else ""
            "${parameter.name.orEmpty()}: $type$default"
        }

    private fun explicitPropertyType(declaration: KtDeclaration): String? =
        when (declaration) {
            is KtProperty -> declaration.typeReference?.text
            is KtParameter -> declaration.typeReference?.text
            else -> null
        }

    private fun renderedReturnType(declaration: KtDeclaration): String? =
        with(session) {
            (declaration.symbol as? KaCallableSymbol)
                ?.returnType
                ?.render(KaTypeRendererForSource.WITH_SHORT_NAMES, position = Variance.INVARIANT)
        }

    private fun isVar(declaration: KtDeclaration): Boolean =
        when (declaration) {
            is KtProperty -> declaration.isVar
            is KtParameter -> declaration.isMutable
            else -> false
        }

    private fun visibility(declaration: KtDeclaration): String =
        explicitVisibility(declaration) ?: "public"

    private fun explicitVisibility(declaration: KtDeclaration): String? =
        when {
            declaration.hasModifier(KtTokens.PRIVATE_KEYWORD) -> "private"
            declaration.hasModifier(KtTokens.PROTECTED_KEYWORD) -> "protected"
            declaration.hasModifier(KtTokens.INTERNAL_KEYWORD) -> "internal"
            else -> null
        }

    private fun classModality(declaration: KtClassOrObject): String =
        when {
            declaration is KtClass && declaration.isInterface() -> "abstract"
            declaration.hasModifier(KtTokens.SEALED_KEYWORD) -> "sealed"
            declaration.hasModifier(KtTokens.ABSTRACT_KEYWORD) -> "abstract"
            declaration.hasModifier(KtTokens.OPEN_KEYWORD) -> "open"
            else -> "final"
        }

    /** Modality + `override`, with a trailing space; empty for abstract-less contexts. */
    private fun memberModality(declaration: KtDeclaration): String {
        val containingInterface =
            (declaration.containingClassOrObject as? KtClass)?.isInterface() == true
        val isAbstract =
            declaration.hasModifier(KtTokens.ABSTRACT_KEYWORD) ||
                (containingInterface && !hasImplementation(declaration))
        val isOverride = declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD)
        val modality =
            when {
                isAbstract -> "abstract"
                declaration.hasModifier(KtTokens.OPEN_KEYWORD) || isOverride -> "open"
                containingInterface -> "open"
                else -> "final"
            }
        return if (isOverride) "$modality override " else "$modality "
    }

    private fun hasImplementation(declaration: KtDeclaration): Boolean =
        when (declaration) {
            is KtNamedFunction -> declaration.hasBody()
            is KtProperty ->
                declaration.hasInitializer() ||
                    declaration.hasDelegate() ||
                    declaration.accessors.any { it.hasBody() }
            else -> true
        }
}
