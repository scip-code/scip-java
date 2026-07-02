package org.scip_code.scip_java.kotlin_analysis

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
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
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parents
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
 * Unlike scip-kotlinc, this indexer does not run inside the user's kotlinc process: it bundles its
 * own analysis engine, so the Kotlin version of the indexed project only matters as a
 * language-version setting, never as a binary compatibility constraint.
 *
 * One SCIP shard is written per source file under
 * `<targetroot>/META-INF/scip/<relative-source-path>.scip`, matching the layout produced by the
 * scip-javac and scip-kotlinc compiler plugins.
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

    private class IndexingVisitor(
        private val session: KaSession,
        private val cache: SymbolsCache,
        private val lines: LineIndex,
        private val out: ScipDocumentBuilder,
    ) : KtTreeVisitorVoid() {
        private val signatures = SignatureRenderer(session)

        override fun visitDeclaration(dcl: KtDeclaration) {
            super.visitDeclaration(dcl)
            when (dcl) {
                is KtEnumEntry -> emitEnumEntry(dcl)
                is KtClassOrObject -> emitClassLike(dcl)
                is KtConstructor<*> -> emitConstructor(dcl)
                is KtNamedFunction -> emitFunction(dcl)
                is KtProperty -> emitProperty(dcl)
                is KtParameter -> emitParameter(dcl)
                is KtPropertyAccessor -> emitExplicitAccessor(dcl)
                is KtTypeParameter -> emitTypeParameter(dcl)
                is KtDestructuringDeclarationEntry -> emitLocalVariable(dcl)
                else -> {}
            }
        }

        override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
            super.visitSimpleNameExpression(expression)
            val referencedName = expression.getReferencedName()
            if (referencedName == "this" || referencedName == "super") return
            val target = with(session) { expression.mainReference.resolveToSymbol() } ?: return
            val range = expression.getReferencedNameElement().textRange

            // The implicit `it` lambda parameter has no declaration PSI; its
            // definition is anchored at the whole lambda literal, emitted at the
            // first reference.
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

            // Property references fan out to the property and its accessors,
            // e.g. `banana` yields `Class#banana.`, `Class#getBanana().` and
            // `Class#setBanana().`.
            if (target is KaPropertySymbol) {
                propertyReferenceSymbols(target).forEach { addReference(it, range) }
                return
            }

            addReference(cache.symbolForReference(session, target), range)
        }

        // ---------------------------------------------------------------
        // Declarations
        // ---------------------------------------------------------------

        private fun emitClassLike(declaration: KtClassOrObject) {
            val isAnonymous = declaration is KtObjectDeclaration && declaration.isObjectLiteral()
            val range =
                when {
                    declaration.nameIdentifier != null -> declaration.nameIdentifier!!.textRange
                    isAnonymous ->
                        (declaration as KtObjectDeclaration).getObjectKeyword()?.textRange
                            ?: declaration.textRange
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
            // Classes and objects (but not interfaces) declare an implicit primary
            // constructor when no explicit one is present in source.
            val isInterface = declaration is KtClass && declaration.isInterface()
            if (!isInterface && declaration.primaryConstructor == null) {
                emitDefinition(
                    symbol = cache.implicitConstructorSymbol(declaration),
                    range = range,
                    enclosing = declaration.textRange,
                    displayName = displayName,
                    signatureText = signatures.implicitConstructorSignature(declaration),
                    documentation = docComment(declaration),
                )
            }
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
                    is KtPrimaryConstructor ->
                        declaration.getConstructorKeyword()?.textRange
                            ?: declaration.containingClassOrObject?.nameIdentifier?.textRange
                            ?: return
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
                if (declaration.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.OVERRIDE_KEYWORD)) {
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
            // Parameters of function types, catch clauses and for loops are not
            // declarations of their own.
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
            // A `val`/`var` constructor parameter also declares a property: the
            // property initializer references the parameter at the same range, and
            // the property plus its synthetic accessors are all anchored there.
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
            }
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
         * A lambda with a single implicit `it` parameter declares it without any PSI: the FIR
         * indexer anchored its definition at the whole lambda literal.
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

        // ---------------------------------------------------------------
        // Symbol helpers
        // ---------------------------------------------------------------

        private fun propertyReferenceSymbols(target: KaPropertySymbol): List<Symbol> {
            val psi = target.psi
            return when {
                psi is KtProperty -> {
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
                psi is KtParameter ->
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

        // ---------------------------------------------------------------
        // Emission
        // ---------------------------------------------------------------

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
     * Occurrence anchor ranges are always single-line in the scip-kotlinc output: multiline
     * declarations (unnamed companion objects, multiline secondary constructors) were collapsed
     * onto their start line with the end character measured from that line's start. The true extent
     * lives in the enclosing range. Reproduced here so anchors stay byte-identical and renderable
     * by `scip snapshot`.
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
        var line = java.util.Arrays.binarySearch(lineStartOffsets, offset)
        if (line < 0) line = -line - 2
        return line to (offset - lineStartOffsets[line])
    }
}
