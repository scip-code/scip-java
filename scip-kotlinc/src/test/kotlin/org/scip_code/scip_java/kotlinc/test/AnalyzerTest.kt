package org.scip_code.scip_java.kotlinc.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.fail
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.io.TempDir
import org.scip_code.scip.Document
import org.scip_code.scip.SymbolInformation.Kind
import org.scip_code.scip_java.kotlinc.*

@OptIn(ExperimentalCompilerApi::class)
class AnalyzerTest {
    fun compileScip(path: Path, @Language("kotlin") code: String): Document {
        val buildPath = File(path.resolve("build").toString()).apply { mkdir() }
        val source = SourceFile.testKt(code)
        lateinit var document: Document

        val result =
            KotlinCompilation()
                .apply {
                    sources = listOf(source)
                    compilerPluginRegistrars = listOf(AnalyzerRegistrar { document = it })
                    verbose = false
                    pluginOptions =
                        listOf(
                            PluginOption("scip-kotlinc", "sourceroot", path.toString()),
                            PluginOption("scip-kotlinc", "targetroot", buildPath.toString()),
                        )
                    commandLineProcessors = listOf(AnalyzerCommandLineProcessor())
                    workingDir = path.toFile()
                }
                .compile()

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        document shouldNotBe null
        return document
    }

