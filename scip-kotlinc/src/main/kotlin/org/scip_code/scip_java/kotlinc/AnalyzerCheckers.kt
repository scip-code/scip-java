package org.scip_code.scip_java.kotlinc

import java.nio.file.Path
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirResolvedQualifierChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirTypeOperatorCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirTypeOperatorCall
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.calls.FirSyntheticFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousObjectSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

open class AnalyzerCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    companion object {
        val visitors: MutableMap<KtSourceFile, ScipVisitor> = mutableMapOf()

        private fun getIdentifier(element: KtSourceElement): KtSourceElement =
            element.treeStructure
                .findChildByType(element.lighterASTNode, KtTokens.IDENTIFIER)
                ?.toKtLightSourceElement(element.treeStructure) ?: element

        context(context: CheckerContext)
        private fun ScipVisitor.emitTypeRef(typeRef: FirTypeRef) {
            val klass = typeRef.toClassLikeSymbol(context.session)
            val source = typeRef.source
            if (klass != null && source != null && source.kind !is KtFakeSourceElementKind) {
                visitClassReference(klass, getIdentifier(source))
            }
        }
    }

    override val declarationCheckers: DeclarationCheckers
        get() = AnalyzerDeclarationCheckers(session.analyzerParamsProvider.sourceroot)

    override val expressionCheckers: ExpressionCheckers
        get() =
            object : ExpressionCheckers() {
                override val qualifiedAccessExpressionCheckers:
                    Set<FirQualifiedAccessExpressionChecker> =
                    setOf(SemanticQualifiedAccessExpressionChecker())

                override val resolvedQualifierCheckers: Set<FirResolvedQualifierChecker> =
                    setOf(SemanticResolvedQualifierChecker())

                override val typeOperatorCallCheckers: Set<FirTypeOperatorCallChecker> =
                    setOf(SemanticClassReferenceExpressionChecker())
            }

    open class AnalyzerDeclarationCheckers(sourceroot: Path) : DeclarationCheckers() {
        override val fileCheckers: Set<FirFileChecker> =
            setOf(SemanticFileChecker(sourceroot), SemanticImportsChecker())
        override val classLikeCheckers: Set<FirClassLikeChecker> = setOf(SemanticClassLikeChecker())
        override val constructorCheckers: Set<FirConstructorChecker> =
            setOf(SemanticConstructorChecker())
        override val simpleFunctionCheckers: Set<FirSimpleFunctionChecker> =
            setOf(SemanticSimpleFunctionChecker())
        override val anonymousFunctionCheckers: Set<FirAnonymousFunctionChecker> =
            setOf(SemanticAnonymousFunctionChecker())
        override val propertyCheckers: Set<FirPropertyChecker> = setOf(SemanticPropertyChecker())
        override val valueParameterCheckers: Set<FirValueParameterChecker> =
            setOf(SemanticValueParameterChecker())
        override val typeParameterCheckers: Set<FirTypeParameterChecker> =
            setOf(SemanticTypeParameterChecker())
        override val typeAliasCheckers: Set<FirTypeAliasChecker> = setOf(SemanticTypeAliasChecker())
        override val propertyAccessorCheckers: Set<FirPropertyAccessorChecker> =
            setOf(SemanticPropertyAccessorChecker())
        override val enumEntryCheckers: Set<FirEnumEntryChecker> = setOf(SemanticEnumEntryChecker())
    }

    private class SemanticFileChecker(private val sourceroot: Path) :
        FirFileChecker(MppCheckerKind.Common) {
        companion object {
            val globals = GlobalSymbolsCache()
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirFile) {
            val ktFile = declaration.sourceFile ?: return
            val lineMap = LineMap(declaration)
            val visitor = ScipVisitor(sourceroot, ktFile, lineMap, globals)
            visitors[ktFile] = visitor
        }
    }

    class SemanticImportsChecker : FirFileChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirFile) {
            val ktFile = declaration.sourceFile ?: return
            val visitor = visitors[ktFile]

            val eachFqNameElement =
                {
                    fqName: FqName,
                    tree: FlyweightCapableTreeStructure<LighterASTNode>,
                    names: LighterASTNode,
                    callback: (FqName, KtLightSourceElement) -> Unit ->
                    val nameList =
                        if (names.tokenType == KtNodeTypes.REFERENCE_EXPRESSION) listOf(names)
                        else tree.collectDescendantsOfType(names, KtNodeTypes.REFERENCE_EXPRESSION)

                    var ancestor = fqName
                    var depth = 0

                    while (ancestor != FqName.ROOT) {
                        val nameNode = nameList[nameList.lastIndex - depth]
                        val nameSource = nameNode.toKtLightSourceElement(tree)

                        callback(ancestor, nameSource)

                        ancestor = ancestor.parent()
                        depth++
                    }
                }

            val packageDirective = declaration.packageDirective
            val fqName = packageDirective.packageFqName
            val source = packageDirective.source
            if (source != null) {
                val names =
                    source.treeStructure.findChildByType(
                        source.lighterASTNode,
                        KtNodeTypes.DOT_QUALIFIED_EXPRESSION,
                    )
                        ?: source.treeStructure.findChildByType(
                            source.lighterASTNode,
                            KtNodeTypes.REFERENCE_EXPRESSION,
                        )

                if (names != null) {
                    eachFqNameElement(fqName, source.treeStructure, names) { fqName, name ->
                        visitor?.visitPackage(fqName, name)
                    }
                }
            }

            declaration.imports.forEach { import ->
                val source = import.source ?: return@forEach
                val fqName = import.importedFqName ?: return@forEach

                val names =
                    source.treeStructure.findDescendantByType(
                        source.lighterASTNode,
                        KtNodeTypes.DOT_QUALIFIED_EXPRESSION,
                    )
                if (names != null) {
                    eachFqNameElement(fqName, source.treeStructure, names) { fqName, name ->
                        val symbolProvider = context.session.symbolProvider

                        val klass =
                            symbolProvider.getClassLikeSymbolByClassId(ClassId.topLevel(fqName))
                        val callables =
                            symbolProvider.getTopLevelCallableSymbols(
                                fqName.parent(),
                                fqName.shortName(),
                            )

                        if (klass != null) {
                            visitor?.visitClassReference(klass, name)
                        } else if (callables.isNotEmpty()) {
                            for (callable in callables) {
                                visitor?.visitCallableReference(callable, name)
                            }
                        } else {
                            visitor?.visitPackage(fqName, name)
                        }
                    }
                }
            }
        }
    }

    private class SemanticClassLikeChecker : FirClassLikeChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirClassLikeDeclaration) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            val objectKeyword =
                if (declaration is FirAnonymousObject) {
                    source.treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.OBJECT_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)
                } else {
                    null
                }
            val identifierSource = getIdentifier(source)
            // For unnamed companion objects, getIdentifier() falls back to source (no IDENTIFIER
            // token). Use the 'companion' keyword as the range instead. The COMPANION_KEYWORD is
            // inside a MODIFIER_LIST child, so we use findDescendantByType instead of
            // findChildByType.
            val companionKeyword =
                if (
                    identifierSource === source &&
                        declaration is FirRegularClass &&
                        declaration.isCompanion
                ) {
                    source.treeStructure
                        .findDescendantByType(source.lighterASTNode, KtTokens.COMPANION_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)
                } else {
                    null
                }
            visitor?.visitClassOrObject(
                declaration,
                objectKeyword ?: companionKeyword ?: identifierSource,
                enclosingSource = source,
            )

            if (declaration is FirClass) {
                for (superType in declaration.superTypeRefs) {
                    visitor?.emitTypeRef(superType)
                }
            }
        }
    }

    private class SemanticConstructorChecker : FirConstructorChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirConstructor) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]

            if (declaration.isPrimary) {
                // if the constructor is not denoted by the 'constructor' keyword, we want to link
                // it to the
                // class identifier
                val klass = declaration.symbol.getContainingClassSymbol()
                val klassSource = klass?.source ?: source
                val constructorKeyboard =
                    source.treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.CONSTRUCTOR_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)

                val objectKeyword =
                    if (klass is FirAnonymousObjectSymbol) {
                        source.treeStructure
                            .findChildByType(source.lighterASTNode, KtTokens.OBJECT_KEYWORD)
                            ?.toKtLightSourceElement(source.treeStructure)
                    } else {
                        null
                    }

                visitor?.visitPrimaryConstructor(
                    declaration,
                    constructorKeyboard ?: objectKeyword ?: getIdentifier(klassSource),
                    enclosingSource = source,
                )
            } else {
                visitor?.visitSecondaryConstructor(
                    declaration,
                    getIdentifier(source),
                    enclosingSource = source,
                )
            }
        }
    }

    private class SemanticSimpleFunctionChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirNamedFunction) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitNamedFunction(
                declaration,
                getIdentifier(source),
                enclosingSource = source,
            )
            visitor?.emitTypeRef(declaration.returnTypeRef)
            declaration.receiverParameter?.typeRef?.let { visitor?.emitTypeRef(it) }
        }
    }

    private class SemanticAnonymousFunctionChecker :
        FirAnonymousFunctionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirAnonymousFunction) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitNamedFunction(declaration, source, enclosingSource = source)
        }
    }

    private class SemanticPropertyChecker : FirPropertyChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirProperty) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitProperty(declaration, getIdentifier(source), enclosingSource = source)
            visitor?.emitTypeRef(declaration.returnTypeRef)
            declaration.receiverParameter?.typeRef?.let { visitor?.emitTypeRef(it) }
        }
    }

    private class SemanticValueParameterChecker : FirValueParameterChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirValueParameter) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitParameter(declaration, getIdentifier(source), enclosingSource = source)
            visitor?.emitTypeRef(declaration.returnTypeRef)
        }
    }

    private class SemanticTypeParameterChecker : FirTypeParameterChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirTypeParameter) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitTypeParameter(
                declaration,
                getIdentifier(source),
                enclosingSource = source,
            )
        }
    }

    private class SemanticTypeAliasChecker : FirTypeAliasChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirTypeAlias) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitTypeAlias(declaration, getIdentifier(source), enclosingSource = source)
        }
    }

    private class SemanticPropertyAccessorChecker :
        FirPropertyAccessorChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirPropertyAccessor) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            val identifierSource =
                if (declaration.isGetter) {
                    source.treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.GET_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure) ?: getIdentifier(source)
                } else if (declaration.isSetter) {
                    source.treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.SET_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure) ?: getIdentifier(source)
                } else {
                    getIdentifier(source)
                }

            visitor?.visitPropertyAccessor(declaration, identifierSource, enclosingSource = source)
        }
    }

    private class SemanticEnumEntryChecker : FirEnumEntryChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirEnumEntry) {
            val source = declaration.source ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitEnumEntry(declaration, getIdentifier(source), enclosingSource = source)
        }
    }

    private class SemanticResolvedQualifierChecker :
        FirResolvedQualifierChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirResolvedQualifier) {
            val symbol = expression.symbol ?: return
            val source = expression.source ?: return
            if (source.kind is KtFakeSourceElementKind) return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitClassReference(symbol, getIdentifier(source))
        }
    }

    private class SemanticQualifiedAccessExpressionChecker :
        FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirQualifiedAccessExpression) {
            val source = expression.source ?: return
            val calleeReference = expression.calleeReference
            if ((calleeReference as? FirResolvedNamedReference) == null) {
                return
            }

            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]
            val identifierSource = getIdentifier(calleeReference.source ?: source)
            visitor?.visitSimpleNameExpression(calleeReference, identifierSource)

            val resolvedSymbol = calleeReference.resolvedSymbol
            if (
                resolvedSymbol.origin == FirDeclarationOrigin.SamConstructor &&
                    resolvedSymbol is FirSyntheticFunctionSymbol
            ) {
                val referencedKlass = resolvedSymbol.resolvedReturnType.toClassLikeSymbol()
                if (referencedKlass != null) {
                    visitor?.visitClassReference(referencedKlass, identifierSource)
                }
            }

            // When encountering a reference to a property symbol, emit both getter and setter
            // symbols
            if (resolvedSymbol is FirPropertySymbol) {
                resolvedSymbol.getterSymbol?.let {
                    visitor?.visitCallableReference(it, identifierSource)
                }
                resolvedSymbol.setterSymbol?.let {
                    visitor?.visitCallableReference(it, identifierSource)
                }
            }
        }
    }

    private class SemanticClassReferenceExpressionChecker :
        FirTypeOperatorCallChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirTypeOperatorCall) {
            val typeRef = expression.conversionTypeRef
            val source = typeRef.source ?: return
            val classSymbol = typeRef.toClassLikeSymbol(context.session) ?: return
            val ktFile = context.containingFileSymbol?.sourceFile ?: return
            val visitor = visitors[ktFile]

            visitor?.visitClassReference(classSymbol, getIdentifier(source))
        }
    }
}
