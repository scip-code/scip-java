package org.scip_code.scip_java.kotlin_analysis

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.types.Variance
import org.scip_code.scip.Document
import org.scip_code.scip.Occurrence
import org.scip_code.scip.SymbolRole
import org.scip_code.scip.relationship
import org.scip_code.scip.signature
import org.scip_code.scip.symbolInformation
import org.scip_code.scip_java.shared.ScipDocumentBuilder
import org.scip_code.scip_java.shared.ScipRange
import org.scip_code.scip_java.shared.ScipShardPaths
import org.scip_code.scip_java.shared.ScipShardWriter

/**
 * Indexes Kotlin sources into SCIP documents using the Kotlin Analysis API in standalone mode.
 *
 * The indexer bundles its own analysis engine, so the Kotlin version of the indexed project only
 * matters as a language-version setting, never as a binary-compatibility constraint.
 *
 * One SCIP shard is written per source file under
 * `<targetroot>/META-INF/scip/<relative-source-path>.scip`, the layout produced by the scip-javac
 * compiler plugin.
 */
class KotlinAnalysisIndexer(
    private val sourceroot: Path,
    private val targetroot: Path,
    private val sourceRoots: List<Path>,
    private val classpath: List<Path> = emptyList(),
    private val jdkHome: Path? = Paths.get(System.getProperty("java.home")),
    private val callback: (Document) -> Unit = {},
) {
    fun run(): List<Document> {
        val disposable = Disposer.newDisposable("scip-kotlin-analysis")
        try {
            val session =
                buildStandaloneAnalysisAPISession(projectDisposable = disposable) {
                    buildKtModuleProvider {
                        platform = JvmPlatforms.defaultJvmPlatform
                        val dependencies = buildList {
                            jdkHome?.let { home ->
                                add(
                                    buildKtSdkModule {
                                        platform = JvmPlatforms.defaultJvmPlatform
                                        addBinaryRootsFromJdkHome(home, isJre = false)
                                        libraryName = "JDK"
                                    }
                                )
                            }
                            if (classpath.isNotEmpty()) {
                                add(
                                    buildKtLibraryModule {
                                        platform = JvmPlatforms.defaultJvmPlatform
                                        addBinaryRoots(classpath)
                                        libraryName = "classpath"
                                    }
                                )
                            }
                        }
                        addModule(
                            buildKtSourceModule {
                                platform = JvmPlatforms.defaultJvmPlatform
                                moduleName = "scip-kotlin-analysis"
                                addSourceRoots(sourceRoots)
                                dependencies.forEach { addRegularDependency(it) }
                            }
                        )
                    }
                }

            val documents = mutableListOf<Document>()
            val ktFiles = session.modulesWithFiles.values.flatten().filterIsInstance<KtFile>()
            for (ktFile in ktFiles) {
                val document = indexFile(ktFile)
                writeShard(ktFile, document)
                callback(document)
                documents.add(document)
            }
            return documents
        } finally {
            Disposer.dispose(disposable)
        }
    }

    private fun indexFile(ktFile: KtFile): Document {
        val cache = SymbolsCache()
        val text = ktFile.text
        val lines = LineIndex(text)
        val builder = ScipDocumentBuilder()
        analyze(ktFile) { ktFile.accept(IndexingVisitor(this, cache, lines, builder)) }
        val relativePath =
            ScipShardPaths.relativePath(sourceroot, Paths.get(ktFile.virtualFilePath))
        return builder.build("kotlin", relativePath, text)
    }

    private fun writeShard(ktFile: KtFile, document: Document) {
        val absolutePath = Paths.get(ktFile.virtualFilePath).normalize()
        val shardPath = ScipShardPaths.shardPath(targetroot, sourceroot, absolutePath)
        if (shardPath.isPresent) {
            ScipShardWriter.writeShard(shardPath.get(), document)
        } else {
            System.err.println(
                "given file is not under the sourceroot.\n\tSourceroot: $sourceroot\n\tFile path: $absolutePath"
            )
        }
    }

    @OptIn(KaExperimentalApi::class)
    private class IndexingVisitor(
        private val session: KaSession,
        private val cache: SymbolsCache,
        private val lines: LineIndex,
        private val out: ScipDocumentBuilder,
    ) : KtTreeVisitorVoid() {
        private val signatures = SignatureRenderer(session)

        override fun visitDeclaration(dcl: KtDeclaration) {
            // Emit before visiting children so same-range occurrence order and
            // local-symbol numbering match scip-kotlinc.
            when (dcl) {
                is KtEnumEntry -> emitEnumEntry(dcl)
                is KtClassOrObject -> emitClassLike(dcl)
                is KtConstructor<*> -> emitConstructor(dcl)
                is KtNamedFunction -> emitFunction(dcl)
                is KtProperty -> emitProperty(dcl)
                is KtParameter -> emitParameter(dcl)
                is KtPropertyAccessor -> emitExplicitAccessor(dcl)
                is KtTypeParameter -> emitTypeParameter(dcl)
                is KtTypeAlias -> emitTypeAlias(dcl)
                is KtDestructuringDeclarationEntry -> emitDestructuringEntry(dcl)
                else -> {}
            }
            super.visitDeclaration(dcl)
        }

        override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
            super.visitSimpleNameExpression(expression)
            val referencedName = expression.getReferencedName()
            if (referencedName == "this" || referencedName == "super") return
            val target = with(session) { expression.mainReference.resolveToSymbol() } ?: return
            val range = expression.getReferencedNameElement().textRange

            val targetPsi = target.psi
            if (
                targetPsi is KtFunctionLiteral &&
                    target is KaCallableSymbol &&
                    target !is KaFunctionSymbol
            ) {
                emitImplicitItDefinition(targetPsi, target)
                addReference(cache.implicitItSymbol(targetPsi), range)
                return
            }

            // Super-type entries and annotations reference the class, not the
            // constructor (`class Seagull : Bird()` references `Bird#`).
            if (
                target is KaConstructorSymbol &&
                    expression.parents
                        .takeWhile { it !is KtFile }
                        .any { it is KtConstructorCalleeExpression }
            ) {
                addReference(cache.constructorOwnerSymbol(session, target), range)
                return
            }

            // Property references fan out to the property and its accessors
            // (`banana` → `Class#banana.`, `Class#getBanana().`, `Class#setBanana().`).
            if (target is KaPropertySymbol) {
                propertyReferenceSymbols(target).forEach { addReference(it, range) }
                return
            }

            addReference(cache.symbolForReference(session, target), range)
        }

        private fun emitClassLike(declaration: KtClassOrObject) {
            val isAnonymous = declaration is KtObjectDeclaration && declaration.isObjectLiteral()
            val range =
                when {
                    declaration.nameIdentifier != null -> declaration.nameIdentifier!!.textRange
                    isAnonymous ->
                        declaration.getObjectKeyword()?.textRange ?: declaration.textRange
                    else -> declaration.textRange // unnamed companion object
                }
            val displayName = if (isAnonymous) "<anonymous>" else declaration.name ?: "Companion"
            val symbol = cache.symbolForDeclaration(declaration)
            emitDefinition(
                symbol = symbol,
                range = range,
                enclosing = declaration.textRange,
                displayName = displayName,
                signatureText = signatures.classSignature(declaration),
                documentation = docComment(declaration),
                relationships = superTypeRelationships(declaration),
            )
            // Classes and objects (not interfaces) get an implicit primary constructor
            // when none is present. A keyword-less primary constructor also anchors at
            // the class identifier; both are emitted here so the class definition stays
            // first at that range (emitConstructor skips the keyword-less case).
            val isInterface = declaration is KtClass && declaration.isInterface()
            val primaryConstructor = declaration.primaryConstructor
            if (!isInterface && primaryConstructor == null) {
                emitDefinition(
                    symbol = cache.implicitConstructorSymbol(declaration),
                    range = range,
                    enclosing = declaration.textRange,
                    displayName = displayName,
                    signatureText = signatures.implicitConstructorSignature(declaration),
                    documentation = docComment(declaration),
                )
            } else if (
                primaryConstructor != null && primaryConstructor.getConstructorKeyword() == null
            ) {
                emitDefinition(
                    symbol = cache.symbolForDeclaration(primaryConstructor),
                    range = range,
                    // Span from the definition anchor through the parameter list so the
                    // enclosing range contains its own anchor (scip-kotlinc's
                    // parameter-list-only range did not).
                    enclosing =
                        TextRange(range.startOffset, primaryConstructor.textRange.endOffset),
                    displayName = displayName,
                    signatureText = signatures.constructorSignature(primaryConstructor),
                    documentation = docComment(primaryConstructor),
                )
            }
            if (declaration is KtClass && declaration.isEnum()) {
                emitEnumSynthetics(declaration, range)
            }
            if (declaration is KtClass && declaration.isData()) {
                declaration.primaryConstructor?.let { constructor ->
                    emitDefinition(
                        symbol = cache.memberMethodSymbol(declaration, "copy"),
                        range = constructor.textRange,
                        enclosing = constructor.textRange,
                        displayName = "copy",
                        signatureText = signatures.dataCopySignature(declaration),
                        documentation = null,
                    )
                }
            }
        }

        /**
         * Compiler-generated enum members — `values()`, `valueOf(value)`, `entries` and its getter
         * — anchored at the enum class identifier. Unlike scip-kotlinc, the entries getter is owned
         * by the enum class; the old package-owned symbol was a bug.
         */
        private fun emitEnumSynthetics(declaration: KtClass, range: TextRange) {
            val name = declaration.name.orEmpty()
            val enclosing = declaration.textRange
            emitDefinition(
                symbol = cache.memberMethodSymbol(declaration, "values"),
                range = range,
                enclosing = enclosing,
                displayName = "values",
                signatureText = signatures.enumValuesSignature(name),
                documentation = null,
            )
            val valueOf = cache.memberMethodSymbol(declaration, "valueOf")
            emitDefinition(
                symbol = valueOf,
                range = range,
                enclosing = enclosing,
                displayName = "valueOf",
                signatureText = signatures.enumValueOfSignature(name),
                documentation = null,
            )
            emitDefinition(
                symbol = cache.methodParameterSymbol(valueOf, "value"),
                range = range,
                enclosing = enclosing,
                displayName = "value",
                signatureText = "value: String",
                documentation = null,
            )
            emitDefinition(
                symbol = cache.memberTermSymbol(declaration, "entries"),
                range = range,
                enclosing = enclosing,
                displayName = "entries",
                signatureText = signatures.enumEntriesSignature(name),
                documentation = null,
            )
            emitDefinition(
                symbol = cache.memberMethodSymbol(declaration, "getEntries"),
                range = range,
                enclosing = enclosing,
                displayName = "entries",
                signatureText = signatures.enumGetEntriesSignature(name),
                documentation = null,
            )
        }

        private fun emitEnumEntry(declaration: KtEnumEntry) {
            val identifier = declaration.nameIdentifier ?: return
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.propertySignature(declaration),
                documentation = docComment(declaration),
            )
        }

        private fun emitConstructor(declaration: KtConstructor<*>) {
            val range =
                when (declaration) {
                    // Keyword-less primary constructors are emitted by emitClassLike.
                    is KtPrimaryConstructor ->
                        declaration.getConstructorKeyword()?.textRange ?: return
                    is KtSecondaryConstructor -> declaration.textRange
                    else -> return
                }
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = range,
                enclosing = declaration.textRange,
                displayName = declaration.containingClassOrObject?.name ?: "<anonymous>",
                signatureText = signatures.constructorSignature(declaration),
                documentation = docComment(declaration),
            )
        }

        private fun emitFunction(declaration: KtNamedFunction) {
            val identifier = declaration.nameIdentifier ?: return
            val relationships =
                if (declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
                    overriddenSymbols(declaration)
                } else {
                    emptyList()
                }
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.functionSignature(declaration),
                documentation = docComment(declaration),
                relationships = relationships,
            )
        }

        private fun emitProperty(declaration: KtProperty) {
            val identifier = declaration.nameIdentifier ?: return
            val symbol = cache.symbolForDeclaration(declaration)
            if (symbol.isLocal()) {
                emitDefinition(
                    symbol = symbol,
                    range = identifier.textRange,
                    enclosing = declaration.textRange,
                    displayName = declaration.name.orEmpty(),
                    signatureText = signatures.localVariableSignature(declaration),
                    documentation = docComment(declaration),
                )
                return
            }
            emitDefinition(
                symbol = symbol,
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.propertySignature(declaration),
                documentation = docComment(declaration),
            )
            emitSyntheticAccessors(
                property = declaration,
                nameRange = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                hasExplicitGetter = declaration.getter != null,
                hasExplicitSetter = declaration.setter != null,
                isVar = declaration.isVar,
            )
        }

        private fun emitParameter(declaration: KtParameter) {
            val loopOrCatchIdentifier = declaration.nameIdentifier
            // `for` loop variables and `catch` parameters are local declarations,
            // numbered at the declaration site like in the FIR indexer.
            if (loopOrCatchIdentifier != null) {
                if (declaration.parent is KtForExpression) {
                    val type =
                        declaration.typeReference?.text
                            ?: renderedParameterType(declaration)
                            ?: "Any"
                    emitDefinition(
                        symbol = cache.symbolForDeclaration(declaration),
                        range = loopOrCatchIdentifier.textRange,
                        enclosing = declaration.textRange,
                        displayName = declaration.name.orEmpty(),
                        signatureText = "local val ${declaration.name.orEmpty()}: $type",
                        documentation = null,
                    )
                    return
                }
                if ((declaration.parent as? KtParameterList)?.parent is KtCatchClause) {
                    emitDefinition(
                        symbol = cache.symbolForDeclaration(declaration),
                        range = loopOrCatchIdentifier.textRange,
                        enclosing = declaration.textRange,
                        displayName = declaration.name.orEmpty(),
                        signatureText = signatures.parameterSignature(declaration),
                        documentation = null,
                    )
                    return
                }
            }
            // Parameters of function types are not declarations of their own.
            if (!cache.isDeclarationParameter(declaration)) return
            val identifier = declaration.nameIdentifier ?: return
            val symbol = cache.symbolForDeclaration(declaration)
            emitDefinition(
                symbol = symbol,
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.parameterSignature(declaration),
                documentation = null,
            )
            // A `val`/`var` constructor parameter also declares a property; it and
            // its synthetic accessors are all anchored at the parameter identifier,
            // which the property initializer also references.
            if (declaration.hasValOrVar()) {
                emitDefinition(
                    symbol = cache.parameterPropertySymbol(declaration),
                    range = identifier.textRange,
                    enclosing = declaration.textRange,
                    displayName = declaration.name.orEmpty(),
                    signatureText = signatures.propertySignature(declaration),
                    documentation = null,
                )
                addReference(symbol, identifier.textRange)
                emitSyntheticAccessors(
                    property = declaration,
                    nameRange = identifier.textRange,
                    enclosing = declaration.textRange,
                    displayName = declaration.name.orEmpty(),
                    hasExplicitGetter = false,
                    hasExplicitSetter = false,
                    isVar = declaration.isMutable,
                )
                emitDataClassParameterSynthetics(declaration, identifier.textRange)
            }
        }

        /**
         * A data-class constructor parameter additionally declares `componentN()` and a `copy()`
         * parameter, and the generated `copy()` default value reads the property — all anchored at
         * the parameter identifier, mirroring scip-kotlinc.
         */
        private fun emitDataClassParameterSynthetics(declaration: KtParameter, range: TextRange) {
            val constructor = declaration.parent?.parent as? KtPrimaryConstructor ?: return
            val containingClass = constructor.containingClassOrObject as? KtClass ?: return
            if (!containingClass.isData()) return
            val index = constructor.valueParameters.indexOf(declaration)
            if (index < 0) return
            emitDefinition(
                symbol = cache.memberMethodSymbol(containingClass, "component${index + 1}"),
                range = range,
                enclosing = declaration.textRange,
                displayName = "component${index + 1}",
                signatureText = signatures.dataComponentSignature(index + 1, declaration),
                documentation = null,
            )
            emitDefinition(
                symbol =
                    cache.methodParameterSymbol(
                        cache.memberMethodSymbol(containingClass, "copy"),
                        declaration.name.orEmpty(),
                    ),
                range = range,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.dataCopyParameterSignature(declaration),
                documentation = null,
            )
            addReference(cache.parameterPropertySymbol(declaration), range)
            addReference(cache.syntheticAccessorSymbol(declaration, setter = false), range)
            if (declaration.isMutable) {
                addReference(cache.syntheticAccessorSymbol(declaration, setter = true), range)
            }
        }

        private fun emitTypeAlias(declaration: KtTypeAlias) {
            val identifier = declaration.nameIdentifier ?: return
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.typeAliasSignature(declaration),
                documentation = docComment(declaration),
            )
        }

        /**
         * `when (subject)` introduces a synthetic `<when-subject>` local in the FIR desugaring,
         * defined at the subject expression. Emitted before visiting children so local numbering
         * matches.
         */
        override fun visitWhenExpression(expression: KtWhenExpression) {
            val subject = expression.subjectExpression
            if (subject != null && expression.subjectVariable == null) {
                emitDefinition(
                    symbol = cache.syntheticLocalSymbol(expression),
                    range = subject.textRange,
                    enclosing = subject.textRange,
                    displayName = "<when-subject>",
                    signatureText =
                        signatures.whenSubjectSignature(renderedExpressionType(subject)),
                    documentation = null,
                )
            }
            super.visitWhenExpression(expression)
        }

        /**
         * Destructuring introduces a synthetic `<destruct>` local holding the destructured value;
         * each entry references its `componentN()` function and the `<destruct>` local. Emitted
         * before visiting children so local numbering matches.
         */
        override fun visitDestructuringDeclaration(
            destructuringDeclaration: KtDestructuringDeclaration
        ) {
            val isParameter = destructuringDeclaration.parent is KtParameter
            val type =
                destructuringDeclaration.initializer?.let { renderedExpressionType(it) }
                    ?: (destructuringDeclaration.parent as? KtParameter)?.let {
                        renderedParameterType(it)
                    }
                    ?: "Any"
            emitDefinition(
                symbol = cache.syntheticLocalSymbol(destructuringDeclaration),
                range = destructuringDeclaration.textRange,
                enclosing = destructuringDeclaration.textRange,
                displayName = "<destruct>",
                signatureText = signatures.destructSignature(type, isParameter),
                documentation = null,
            )
            super.visitDestructuringDeclaration(destructuringDeclaration)
        }

        private fun emitDestructuringEntry(declaration: KtDestructuringDeclarationEntry) {
            emitLocalVariable(declaration)
            val range = declaration.nameIdentifier?.textRange ?: return
            val destructuring = declaration.parent as? KtDestructuringDeclaration ?: return
            // Entries desugar to `componentN()` calls on the destructured value.
            // Standalone analysis does not resolve destructuring-entry references, so
            // the symbol is derived from the destructured type and entry position.
            val index = destructuring.entries.indexOf(declaration)
            destructuredClassId(destructuring)?.let { classId ->
                val name = "component${index + 1}"
                // `componentN` may be a member (data classes, Pair) or an extension
                // (e.g. Map.Entry's componentN lives in kotlin.collections).
                val isMember =
                    with(session) {
                        findClass(classId)
                            ?.declaredMemberScope
                            ?.callables(Name.identifier(name))
                            ?.any() == true
                    }
                val symbol =
                    if (isMember) {
                        Symbol.createGlobal(
                            cache.classSymbol(classId),
                            ScipSymbolDescriptor(ScipSymbolDescriptor.Kind.METHOD, name),
                        )
                    } else {
                        with(session) {
                            findTopLevelCallables(classId.packageFqName, Name.identifier(name))
                                .firstOrNull { extension ->
                                    (extension.receiverParameter?.returnType as? KaClassType)
                                        ?.classId == classId
                                }
                                ?.let { cache.symbolForReference(session, it) }
                        } ?: Symbol.NONE
                    }
                addReference(symbol, range)
            }
            addReference(cache.syntheticLocalSymbol(destructuring), range)
        }

        private fun destructuredClassId(destructuring: KtDestructuringDeclaration): ClassId? =
            with(session) {
                val type =
                    destructuring.initializer?.expressionType
                        ?: (destructuring.parent as? KtParameter)?.let {
                            (it.symbol as? KaCallableSymbol)?.returnType
                        }
                (type as? KaClassType)?.classId
            }

        private fun renderedExpressionType(expression: KtExpression): String =
            with(session) {
                expression.expressionType?.render(
                    KaTypeRendererForSource.WITH_SHORT_NAMES,
                    position = Variance.INVARIANT,
                )
            } ?: "Any"

        private fun renderedParameterType(parameter: KtParameter): String? =
            with(session) {
                (parameter.symbol as? KaCallableSymbol)
                    ?.returnType
                    ?.render(
                        KaTypeRendererForSource.WITH_SHORT_NAMES,
                        position = Variance.INVARIANT,
                    )
            }

        private fun emitSyntheticAccessors(
            property: KtDeclaration,
            nameRange: TextRange,
            enclosing: TextRange,
            displayName: String,
            hasExplicitGetter: Boolean,
            hasExplicitSetter: Boolean,
            isVar: Boolean,
        ) {
            if (!hasExplicitGetter) {
                emitDefinition(
                    symbol = cache.syntheticAccessorSymbol(property, setter = false),
                    range = nameRange,
                    enclosing = enclosing,
                    displayName = displayName,
                    signatureText = signatures.syntheticAccessorSignature(property, setter = false),
                    documentation = null,
                )
            }
            if (isVar && !hasExplicitSetter) {
                emitDefinition(
                    symbol = cache.syntheticAccessorSymbol(property, setter = true),
                    range = nameRange,
                    enclosing = enclosing,
                    displayName = displayName,
                    signatureText = signatures.syntheticAccessorSignature(property, setter = true),
                    documentation = null,
                )
                emitDefinition(
                    symbol = cache.syntheticSetterValueSymbol(property),
                    range = nameRange,
                    enclosing = enclosing,
                    displayName = "value",
                    signatureText = signatures.setterValueSignature(property),
                    documentation = null,
                )
            }
        }

        private fun emitExplicitAccessor(declaration: KtPropertyAccessor) {
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = declaration.namePlaceholder.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.property.name.orEmpty(),
                signatureText = signatures.accessorSignature(declaration),
                documentation = docComment(declaration),
            )
        }

        private fun emitTypeParameter(declaration: KtTypeParameter) {
            val identifier = declaration.nameIdentifier ?: return
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = declaration.text,
                documentation = null,
            )
        }

        private fun emitLocalVariable(declaration: KtVariableDeclaration) {
            val identifier = declaration.nameIdentifier ?: return
            emitDefinition(
                symbol = cache.symbolForDeclaration(declaration),
                range = identifier.textRange,
                enclosing = declaration.textRange,
                displayName = declaration.name.orEmpty(),
                signatureText = signatures.localVariableSignature(declaration),
                documentation = null,
            )
        }

        private val emittedItParameters = HashSet<KtFunctionLiteral>()

        /**
         * The implicit `it` parameter has no declaration PSI: its definition is anchored at the
         * whole lambda literal and emitted at the first reference.
         */
        private fun emitImplicitItDefinition(literal: KtFunctionLiteral, target: KaCallableSymbol) {
            if (!emittedItParameters.add(literal)) return
            emitDefinition(
                symbol = cache.implicitItSymbol(literal),
                range = literal.textRange,
                enclosing = literal.textRange,
                displayName = "it",
                signatureText = signatures.implicitItSignature(target),
                documentation = null,
            )
        }

        private fun propertyReferenceSymbols(target: KaPropertySymbol): List<Symbol> {
            return when (val psi = target.psi) {
                is KtProperty -> {
                    val property = cache.symbolForDeclaration(psi)
                    if (property.isLocal()) return listOf(property)
                    buildList {
                        add(property)
                        psi.getter?.let { add(cache.symbolForDeclaration(it)) }
                            ?: add(cache.syntheticAccessorSymbol(psi, setter = false))
                        if (psi.isVar) {
                            psi.setter?.let { add(cache.symbolForDeclaration(it)) }
                                ?: add(cache.syntheticAccessorSymbol(psi, setter = true))
                        }
                    }
                }
                is KtParameter ->
                    buildList {
                        add(cache.parameterPropertySymbol(psi))
                        add(cache.syntheticAccessorSymbol(psi, setter = false))
                        if (psi.isMutable) {
                            add(cache.syntheticAccessorSymbol(psi, setter = true))
                        }
                    }
                else ->
                    buildList {
                        add(cache.symbolForReference(session, target))
                        add(cache.externalAccessorSymbol(session, target, setter = false))
                        if (!target.isVal) {
                            add(cache.externalAccessorSymbol(session, target, setter = true))
                        }
                    }
            }.filter { it != Symbol.NONE }
        }

        private fun superTypeRelationships(declaration: KtClassOrObject): List<Symbol> =
            with(session) {
                val classSymbol = declaration.symbol as? KaClassSymbol ?: return@with emptyList()
                classSymbol.superTypes
                    .mapNotNull { (it as? KaClassType)?.classId }
                    .filterNot { it == ANY_CLASS_ID }
                    .map { cache.classSymbol(it) }
                    .filter { it != Symbol.NONE }
            }

        private fun overriddenSymbols(declaration: KtNamedFunction): List<Symbol> =
            with(session) {
                val symbol = declaration.symbol as? KaCallableSymbol ?: return@with emptyList()
                symbol.directlyOverriddenSymbols
                    .map { cache.symbolForReference(session, it) }
                    .filter { it != Symbol.NONE }
                    .toList()
            }

        private fun emitDefinition(
            symbol: Symbol,
            range: TextRange,
            enclosing: TextRange?,
            displayName: String,
            signatureText: String?,
            documentation: String?,
            relationships: List<Symbol> = emptyList(),
        ) {
            if (symbol == Symbol.NONE) return
            val scipRange = lines.anchorRange(range) ?: return
            val builder =
                Occurrence.newBuilder()
                    .setSymbol(symbol.toString())
                    .setSymbolRoles(SymbolRole.Definition.number)
            builder.setSingleLineRange(scipRange.toSingleLineRange())
            enclosing
                ?.let { lines.scipRange(it) }
                ?.let { enclosingRange ->
                    if (enclosingRange.isSingleLine) {
                        builder.setSingleLineEnclosingRange(enclosingRange.toSingleLineRange())
                    } else {
                        builder.setMultiLineEnclosingRange(enclosingRange.toMultiLineRange())
                    }
                }
            out.addOccurrence(builder.build())
            out.addSymbol(
                symbolInformation {
                    this.symbol = symbol.toString()
                    this.displayName = displayName
                    if (signatureText != null) {
                        this.signatureDocumentation = signature {
                            language = "kotlin"
                            text = signatureText
                        }
                    }
                    if (documentation != null) {
                        this.documentation += documentation
                    }
                    for (parent in relationships) {
                        this.relationships += relationship {
                            this.symbol = parent.toString()
                            this.isImplementation = true
                        }
                    }
                }
            )
        }

        private fun addReference(symbol: Symbol, range: TextRange) {
            if (symbol == Symbol.NONE) return
            val scipRange = lines.anchorRange(range) ?: return
            val builder = Occurrence.newBuilder().setSymbol(symbol.toString())
            builder.setSingleLineRange(scipRange.toSingleLineRange())
            out.addOccurrence(builder.build())
        }

        private fun docComment(declaration: KtDeclaration): String? {
            val kdoc = declaration.docComment?.text ?: return null
            return stripKdoc(kdoc).ifEmpty { null }
        }

        companion object {
            private val ANY_CLASS_ID = ClassId(FqName("kotlin"), Name.identifier("Any"))
        }
    }
}

