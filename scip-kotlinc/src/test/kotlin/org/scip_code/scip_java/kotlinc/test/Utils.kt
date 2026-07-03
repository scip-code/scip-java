package org.scip_code.scip_java.kotlinc.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import java.nio.file.Paths
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.scip_code.scip.Document
import org.scip_code.scip.Occurrence
import org.scip_code.scip.SymbolInformation
import org.scip_code.scip_java.kotlinc.*
import org.scip_code.scip_java.kotlinc.AnalyzerCheckers.Companion.visitors
import org.scip_code.scip_java.shared.ScipOptions

data class ExpectedSymbols(
    val testName: String,
    val source: SourceFile,
    val symbolsCacheData: SymbolCacheData? = null,
    val scip: ScipData? = null,
) {
    /** Subset of a SCIP [Document] that a single test wants to assert on. */
    data class ScipData(
        val expectedOccurrences: List<Occurrence>? = null,
        val expectedSymbols: List<SymbolInformation>? = null,
    )

    data class SymbolCacheData(
        val expectedGlobals: List<Symbol>? = null,
        val localsCount: Int? = null,
    )
}

fun SourceFile.Companion.testKt(@Language("kotlin") contents: String): SourceFile =
    kotlin("Test.kt", contents)

@ExperimentalCompilerApi
fun List<ExpectedSymbols>.mapCheckExpectedSymbols(): List<DynamicTest> =
    this.flatMap { (testName, source, symbolsData, scipData) ->
        val globals = GlobalSymbolsCache(testing = true)
        val locals = LocalSymbolsCache()
        lateinit var document: Document
        val compilation = configureTestCompiler(source, globals, locals) { document = it }
        listOf(
            dynamicTest("$testName - compilation") {
                val result = shouldNotThrowAny { compilation.compile() }
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            },
            dynamicTest("$testName - symbols") {
                symbolsData?.apply {
                    println(
                        "checking symbols: ${expectedGlobals?.size ?: 0} globals and presence of $localsCount locals"
                    )
                    checkContainsExpectedSymbols(globals, locals, expectedGlobals, localsCount)
                } ?: assumeFalse(true)
            },
            dynamicTest("$testName - scip") {
                scipData?.apply {
                    println(
                        "checking scip: ${expectedOccurrences?.size ?: 0} occurrences and ${expectedSymbols?.size ?: 0} symbols"
                    )
                    checkContainsExpectedScip(document, expectedOccurrences, expectedSymbols)
                } ?: assumeFalse(true)
            },
        )
    }

fun checkContainsExpectedSymbols(
    globals: GlobalSymbolsCache,
    locals: LocalSymbolsCache,
    expectedGlobals: List<Symbol>?,
    localsCount: Int? = null,
) {
    assertSoftly(globals) { expectedGlobals?.let { this.shouldContainInOrder(it) } }
    localsCount?.also { locals.size shouldBe it }
}

fun checkContainsExpectedScip(
    document: Document,
    expectedOccurrences: List<Occurrence>?,
    expectedSymbols: List<SymbolInformation>?,
) {
    assertSoftly(document.occurrencesList) {
        expectedOccurrences?.let { this.shouldContainInOrder(it) }
    }
    assertSoftly(document.symbolsList) { expectedSymbols?.let { this.shouldContainInOrder(it) } }
}

@OptIn(ExperimentalCompilerApi::class)
private fun configureTestCompiler(
    source: SourceFile,
    globals: GlobalSymbolsCache,
    locals: LocalSymbolsCache,
    hook: (Document) -> Unit = {},
): KotlinCompilation {
    val compilation =
        KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            verbose = false
        }

    val analyzer = scipVisitorAnalyzer(globals, locals, compilation.workingDir.toPath(), hook)
    compilation.apply { compilerPluginRegistrars = listOf(analyzer) }
    return compilation
}

private class TestAnalyzerDeclarationCheckers(
    globals: GlobalSymbolsCache,
    locals: LocalSymbolsCache,
    sourceRoot: Path,
) : AnalyzerCheckers.AnalyzerDeclarationCheckers(sourceRoot) {
    override val fileCheckers: Set<FirFileChecker> =
        setOf(
            object : FirFileChecker(MppCheckerKind.Common) {
                context(context: CheckerContext, reporter: DiagnosticReporter)
                override fun check(declaration: FirFile) {
                    val ktFile = declaration.sourceFile ?: return
                    val lineMap = LineMap(declaration)
                    val visitor = ScipVisitor(sourceRoot, ktFile, lineMap, globals, locals)
                    visitors[ktFile] = visitor
                }
            },
            AnalyzerCheckers.SemanticImportsChecker(),
        )
}

private class TestAnalyzerCheckers(session: FirSession) : AnalyzerCheckers(session) {
    override val declarationCheckers: DeclarationCheckers
        get() =
            TestAnalyzerDeclarationCheckers(
                session.testAnalyzerParamsProvider.globals,
                session.testAnalyzerParamsProvider.locals,
                session.testAnalyzerParamsProvider.sourceroot,
            )
}

class TestAnalyzerParamsProvider(
    session: FirSession,
    var globals: GlobalSymbolsCache,
    var locals: LocalSymbolsCache,
    sourceroot: Path,
) : AnalyzerParamsProvider(session, ScipOptions().apply { this.sourceroot = sourceroot }) {
    companion object {
        fun getFactory(
            globals: GlobalSymbolsCache,
            locals: LocalSymbolsCache,
            sourceroot: Path,
        ): Factory {
            return Factory { TestAnalyzerParamsProvider(it, globals, locals, sourceroot) }
        }
    }
}

val FirSession.testAnalyzerParamsProvider: TestAnalyzerParamsProvider by
    FirSession.sessionComponentAccessor()

@OptIn(ExperimentalCompilerApi::class)
fun scipVisitorAnalyzer(
    globals: GlobalSymbolsCache,
    locals: LocalSymbolsCache,
    sourceroot: Path,
    hook: (Document) -> Unit = {},
): CompilerPluginRegistrar {
    return object : CompilerPluginRegistrar() {
        override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
            FirExtensionRegistrarAdapter.registerExtension(
                object : FirExtensionRegistrar() {
                    override fun ExtensionRegistrarContext.configurePlugin() {
                        +TestAnalyzerParamsProvider.getFactory(globals, locals, sourceroot)
                        +::TestAnalyzerCheckers
                    }
                }
            )
            IrGenerationExtension.registerExtension(
                PostAnalysisExtension(
                    sourceRoot = sourceroot,
                    targetRoot = Paths.get(""),
                    callback = hook,
                )
            )
        }

        override val pluginId = PLUGIN_ID

        override val supportsK2: Boolean
            get() = true
    }
}
