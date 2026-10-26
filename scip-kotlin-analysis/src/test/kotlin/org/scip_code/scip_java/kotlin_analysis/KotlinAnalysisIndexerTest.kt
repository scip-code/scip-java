package org.scip_code.scip_java.kotlin_analysis

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.scip_code.scip.Document
import org.scip_code.scip.SymbolInformation.Kind
import org.scip_code.scip.SymbolRole

class KotlinAnalysisIndexerTest {

    @Test
    fun `indexes definitions, references and locals across files`() {
        val sourceroot = Files.createTempDirectory("scip-kotlin-analysis").toRealPath()
        val src = Files.createDirectories(sourceroot.resolve("src"))
        Files.writeString(
            src.resolve("Greeter.kt"),
            """
            |package snapshots
            |
            |class Greeter(val name: String) {
            |    fun greet(): String {
            |        val message = "Hello, " + name
            |        return message
            |    }
            |
            |    fun greet(punctuation: String): String = greet() + punctuation
            |}
            """
                .trimMargin(),
        )
        Files.writeString(
            src.resolve("Main.kt"),
            """
            |package snapshots
            |
            |fun useGreeter(): String {
            |    val greeter = Greeter("world")
            |    return greeter.greet("!")
            |}
            """
                .trimMargin(),
        )
        val targetroot = sourceroot.resolve("scip-targetroot")

        val documents =
            KotlinAnalysisIndexer(
                    sourceroot = sourceroot,
                    targetroot = targetroot,
                    sourceRoots = listOf(src),
                    classpath = listOf(kotlinStdlibJar()),
                )
                .run()
                .sortedBy { it.relativePath }

        assertEquals(listOf("src/Greeter.kt", "src/Main.kt"), documents.map { it.relativePath })
        val greeter = documents[0]
        val main = documents[1]

        val greeterDefinitions = definitions(greeter)
        assertContains(greeterDefinitions, "snapshots/Greeter#")
        assertContains(greeterDefinitions, "snapshots/Greeter#`<init>`().(name)")
        assertContains(greeterDefinitions, "snapshots/Greeter#greet().")
        assertContains(greeterDefinitions, "snapshots/Greeter#greet(+1).")
        assertContains(greeterDefinitions, "snapshots/Greeter#greet(+1).(punctuation)")
        // `val message` inside the function body is the first local of the file.
        assertContains(greeterDefinitions, "local 0")
        // The overload delegating to `greet()` references it.
        assertContains(references(greeter), "snapshots/Greeter#greet().")
        // The stdlib type reference resolves to a classpath symbol.
        assertContains(references(greeter), "kotlin/String#")
        // A `val` constructor parameter's property belongs to the class, and its
        // synthetic getter to the property.
        val enclosingSymbols = greeter.symbolsList.associate { it.symbol to it.enclosingSymbol }
        assertEquals(
            "snapshots/Greeter#`<init>`().",
            enclosingSymbols["snapshots/Greeter#`<init>`().(name)"],
        )
        assertEquals("snapshots/Greeter#", enclosingSymbols["snapshots/Greeter#name."])
        assertEquals("snapshots/Greeter#name.", enclosingSymbols["snapshots/Greeter#name.get()."])

        val mainDefinitions = definitions(main)
        assertContains(mainDefinitions, "snapshots/useGreeter().")
        assertContains(mainDefinitions, "local 0")
        val mainReferences = references(main)
        // Cross-file references: constructor call, method call on the overload,
        // and the local variable usage.
        assertContains(mainReferences, "snapshots/Greeter#`<init>`().")
        assertContains(mainReferences, "snapshots/Greeter#greet(+1).")
        assertContains(mainReferences, "local 0")

        assertTrue(
            Files.exists(targetroot.resolve("META-INF/scip/src/Greeter.kt.scip")),
            "expected SCIP shard for Greeter.kt",
        )
        assertTrue(
            Files.exists(targetroot.resolve("META-INF/scip/src/Main.kt.scip")),
            "expected SCIP shard for Main.kt",
        )
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
            )
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
                    signatureText = "public final val RED: Color"
                },
                scipSymbol {
                    symbol = "sample/Color#GREEN."
                    kind = Kind.EnumMember
                    enclosingSymbol = "sample/Color#"
                    displayName = "GREEN"
                    signatureText = "public final val GREEN: Color"
                },
                scipSymbol {
                    symbol = "sample/Color#BLUE."
                    kind = Kind.EnumMember
                    enclosingSymbol = "sample/Color#"
                    displayName = "BLUE"
                    signatureText = "public final val BLUE: Color"
                },
            )
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
                scipOccurrence {
                    role = REFERENCE
                    symbol = "sample/Foo#Factory#"
                    range {
                        startLine = 7
                        startCharacter = 16
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
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
        assertContainsAll(document.occurrencesList, *occurrences)

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
        assertContainsAll(document.symbolsList, *symbols)
    }

    private fun compileScip(path: Path, code: String): Document {
        val sourceroot = path.toRealPath()
        val source = Files.createDirectories(sourceroot.resolve("src")).resolve("Test.kt")
        Files.writeString(source, code.trimIndent())
        return KotlinAnalysisIndexer(
                sourceroot = sourceroot,
                targetroot = sourceroot.resolve("scip-targetroot"),
                sourceRoots = listOf(source),
                classpath = listOf(kotlinStdlibJar()),
            )
            .run()
            .single()
    }

    private fun <T> assertContainsAll(actual: List<T>, vararg expected: T) {
        val missing = expected.filterNot { it in actual }
        assertTrue(
            missing.isEmpty(),
            "missing:\n${missing.joinToString("\n")}\nactual:\n${actual.joinToString("\n")}",
        )
    }

    private fun definitions(document: Document): List<String> =
        document.occurrencesList
            .filter { it.symbolRoles and SymbolRole.Definition.number != 0 }
            .map { it.symbol }

    private fun references(document: Document): List<String> =
        document.occurrencesList
            .filter { it.symbolRoles and SymbolRole.Definition.number == 0 }
            .map { it.symbol }

    private fun kotlinStdlibJar(): Path =
        Paths.get(Unit::class.java.protectionDomain.codeSource.location.toURI())
}