/** Strips the `/**`, leading `*`s, and `*/` from a kdoc block, returning just the body text. */
internal fun stripKdoc(kdoc: String): String {
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

/** Maps text offsets to 0-based line/character positions. */
internal class LineIndex(private val text: String) {
    private val lineStartOffsets: IntArray

    init {
        val offsets = ArrayList<Int>()
        offsets.add(0)
        for (index in text.indices) {
            if (text[index] == '\n') offsets.add(index + 1)
        }
        lineStartOffsets = offsets.toIntArray()
    }

    fun scipRange(range: TextRange): ScipRange? {
        if (range.startOffset > text.length || range.endOffset > text.length) return null
        val (startLine, startCharacter) = position(range.startOffset)
        val (endLine, endCharacter) = position(range.endOffset)
        return ScipRange.range(startLine, startCharacter, endLine, endCharacter)
    }

    /**
     * Occurrence anchors are always single-line, like in scip-kotlinc: multiline declarations are
     * collapsed onto their start line with the end character measured from that line's start. The
     * true extent lives in the enclosing range.
     */
    fun anchorRange(range: TextRange): ScipRange? {
        if (range.startOffset > text.length || range.endOffset > text.length) return null
        val (startLine, startCharacter) = position(range.startOffset)
        val (endLine, endCharacter) = position(range.endOffset)
        if (startLine == endLine) {
            return ScipRange.singleLine(startLine, startCharacter, endCharacter)
        }
        val flattenedEnd = range.endOffset - lineStart(startLine)
        return ScipRange.singleLine(startLine, startCharacter, flattenedEnd)
    }

    private fun lineStart(line: Int): Int = lineStartOffsets[line]

    private fun position(offset: Int): Pair<Int, Int> {
        var line = Arrays.binarySearch(lineStartOffsets, offset)
        if (line < 0) line = -line - 2
        return line to (offset - lineStartOffsets[line])
    }
}