    @Test
    fun `basic test`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample
            class Banana {
                fun foo() { }
            }""",
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Banana#"
                    range {
                        startLine = 1
                        startCharacter = 6
                        endLine = 1
                        endCharacter = 12
                    }
                    enclosingRange {
                        startLine = 1
                        endLine = 3
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Banana#foo()."
                    range {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 17
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Banana#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Banana"
                    signatureText = "public final class Banana : Any"
                },
                scipSymbol {
                    symbol = "sample/Banana#foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Banana#"
                    displayName = "foo"
                    signatureText = "public final fun foo(): Unit"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun imports(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    import kotlin.Boolean
                    import kotlin.Int as KInt
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/"
                    range {
                        startLine = 2
                        startCharacter = 7
                        endLine = 2
                        endCharacter = 13
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Boolean#"
                    range {
                        startLine = 2
                        startCharacter = 14
                        endLine = 2
                        endCharacter = 21
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/"
                    range {
                        startLine = 3
                        startCharacter = 7
                        endLine = 3
                        endCharacter = 13
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 3
                        startCharacter = 14
                        endLine = 3
                        endCharacter = 17
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)
    }

    @Test
    fun `local classes`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun foo() {
                      class LocalClass {
                        fun localClassMethod() {}
                      }
                    }
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/foo()."
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 2
                        endLine = 6
                        endCharacter = 1
                    }
                },
                // LocalClass
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 0"
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 2
                        endLine = 5
                        endCharacter = 3
                    }
                },
                // LocalClass constructor
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 1"
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 2
                        endLine = 5
                        endCharacter = 3
                    }
                },
                // localClassMethod
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 2"
                    range {
                        startLine = 4
                        startCharacter = 8
                        endLine = 4
                        endCharacter = 24
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 4
                        endLine = 4
                        endCharacter = 29
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "foo"
                    signatureText = "public final fun foo(): Unit"
                },
                scipSymbol {
                    symbol = "local 0"
                    kind = Kind.Class
                    enclosingSymbol = "sample/foo()."
                    displayName = "LocalClass"
                    signatureText = "local final class LocalClass : Any"
                },
                scipSymbol {
                    symbol = "local 1"
                    kind = Kind.Constructor
                    enclosingSymbol = "local 0"
                    displayName = "LocalClass"
                    signatureText = "public constructor(): LocalClass"
                },
                scipSymbol {
                    symbol = "local 2"
                    kind = Kind.Method
                    enclosingSymbol = "local 0"
                    displayName = "localClassMethod"
                    signatureText = "public final fun localClassMethod(): Unit"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `lambda parameters`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun use() {
                        val f = { n: Int -> n * 2 }
                    }
                """,
            )

        val occurrences =
            arrayOf(
                // val f is a local variable — gets local0 via isLocalMember
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 0"
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 31
                    }
                },
                // n is a lambda parameter — gets local1 via the owner == Symbol.NONE path
                // (the containing FirAnonymousFunction is skipped, yielding Symbol.NONE as owner)
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 1"
                    range {
                        startLine = 3
                        startCharacter = 14
                        endLine = 3
                        endCharacter = 15
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 14
                        endLine = 3
                        endCharacter = 20
                    }
                },
                // explicit type annotation on n emits a class reference
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 3
                        startCharacter = 17
                        endLine = 3
                        endCharacter = 20
                    }
                },
                // reference to n in the lambda body uses the same local symbol
                scipOccurrence {
                    role = REFERENCE
                    symbol = "local 1"
                    range {
                        startLine = 3
                        startCharacter = 24
                        endLine = 3
                        endCharacter = 25
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/use()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "use"
                    signatureText = "public final fun use(): Unit"
                },
                scipSymbol {
                    symbol = "local 0"
                    kind = Kind.Property
                    enclosingSymbol = "sample/use()."
                    displayName = "f"
                    signatureText = "local val f: (Int) -> Int"
                },
                scipSymbol {
                    symbol = "local 1"
                    kind = Kind.Parameter
                    displayName = "n"
                    signatureText = "n: Int"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `local functions`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun outer() {
                        fun inner() {}
                        fun innerWithReturnType(): Int = 42
                        inner()
                    }
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/outer()."
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 6
                        endCharacter = 1
                    }
                },
                // inner() — local named function gets a local symbol
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 0"
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 13
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 18
                    }
                },
                // innerWithReturnType() — local named function with explicit return type
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 1"
                    range {
                        startLine = 4
                        startCharacter = 8
                        endLine = 4
                        endCharacter = 27
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 4
                        endLine = 4
                        endCharacter = 39
                    }
                },
                // Int return-type reference
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 4
                        startCharacter = 31
                        endLine = 4
                        endCharacter = 34
                    }
                },
                // call site inner() references the same local symbol
                scipOccurrence {
                    role = REFERENCE
                    symbol = "local 0"
                    range {
                        startLine = 5
                        startCharacter = 4
                        endLine = 5
                        endCharacter = 9
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "local 0"
                    kind = Kind.Method
                    enclosingSymbol = "sample/outer()."
                    displayName = "inner"
                    signatureText = "local final fun inner(): Unit"
                },
                scipSymbol {
                    symbol = "local 1"
                    kind = Kind.Method
                    enclosingSymbol = "sample/outer()."
                    displayName = "innerWithReturnType"
                    signatureText = "local final fun innerWithReturnType(): Int"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `user-defined class as return type`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    class MyClass

                    fun bar(): MyClass = MyClass()
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/bar()."
                    range {
                        startLine = 4
                        startCharacter = 4
                        endLine = 4
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 0
                        endLine = 4
                        endCharacter = 30
                    }
                },
                // MyClass in the return type position generates a class reference
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/MyClass#"
                    range {
                        startLine = 4
                        startCharacter = 11
                        endLine = 4
                        endCharacter = 18
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/bar()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "bar"
                    signatureText = "public final fun bar(): MyClass"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `typealias`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    typealias MyAlias = Int
                    val x: MyAlias = 42
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/MyAlias#"
                    range {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 17
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 23
                    }
                },
                // Note: val x: MyAlias does not emit a REFERENCE for sample/MyAlias# because the
                // property checker resolves the type alias to its expansion (Int).
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/x."
                    range {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 5
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 0
                        endLine = 3
                        endCharacter = 19
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/MyAlias#"
                    kind = Kind.TypeAlias
                    enclosingSymbol = "sample/"
                    displayName = "MyAlias"
                    signatureText = "public final typealias MyAlias = Int\n"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `type parameters`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun <T> identity(x: T): T = x
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/identity()."
                    range {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 16
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 29
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/identity().[T]"
                    range {
                        startLine = 2
                        startCharacter = 5
                        endLine = 2
                        endCharacter = 6
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 5
                        endLine = 2
                        endCharacter = 6
                    }
                },
                // Note: T in type-annotation positions (x: T and return type : T) does not produce
                // REFERENCE occurrences. The checkers use toClassLikeSymbol() to detect type
                // references, which returns null for type parameters, so those usages are not
                // currently tracked.
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/identity().[T]"
                    kind = Kind.TypeParameter
                    enclosingSymbol = "sample/identity()."
                    displayName = "T"
                    signatureText = "T"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `generic class declaration`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    class Box<T>(val value: T) {
                        fun unwrap(): T = value
                    }
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Box#"
                    range {
                        startLine = 2
                        startCharacter = 6
                        endLine = 2
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 2
                        endLine = 4
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Box#[T]"
                    range {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 11
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Box#unwrap()."
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 14
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 27
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)
    }

    @Test
    fun overrides(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample

            interface Interface {
                fun foo()
            }

            class Class : Interface {
                override fun foo() {}
            }
            """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Interface#"
                    range {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 19
                    }
                    enclosingRange {
                        startLine = 2
                        endLine = 4
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Interface#foo()."
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 13
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Class#"
                    range {
                        startLine = 6
                        startCharacter = 6
                        endLine = 6
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 6
                        endLine = 8
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Interface#"
                    range {
                        startLine = 6
                        startCharacter = 14
                        endLine = 6
                        endCharacter = 23
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Class#foo()."
                    range {
                        startLine = 7
                        startCharacter = 17
                        endLine = 7
                        endCharacter = 20
                    }
                    enclosingRange {
                        startLine = 7
                        startCharacter = 4
                        endLine = 7
                        endCharacter = 25
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Interface#"
                    kind = Kind.Interface
                    enclosingSymbol = "sample/"
                    displayName = "Interface"
                    signatureText = "public abstract interface Interface : Any"
                },
                scipSymbol {
                    symbol = "sample/Interface#foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Interface#"
                    displayName = "foo"
                    signatureText = "public abstract fun foo(): Unit\n"
                },
                scipSymbol {
                    symbol = "sample/Class#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Class"
                    signatureText = "public final class Class : Interface"
                    addOverriddenSymbols("sample/Interface#")
                },
                scipSymbol {
                    symbol = "sample/Class#foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Class#"
                    displayName = "foo"
                    signatureText = "public open override fun foo(): Unit"
                    addOverriddenSymbols("sample/Interface#foo().")
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `anonymous object`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample

            interface Interface {
                fun foo()
            }

            fun main() {
                val a = object : Interface {
                    override fun foo() {}
                }
                val b = object : Interface {
                    override fun foo() {}
                }
            }
            """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Interface#"
                    range {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 19
                    }
                    enclosingRange {
                        startLine = 2
                        endLine = 4
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Interface#foo()."
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 13
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 1"
                    range {
                        startLine = 7
                        startCharacter = 12
                        endLine = 7
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 7
                        startCharacter = 12
                        endLine = 9
                        endCharacter = 5
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 2"
                    range {
                        startLine = 7
                        startCharacter = 12
                        endLine = 7
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 7
                        startCharacter = 12
                        endLine = 9
                        endCharacter = 5
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Interface#"
                    range {
                        startLine = 7
                        startCharacter = 21
                        endLine = 7
                        endCharacter = 30
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 3"
                    range {
                        startLine = 8
                        startCharacter = 21
                        endLine = 8
                        endCharacter = 24
                    }
                    enclosingRange {
                        startLine = 8
                        startCharacter = 8
                        endLine = 8
                        endCharacter = 29
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 5"
                    range {
                        startLine = 10
                        startCharacter = 12
                        endLine = 10
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 10
                        startCharacter = 12
                        endLine = 12
                        endCharacter = 5
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 6"
                    range {
                        startLine = 10
                        startCharacter = 12
                        endLine = 10
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 10
                        startCharacter = 12
                        endLine = 12
                        endCharacter = 5
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Interface#"
                    range {
                        startLine = 10
                        startCharacter = 21
                        endLine = 10
                        endCharacter = 30
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 7"
                    range {
                        startLine = 11
                        startCharacter = 21
                        endLine = 11
                        endCharacter = 24
                    }
                    enclosingRange {
                        startLine = 11
                        startCharacter = 8
                        endLine = 11
                        endCharacter = 29
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Interface#"
                    kind = Kind.Interface
                    enclosingSymbol = "sample/"
                    displayName = "Interface"
                    signatureText = "public abstract interface Interface : Any"
                },
                scipSymbol {
                    symbol = "local 1"
                    kind = Kind.Class
                    enclosingSymbol = "local 0"
                    displayName = "<anonymous>"
                    signatureText = "object : Interface"
                    addOverriddenSymbols("sample/Interface#")
                },
                scipSymbol {
                    symbol = "local 3"
                    kind = Kind.Method
                    enclosingSymbol = "local 1"
                    displayName = "foo"
                    signatureText = "public open override fun foo(): Unit"
                    addOverriddenSymbols("sample/Interface#foo().")
                },
                scipSymbol {
                    symbol = "local 5"
                    kind = Kind.Class
                    enclosingSymbol = "local 4"
                    displayName = "<anonymous>"
                    signatureText = "object : Interface"
                    addOverriddenSymbols("sample/Interface#")
                },
                scipSymbol {
                    symbol = "local 7"
                    kind = Kind.Method
                    enclosingSymbol = "local 5"
                    displayName = "foo"
                    signatureText = "public open override fun foo(): Unit"
                    addOverriddenSymbols("sample/Interface#foo().")
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `function return type`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample

            fun foo(arg: Int): Boolean = true
            """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/foo()."
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 2
                        endLine = 2
                        endCharacter = 33
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/foo().(arg)"
                    range {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 16
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 2
                        startCharacter = 13
                        endLine = 2
                        endCharacter = 16
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Boolean#"
                    range {
                        startLine = 2
                        startCharacter = 19
                        endLine = 2
                        endCharacter = 26
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)
    }

    @Test
    fun `type operators`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample

            fun foo(x: Any) {
                when (x) {
                    is Int -> true
                    else -> x as Float
                }
            }

            class Wrapper
            fun classify(x: Any) {
                if (x is Wrapper) {}
                val s = x as? String
                val w = x as Wrapper
            }
            """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 4
                        startCharacter = 11
                        endLine = 4
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Float#"
                    range {
                        startLine = 5
                        startCharacter = 21
                        endLine = 5
                        endCharacter = 26
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Wrapper#"
                    range {
                        startLine = 11
                        startCharacter = 13
                        endLine = 11
                        endCharacter = 20
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/String#"
                    range {
                        startLine = 12
                        startCharacter = 18
                        endLine = 12
                        endCharacter = 24
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Wrapper#"
                    range {
                        startLine = 13
                        startCharacter = 17
                        endLine = 13
                        endCharacter = 24
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)
    }

    @Test
    fun `exception test`(@TempDir path: Path) {
        val buildPath = File(path.resolve("build").toString()).apply { mkdir() }
        val result =
            KotlinCompilation()
                .apply {
                    sources = listOf(SourceFile.testKt(""))
                    compilerPluginRegistrars =
                        listOf(AnalyzerRegistrar { throw Exception("sample text") })
                    verbose = false
                    messageOutputStream = java.io.OutputStream.nullOutputStream()
                    pluginOptions =
                        listOf(
                            PluginOption("scip-kotlinc", "sourceroot", path.toString()),
                            PluginOption("scip-kotlinc", "targetroot", buildPath.toString()),
                        )
                    commandLineProcessors = listOf(AnalyzerCommandLineProcessor())
                    workingDir = path.toFile()
                }
                .compile()

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        result.messages shouldContain "Exception in scip-kotlin compiler plugin:"
        result.messages shouldContain "sample text"
    }

    @Test
    // shamelessly stolen code snippet from https://learnxinyminutes.com/docs/kotlin/
    fun `learn x in y test`(@TempDir path: Path) {
        val buildPath = File(path.resolve("build").toString()).apply { mkdir() }

        val source =
            SourceFile.testKt(
                """
            @file:Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER", "NAME_SHADOWING", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
            package sample

            fun main(args: Array<String>) {
                val fooVal = 10 // we cannot later reassign fooVal to something else
                var fooVar = 10
                fooVar = 20 // fooVar can be reassigned

                /*
                In most cases, Kotlin can determine what the type of a variable is,
                so we don't have to explicitly specify it every time.
                We can explicitly declare the type of a variable like so:
                */
                val foo: Int = 7

                /*
                Strings can be represented in a similar way as in Java.
                Escaping is done with a backslash.
                */
                val fooString = "My String Is Here!"
                val barString = "Printing on a new line?\nNo Problem!"
                val bazString = "Do you want to add a tab?\tNo Problem!"
                println(fooString)
                println(barString)
                println(bazString)

                /*
                Strings can contain template expressions.
                A template expression starts with a dollar sign (${'$'}).
                */
                val fooTemplateString = "$'fooString' has ${"fooString.length"} characters"
                println(fooTemplateString) // => My String Is Here! has 18 characters

                /*
                For a variable to hold null it must be explicitly specified as nullable.
                A variable can be specified as nullable by appending a ? to its type.
                We can access a nullable variable by using the ?. operator.
                We can use the ?: operator to specify an alternative value to use
                if a variable is null.
                */
                var fooNullable: String? = "abc"
                println(fooNullable?.length) // => 3
                println(fooNullable?.length ?: -1) // => 3
                fooNullable = null
                println(fooNullable?.length) // => null
                println(fooNullable?.length ?: -1) // => -1

                /*
                Functions can be declared using the "fun" keyword.
                Function arguments are specified in brackets after the function name.
                Function arguments can optionally have a default value.
                The function return type, if required, is specified after the arguments.
                */
                fun hello(name: String = "world"): String {
                    return "Hello, $'name'!"
                }
                println(hello("foo")) // => Hello, foo!
                println(hello(name = "bar")) // => Hello, bar!
                println(hello()) // => Hello, world!

                /*
                A function parameter may be marked with the "vararg" keyword
                to allow a variable number of arguments to be passed to the function.
                */
                fun varargExample(vararg names: Int) {
                    println("Argument has ${"names.size"} elements")
                }
                varargExample() // => Argument has 0 elements
                varargExample(1) // => Argument has 1 elements
                varargExample(1, 2, 3) // => Argument has 3 elements

                /*
                When a function consists of a single expression then the curly brackets can
                be omitted. The body is specified after the = symbol.
                */
                fun odd(x: Int): Boolean = x % 2 == 1
                println(odd(6)) // => false
                println(odd(7)) // => true

                // If the return type can be inferred then we don't need to specify it.
                fun even(x: Int) = x % 2 == 0
                println(even(6)) // => true
                println(even(7)) // => false

                // Functions can take functions as arguments and return functions.
                fun not(f: (Int) -> Boolean): (Int) -> Boolean {
                    return {n -> !f.invoke(n)}
                }
                // Named functions can be specified as arguments using the :: operator.
                val notOdd = not(::odd)
                val notEven = not(::even)
                // Lambda expressions can be specified as arguments.
                val notZero = not {n -> n == 0}
                /*
                If a lambda has only one parameter
                then its declaration can be omitted (along with the ->).
                The name of the single parameter will be "it".
                */
                val notPositive = not {it > 0}
                for (i in 0..4) {
                    println("${"notOdd(i)"} ${"notEven(i)"} ${"notZero(i)"} ${"notPositive(i)"}")
                }

                // The "class" keyword is used to declare classes.
                class ExampleClass(val x: Int) {
                    fun memberFunction(y: Int): Int {
                        return x + y
                    }

                    infix fun infixMemberFunction(y: Int): Int {
                        return x * y
                    }
                }
                /*
                To create a new instance we call the constructor.
                Note that Kotlin does not have a "new" keyword.
                */
                val fooExampleClass = ExampleClass(7)
                // Member functions can be called using dot notation.
                println(fooExampleClass.memberFunction(4)) // => 11
                /*
                If a function has been marked with the "infix" keyword then it can be
                called using infix notation.
                */
                println(fooExampleClass infixMemberFunction 4) // => 28

                /*
                Data classes are a concise way to create classes that just hold data.
                The "hashCode"/"equals" and "toString" methods are automatically generated.
                */
                data class DataClassExample (val x: Int, val y: Int, val z: Int)
                val fooData = DataClassExample(1, 2, 4)
                println(fooData) // => DataClassExample(x=1, y=2, z=4)

                // Data classes have a "copy" function.
                val fooCopy = fooData.copy(y = 100)
                println(fooCopy) // => DataClassExample(x=1, y=100, z=4)

                // Objects can be destructured into multiple variables.
                val (a, b, c) = fooCopy
                println("$'a' $'b' $'c'") // => 1 100 4

                // destructuring in "for" loop
                for ((a, b, c) in listOf(fooData)) {
                    println("$'a' $'b' $'c'") // => 1 2 4
                }

                val mapData = mapOf("a" to 1, "b" to 2)
                // Map.Entry is destructurable as well
                for ((key, value) in mapData) {
                    println("$'key' -> $'value'")
                }

                // The "with" function is similar to the JavaScript "with" statement.
                data class MutableDataClassExample (var x: Int, var y: Int, var z: Int)
                val fooMutableData = MutableDataClassExample(7, 4, 9)
                with (fooMutableData) {
                    x -= 2
                    y += 2
                    z--
                }
                println(fooMutableData) // => MutableDataClassExample(x=5, y=6, z=8)

                /*
                We can create a list using the "listOf" function.
                The list will be immutable - elements cannot be added or removed.
                */
                val fooList = listOf("a", "b", "c")
                println(fooList.size) // => 3
                println(fooList.first()) // => a
                println(fooList.last()) // => c
                // Elements of a list can be accessed by their index.
                println(fooList[1]) // => b

                // A mutable list can be created using the "mutableListOf" function.
                val fooMutableList = mutableListOf("a", "b", "c")
                fooMutableList.add("d")
                println(fooMutableList.last()) // => d
                println(fooMutableList.size) // => 4

                // We can create a set using the "setOf" function.
                val fooSet = setOf("a", "b", "c")
                println(fooSet.contains("a")) // => true
                println(fooSet.contains("z")) // => false

                // We can create a map using the "mapOf" function.
                val fooMap = mapOf("a" to 8, "b" to 7, "c" to 9)
                // Map values can be accessed by their key.
                println(fooMap["a"]) // => 8

                /*
                Sequences represent lazily-evaluated collections.
                We can create a sequence using the "generateSequence" function.
                */
                val fooSequence = generateSequence(1, { it + 1 })
                val x = fooSequence.take(10).toList()
                println(x) // => [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

                // An example of using a sequence to generate Fibonacci numbers:
                fun fibonacciSequence(): Sequence<Long> {
                    var a = 0L
                    var b = 1L

                    fun next(): Long {
                        val result = a + b
                        a = b
                        b = result
                        return a
                    }

                    return generateSequence(::next)
                }
                val y = fibonacciSequence().take(10).toList()
                println(y) // => [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]

                // Kotlin provides higher-order functions for working with collections.
                val z = (1..9).map {it * 3}
                              .filter {it < 20}
                              .groupBy {it % 2 == 0}
                              .mapKeys {if (it.key) "even" else "odd"}
                println(z) // => {odd=[3, 9, 15], even=[6, 12, 18]}

                // A "for" loop can be used with anything that provides an iterator.
                for (c in "hello") {
                    println(c)
                }

                // "while" loops work in the same way as other languages.
                var ctr = 0
                while (ctr < 5) {
                    println(ctr)
                    ctr++
                }
                do {
                    println(ctr)
                    ctr++
                } while (ctr < 10)

                /*
                "if" can be used as an expression that returns a value.
                For this reason the ternary ?: operator is not needed in Kotlin.
                */
                val num = 5
                val message = if (num % 2 == 0) "even" else "odd"
                println("$'num' is $'message'") // => 5 is odd

                // "when" can be used as an alternative to "if-else if" chains.
                val i = 10
                when {
                    i < 7 -> println("first block")
                    fooString.startsWith("hello") -> println("second block")
                    else -> println("else block")
                }

                // "when" can be used with an argument.
                when (i) {
                    0, 21 -> println("0 or 21")
                    in 1..20 -> println("in the range 1 to 20")
                    else -> println("none of the above")
                }

                // "when" can be used as a function that returns a value.
                var result = when (i) {
                    0, 21 -> "0 or 21"
                    in 1..20 -> "in the range 1 to 20"
                    else -> "none of the above"
                }
                println(result)

                /*
                We can check if an object is of a particular type by using the "is" operator.
                If an object passes a type check then it can be used as that type without
                explicitly casting it.
                */
                fun smartCastExample(x: Any) : Boolean {
                    if (x is Boolean) {
                        // x is automatically cast to Boolean
                        return x
                    } else if (x is Int) {
                        // x is automatically cast to Int
                        return x > 0
                    } else if (x is String) {
                        // x is automatically cast to String
                        return x.isNotEmpty()
                    } else {
                        return false
                    }
                }
                println(smartCastExample("Hello, world!")) // => true
                println(smartCastExample("")) // => false
                println(smartCastExample(5)) // => true
                println(smartCastExample(0)) // => false
                println(smartCastExample(true)) // => true

                // Smartcast also works with when block
                fun smartCastWhenExample(x: Any) = when (x) {
                    is Boolean -> x
                    is Int -> x > 0
                    is String -> x.isNotEmpty()
                    else -> false
                }

                /*
                Extensions are a way to add new functionality to a class.
                This is similar to C# extension methods.
                */
                fun String.remove(c: Char): String {
                    return this.filter {it != c}
                }
                println("Hello, world!".remove('l')) // => Heo, word!
            }

            // Enum classes are similar to Java enum types.
            enum class EnumExample {
                A, B, C // Enum constants are separated with commas.
            }
            fun printEnum() = println(EnumExample.A) // => A

            // Since each enum is an instance of the enum class, they can be initialized as:
            enum class EnumExample1(val value: Int) {
                A(value = 1),
                B(value = 2),
                C(value = 3)
            }
            fun printProperty() = println(EnumExample1.A.value) // => 1

            // Every enum has properties to obtain its name and ordinal(position) in the enum class declaration:
            fun printName() = println(EnumExample1.A.name) // => A
            fun printPosition() = println(EnumExample1.A.ordinal) // => 0

            /*
            The "object" keyword can be used to create singleton objects.
            We cannot instantiate it but we can refer to its unique instance by its name.
            This is similar to Scala singleton objects.
            */
            object ObjectExample {
                fun hello(): String {
                    return "hello"
                }

                override fun toString(): String {
                    return "Hello, it's me, ${"ObjectExample::class.simpleName"}"
                }
            }


            fun useSingletonObject() {
                println(ObjectExample.hello()) // => hello
                // In Kotlin, "Any" is the root of the class hierarchy, just like "Object" is in Java
                val someRef: Any = ObjectExample
                println(someRef) // => Hello, it's me, ObjectExample
            }


            /* The not-null assertion operator (!!) converts any value to a non-null type and
            throws an exception if the value is null.
            */
            var b: String? = "abc"
            val l = b!!.length

            data class Counter(var value: Int) {
                // overload Counter += Int
                operator fun plusAssign(increment: Int) {
                    this.value += increment
                }

                // overload Counter++ and ++Counter
                operator fun inc() = Counter(value + 1)

                // overload Counter + Counter
                operator fun plus(other: Counter) = Counter(this.value + other.value)

                // overload Counter * Counter
                operator fun times(other: Counter) = Counter(this.value * other.value)

                // overload Counter * Int
                operator fun times(value: Int) = Counter(this.value * value)

                // overload Counter in Counter
                operator fun contains(other: Counter) = other.value == this.value

                // overload Counter[Int] = Int
                operator fun set(index: Int, value: Int) {
                    this.value = index + value
                }

                // overload Counter instance invocation
                operator fun invoke() = println("The value of the counter is $'value'")

            }
            /* You can also overload operators through extension methods */
            // overload -Counter
            operator fun Counter.unaryMinus() = Counter(-this.value)

            fun operatorOverloadingDemo() {
                var counter1 = Counter(0)
                var counter2 = Counter(5)
                counter1 += 7
                println(counter1) // => Counter(value=7)
                println(counter1 + counter2) // => Counter(value=12)
                println(counter1 * counter2) // => Counter(value=35)
                println(counter2 * 2) // => Counter(value=10)
                println(counter1 in Counter(5)) // => false
                println(counter1 in Counter(7)) // => true
                counter1[26] = 10
                println(counter1) // => Counter(value=36)
                counter1() // => The value of the counter is 36
                println(-counter2) // => Counter(value=-5)
            }
        """
            )

        val result =
            KotlinCompilation()
                .apply {
                    sources = listOf(source)
                    compilerPluginRegistrars = listOf(AnalyzerRegistrar())
                    verbose = false
                    pluginOptions =
                        listOf(
                            PluginOption("scip-kotlinc", "sourceroot", path.toString()),
                            PluginOption("scip-kotlinc", "targetroot", buildPath.toString()),
                        )
                    commandLineProcessors = listOf(AnalyzerCommandLineProcessor())
                    workingDir = path.toFile()
                }
                .compile()

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    @Test
    fun `compound package name semicolon test`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                package hello.sample;
                class Apple
                """
                    .trimIndent(),
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "hello/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 13
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "hello/sample/"
                    range {
                        startLine = 0
                        startCharacter = 14
                        endLine = 0
                        endCharacter = 20
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "hello/sample/Apple#"
                    range {
                        startLine = 1
                        startCharacter = 6
                        endLine = 1
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 1
                        endLine = 1
                        endCharacter = 11
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "hello/sample/Apple#`<init>`()."
                    range {
                        startLine = 1
                        startCharacter = 6
                        endLine = 1
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 1
                        endLine = 1
                        endCharacter = 11
                    }
                },
            )

        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "hello/sample/Apple#"
                    kind = Kind.Class
                    enclosingSymbol = "hello/sample/"
                    displayName = "Apple"
                    signatureText = "public final class Apple : Any"
                }
            )

        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `simple package name semicolon test`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample;
            class Banana {
                fun foo() { }
            }""",
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/"
                    range {
                        startLine = 0
                        startCharacter = 8
                        endLine = 0
                        endCharacter = 14
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Banana#"
                    range {
                        startLine = 1
                        startCharacter = 6
                        endLine = 1
                        endCharacter = 12
                    }
                    enclosingRange {
                        startLine = 1
                        endLine = 3
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Banana#foo()."
                    range {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 17
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Banana#"
                    range {
                        startLine = 1
                        startCharacter = 6
                        endLine = 1
                        endCharacter = 12
                    }
                    enclosingRange {
                        startLine = 1
                        endLine = 3
                        endCharacter = 1
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Banana#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Banana"
                    signatureText = "public final class Banana : Any"
                },
                scipSymbol {
                    symbol = "sample/Banana#foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Banana#"
                    displayName = "foo"
                    signatureText = "public final fun foo(): Unit"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun documentation(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                package sample
                import java.io.Serializable
                abstract class DocstringSuperclass

                /** Example class docstring */
                class Docstrings: DocstringSuperclass(), Serializable

                /**
                  * Example method docstring
                  *
                  **/
                fun docstrings(msg: String): Int { return msg.length }
                """
                    .trimIndent(),
            )
        document.assertDocumentation("sample/Docstrings#", "Example class docstring")
        document.assertDocumentation("sample/docstrings().", "Example method docstring")
    }

    @Test
    fun `extension receiver type reference`(@TempDir path: Path) {
        // String is from the kotlin package; our extension in sample gets
        // symbol sample/String#foo(). — distinct from kotlin/String#foo().
        // This means extensions on cross-package types never collide with
        // the receiver class's own methods in the symbol table.
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun String.foo(): Int = 42
                    fun use(s: String) = s.foo()
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/String#foo()."
                    range {
                        startLine = 2
                        startCharacter = 11
                        endLine = 2
                        endCharacter = 14
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 26
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/String#"
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 10
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 2
                        startCharacter = 18
                        endLine = 2
                        endCharacter = 21
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/String#foo()."
                    range {
                        startLine = 3
                        startCharacter = 23
                        endLine = 3
                        endCharacter = 26
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/String#foo()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "foo"
                    signatureText = "public final fun String.foo(): Int"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `extension property receiver type reference`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    val Int.asString: String get() = this.toString()
                    fun use() = 42.asString
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Int#asString."
                    range {
                        startLine = 2
                        startCharacter = 8
                        endLine = 2
                        endCharacter = 16
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 48
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/Int#"
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 7
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "kotlin/String#"
                    range {
                        startLine = 2
                        startCharacter = 18
                        endLine = 2
                        endCharacter = 24
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Int#asString."
                    range {
                        startLine = 3
                        startCharacter = 15
                        endLine = 3
                        endCharacter = 23
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Int#asString."
                    kind = Kind.Property
                    enclosingSymbol = "sample/"
                    displayName = "asString"
                    signatureText = "public final val Int.asString: String"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `extension overload disambiguator`(@TempDir path: Path) {
        // When a class already has a member named foo() and an extension also
        // adds foo(), the extension is counted after the member in the combined
        // sibling list (class members + same-package extensions on the same
        // receiver type), so the extension gets the (+1) disambiguator and the
        // two produce distinct symbols.
        val document =
            compileScip(
                path,
                """
                    package sample

                    class MyClass {
                        fun foo() {}
                    }
                    fun MyClass.foo(x: Int) {}
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/MyClass#foo()."
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 11
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 16
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/MyClass#foo(+1)."
                    range {
                        startLine = 5
                        startCharacter = 12
                        endLine = 5
                        endCharacter = 15
                    }
                    enclosingRange {
                        startLine = 5
                        startCharacter = 0
                        endLine = 5
                        endCharacter = 26
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)
    }

    @Test
    fun `enum entry definitions`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    enum class Color { RED, GREEN, BLUE }

                    fun useEnum(): Color = Color.RED
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Color#"
                    range {
                        startLine = 2
                        startCharacter = 11
                        endLine = 2
                        endCharacter = 16
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 37
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Color#RED."
                    range {
                        startLine = 2
                        startCharacter = 19
                        endLine = 2
                        endCharacter = 22
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 19
                        endLine = 2
                        endCharacter = 23
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Color#GREEN."
                    range {
                        startLine = 2
                        startCharacter = 24
                        endLine = 2
                        endCharacter = 29
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 24
                        endLine = 2
                        endCharacter = 30
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Color#BLUE."
                    range {
                        startLine = 2
                        startCharacter = 31
                        endLine = 2
                        endCharacter = 35
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 31
                        endLine = 2
                        endCharacter = 35
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Color#"
                    range {
                        startLine = 4
                        startCharacter = 23
                        endLine = 4
                        endCharacter = 28
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Color#RED."
                    range {
                        startLine = 4
                        startCharacter = 29
                        endLine = 4
                        endCharacter = 32
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Color#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Color"
                    addOverriddenSymbols("kotlin/Enum#")
                    signatureText = "public final enum class Color : Enum<Color>"
                },
                scipSymbol {
                    symbol = "sample/Color#RED."
                    kind = Kind.EnumMember
                    enclosingSymbol = "sample/Color#"
                    displayName = "RED"
                    signatureText = "public final static enum entry RED: Color"
                },
                scipSymbol {
                    symbol = "sample/Color#GREEN."
                    kind = Kind.EnumMember
                    enclosingSymbol = "sample/Color#"
                    displayName = "GREEN"
                    signatureText = "public final static enum entry GREEN: Color"
                },
                scipSymbol {
                    symbol = "sample/Color#BLUE."
                    kind = Kind.EnumMember
                    enclosingSymbol = "sample/Color#"
                    displayName = "BLUE"
                    signatureText = "public final static enum entry BLUE: Color"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `enum entry with body`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    enum class Op {
                        PLUS {
                            override fun apply(a: Int, b: Int) = a + b
                        };

                        abstract fun apply(a: Int, b: Int): Int
                    }
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Op#PLUS."
                    range {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 8
                    }
                    // Body-having enum entry: enclosing_range covers PLUS through the
                    // trailing `};` that terminates the entry list.
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 5
                        endCharacter = 6
                    }
                },
                // An enum entry with a body is modeled as a synthetic anonymous subclass,
                // so its overridden member is a local symbol (local2), not a global
                // sample/Op#PLUS.apply(). It still gets an enclosing_range spanning the
                // function declaration.
                scipOccurrence {
                    role = DEFINITION
                    symbol = "local 2"
                    range {
                        startLine = 4
                        startCharacter = 21
                        endLine = 4
                        endCharacter = 26
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 8
                        endLine = 4
                        endCharacter = 50
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                // The entry body becomes an anonymous class enclosed by the entry...
                scipSymbol {
                    symbol = "local 0"
                    kind = Kind.Class
                    enclosingSymbol = "sample/Op#PLUS."
                    displayName = "<anonymous>"
                    addOverriddenSymbols("sample/Op#")
                    signatureText = "object : Op"
                },
                // ...and the override is a method of that anonymous class.
                scipSymbol {
                    symbol = "local 2"
                    kind = Kind.Method
                    enclosingSymbol = "local 0"
                    displayName = "apply"
                    addOverriddenSymbols("sample/Op#apply().")
                    signatureText = "public open override fun apply(a: Int, b: Int): Int"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `named object declarations`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    object MySingleton {
                        fun hello(): String = "hi"
                    }
                    fun use() = MySingleton.hello()
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/MySingleton#"
                    range {
                        startLine = 2
                        startCharacter = 7
                        endLine = 2
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 4
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/MySingleton#hello()."
                    range {
                        startLine = 3
                        startCharacter = 8
                        endLine = 3
                        endCharacter = 13
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 30
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/MySingleton#"
                    range {
                        startLine = 5
                        startCharacter = 12
                        endLine = 5
                        endCharacter = 23
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/MySingleton#hello()."
                    range {
                        startLine = 5
                        startCharacter = 24
                        endLine = 5
                        endCharacter = 29
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/MySingleton#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "MySingleton"
                    signatureText = "public final object MySingleton : Any"
                },
                scipSymbol {
                    symbol = "sample/MySingleton#hello()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/MySingleton#"
                    displayName = "hello"
                    signatureText = "public final fun hello(): String"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `companion object`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    class Foo {
                        companion object Factory {
                            fun create(): Foo = Foo()
                        }
                    }
                    fun use() = Foo.Factory.create()
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Foo#"
                    range {
                        startLine = 2
                        startCharacter = 6
                        endLine = 2
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 6
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Foo#Factory#"
                    range {
                        startLine = 3
                        startCharacter = 21
                        endLine = 3
                        endCharacter = 28
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 5
                        endCharacter = 5
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Foo#Factory#create()."
                    range {
                        startLine = 4
                        startCharacter = 12
                        endLine = 4
                        endCharacter = 18
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 8
                        endLine = 4
                        endCharacter = 33
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Foo#"
                    range {
                        startLine = 7
                        startCharacter = 12
                        endLine = 7
                        endCharacter = 15
                    }
                },
                // Foo.Factory is a FirResolvedQualifier spanning the full qualifier expression
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Foo#Factory#"
                    range {
                        startLine = 7
                        startCharacter = 12
                        endLine = 7
                        endCharacter = 23
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Foo#Factory#create()."
                    range {
                        startLine = 7
                        startCharacter = 24
                        endLine = 7
                        endCharacter = 30
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Foo#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Foo"
                    signatureText = "public final class Foo : Any"
                },
                scipSymbol {
                    symbol = "sample/Foo#Factory#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/Foo#"
                    displayName = "Factory"
                    signatureText = "public final companion object Factory : Any"
                },
                scipSymbol {
                    symbol = "sample/Foo#Factory#create()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Foo#Factory#"
                    displayName = "create"
                    signatureText = "public final fun create(): Foo"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `unnamed companion object`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    class Bar {
                        companion object {
                            fun instance(): Bar = Bar()
                        }
                    }
                    fun use() = Bar.instance()
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Bar#"
                    range {
                        startLine = 2
                        startCharacter = 6
                        endLine = 2
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 6
                        endCharacter = 1
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Bar#Companion#instance()."
                    range {
                        startLine = 4
                        startCharacter = 12
                        endLine = 4
                        endCharacter = 20
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 8
                        endLine = 4
                        endCharacter = 35
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Bar#"
                    range {
                        startLine = 7
                        startCharacter = 12
                        endLine = 7
                        endCharacter = 15
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Bar#Companion#instance()."
                    range {
                        startLine = 7
                        startCharacter = 16
                        endLine = 7
                        endCharacter = 24
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Bar#Companion#"
                    range {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 13
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 4
                        endLine = 5
                        endCharacter = 5
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Bar#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Bar"
                    signatureText = "public final class Bar : Any"
                },
                scipSymbol {
                    symbol = "sample/Bar#Companion#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/Bar#"
                    displayName = "Companion"
                    signatureText = "public final companion object Companion : Any"
                },
                scipSymbol {
                    symbol = "sample/Bar#Companion#instance()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/Bar#Companion#"
                    displayName = "instance"
                    signatureText = "public final fun instance(): Bar"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `string template references`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun greet(name: String) = "Hello, ${'$'}name!"
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/greet()."
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 9
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 41
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/greet().(name)"
                    range {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 14
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 10
                        endLine = 2
                        endCharacter = 22
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/greet().(name)"
                    range {
                        startLine = 2
                        startCharacter = 35
                        endLine = 2
                        endCharacter = 39
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/greet()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "greet"
                    signatureText = "public final fun greet(name: String): String"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `multiple supertype references`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    interface Named
                    interface Speakable
                    class Person : Named, Speakable
                    fun use(p: Person): Named = p
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/Person#"
                    range {
                        startLine = 4
                        startCharacter = 6
                        endLine = 4
                        endCharacter = 12
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 0
                        endLine = 4
                        endCharacter = 31
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Named#"
                    range {
                        startLine = 4
                        startCharacter = 15
                        endLine = 4
                        endCharacter = 20
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Speakable#"
                    range {
                        startLine = 4
                        startCharacter = 22
                        endLine = 4
                        endCharacter = 31
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Named#"
                    range {
                        startLine = 5
                        startCharacter = 20
                        endLine = 5
                        endCharacter = 25
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/Person#"
                    kind = Kind.Class
                    enclosingSymbol = "sample/"
                    displayName = "Person"
                    addOverriddenSymbols("sample/Named#")
                    addOverriddenSymbols("sample/Speakable#")
                    signatureText = "public final class Person : Named, Speakable"
                }
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    @Test
    fun `three-way overload disambiguator`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    fun add(x: Int, y: Int): Int = x + y
                    fun add(x: Double, y: Double): Double = x + y
                    fun add(x: String, y: String): String = x + y
                    fun use() {
                        add(1, 2)
                        add(1.0, 2.0)
                        add("a", "b")
                    }
                """,
            )

        val occurrences =
            arrayOf(
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/add()."
                    range {
                        startLine = 2
                        startCharacter = 4
                        endLine = 2
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 2
                        startCharacter = 0
                        endLine = 2
                        endCharacter = 36
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/add(+1)."
                    range {
                        startLine = 3
                        startCharacter = 4
                        endLine = 3
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 3
                        startCharacter = 0
                        endLine = 3
                        endCharacter = 45
                    }
                },
                scipOccurrence {
                    role = DEFINITION
                    symbol = "sample/add(+2)."
                    range {
                        startLine = 4
                        startCharacter = 4
                        endLine = 4
                        endCharacter = 7
                    }
                    enclosingRange {
                        startLine = 4
                        startCharacter = 0
                        endLine = 4
                        endCharacter = 45
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/add()."
                    range {
                        startLine = 6
                        startCharacter = 4
                        endLine = 6
                        endCharacter = 7
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/add(+1)."
                    range {
                        startLine = 7
                        startCharacter = 4
                        endLine = 7
                        endCharacter = 7
                    }
                },
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/add(+2)."
                    range {
                        startLine = 8
                        startCharacter = 4
                        endLine = 8
                        endCharacter = 7
                    }
                },
            )
        document.occurrencesList.shouldContainAll(*occurrences)

        val symbols =
            arrayOf(
                scipSymbol {
                    symbol = "sample/add()."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "add"
                    signatureText = "public final fun add(x: Int, y: Int): Int"
                },
                scipSymbol {
                    symbol = "sample/add(+1)."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "add"
                    signatureText = "public final fun add(x: Double, y: Double): Double"
                },
                scipSymbol {
                    symbol = "sample/add(+2)."
                    kind = Kind.Method
                    enclosingSymbol = "sample/"
                    displayName = "add"
                    signatureText = "public final fun add(x: String, y: String): String"
                },
            )
        document.symbolsList.shouldContainAll(*symbols)
    }

    private fun Document.assertDocumentation(symbol: String, expectedDocumentation: String) {
        val info =
            this.symbolsList.find { it.symbol == symbol }
                ?: fail("no scipSymbol for symbol $symbol")
        val obtainedDocumentation = info.documentationList.joinToString("\n").trim()
        assertEquals(expectedDocumentation, obtainedDocumentation)
    }
}
