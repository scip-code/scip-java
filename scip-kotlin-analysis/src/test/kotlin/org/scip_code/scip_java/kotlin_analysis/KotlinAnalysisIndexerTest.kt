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

    // Compiled by the test build, so references use classpath symbols rather than source PSI.
    open class Base<T>(open var state: T) {
        open fun find(value: T): T = value

        fun find(index: Int): T = state
    }

    open class Middle : Base<String>("")

    class Leaf : Middle()

    class Overriding : Middle() {
        override var state: String = "override"

        override fun find(value: String): String = value
    }

    @Test
    fun `inherited binary members reference their declarations`(@TempDir path: Path) {
        val sourceroot = path.toRealPath()
        val library = Paths.get(Leaf::class.java.protectionDomain.codeSource.location.toURI())
        val code =
            """
            package sample

            import org.scip_code.scip_java.kotlin_analysis.KotlinAnalysisIndexerTest.Leaf
            import org.scip_code.scip_java.kotlin_analysis.KotlinAnalysisIndexerTest.Overriding

            fun use(leaf: Leaf, overriding: Overriding) {
                leaf.find("value")
                leaf.find(1)
                leaf.state = "updated"
                overriding.find("value")
                overriding.find(1)
                overriding.state = "updated"
            }
            """
                .trimIndent()
        val source = sourceroot.resolve("Use.kt")
        Files.writeString(source, code)
        val document =
            KotlinAnalysisIndexer(
                    sourceroot = sourceroot,
                    targetroot = sourceroot.resolve("scip-targetroot"),
                    sourceRoots = listOf(source),
                    classpath = listOf(kotlinStdlibJar(), library),
                )
                .run()
                .single()
        val prefix = "org/scip_code/scip_java/kotlin_analysis/KotlinAnalysisIndexerTest#"
        val expectedReferences =
            mapOf(
                "leaf.find(\"value\")" to setOf("${prefix}Base#find()."),
                "leaf.find(1)" to setOf("${prefix}Base#find(+1)."),
                "leaf.state" to
                    setOf(
                        "${prefix}Base#state.",
                        "${prefix}Base#state.get().",
                        "${prefix}Base#state.set().",
                    ),
                "overriding.find(\"value\")" to setOf("${prefix}Overriding#find()."),
                "overriding.find(1)" to setOf("${prefix}Base#find(+1)."),
                "overriding.state" to
                    setOf(
                        "${prefix}Overriding#state.",
                        "${prefix}Overriding#state.get().",
                        "${prefix}Overriding#state.set().",
                    ),
            )
        for ((expression, expected) in expectedReferences) {
            val line = code.lines().indexOfFirst { expression in it }
            val start = code.lines()[line].indexOf(expression) + expression.indexOf('.') + 1
            val actual =
                document.occurrencesList
                    .filter {
                        it.singleLineRange.line == line &&
                            it.singleLineRange.startCharacter == start
                    }
                    .map { it.symbol }
                    .toSet()
            assertEquals(expected, actual, expression)
        }
    }

    @Test
    fun `classes and properties are not overloads of same-named functions`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
                    package sample

                    class Offset(val seconds: Int) {
                        val shift: Int = 0
                        fun shift(by: Int): Int = by
                    }

                    fun Offset(): Offset = Offset(0)
                """,
            )

        // References to compiled classes number only functions, as does javac.
        assertContainsAll(definitions(document), "sample/Offset().", "sample/Offset#shift().")
    }

    @Test
    fun `data class object methods are defined only when generated`(@TempDir path: Path) {
        val document =
            compileScip(
                path,
                """
            package sample

            data class Generated(val value: Int) {
                fun equals(value: Int): Boolean = this.value == value
                fun hashCode(seed: Int): Int = seed
                fun toString(prefix: String): String = prefix
            }
            data class Explicit(val value: Int) {
                override fun equals(other: Any?): Boolean = false
                override fun hashCode(): Int = value
                override fun toString(): String = "explicit"
            }
            open class FinalBase {
                final override fun equals(other: Any?): Boolean = false
                final override fun hashCode(): Int = 1
                final override fun toString(): String = "inherited"
            }
            data class Inherited(val value: Int) : FinalBase()
            fun use(generated: Generated, explicit: Explicit, inherited: Inherited) {
                generated.equals(null)
                generated.hashCode()
                generated.toString()
                explicit.equals(null)
                explicit.hashCode()
                explicit.toString()
                inherited.equals(null)
                inherited.hashCode()
                inherited.toString()
            }
        """,
            )
        val defined = definitions(document)
        for (name in listOf("equals", "hashCode", "toString")) {
            // Same-named overloads must not suppress or collide with the generated method.
            for (symbol in
                listOf(
                    "sample/Generated#$name(+1).",
                    "sample/Explicit#$name().",
                    "sample/FinalBase#$name().",
                )) {
                assertEquals(1, defined.count { it == symbol }, symbol)
                assertContains(references(document), symbol)
            }
            assertContains(defined, "sample/Generated#$name().")
            assertTrue(defined.none { it.startsWith("sample/Inherited#$name(") })
        }
        assertContains(defined, "sample/Generated#equals(+1).(other)")
    }

    @Test
    fun `synthetic members of local classes have distinct local symbols`(@TempDir path: Path) {
        val code =
            """
            package sample

            fun use() {
                data class First(var version: Int, val label: String)
                data class Second(val version: Int)
                val first = First(1, "one")
                val second = Second(2)
                first.version = 3
                println(second.version)
                first.copy()
                second.copy()
                first.component1()
                first.component2()
                second.component1()
                first.equals(null)
                first.hashCode()
                first.toString()
                second.equals(null)
                second.hashCode()
                second.toString()
                val list = object : java.util.ArrayList<String>() {}
                list.clear()
            }
            """
                .trimIndent()
        val document = compileScip(path, code)
        val locals = document.symbolsList.filter { it.symbol.startsWith("local ") }
        val allSymbols =
            document.occurrencesList.map { it.symbol } +
                document.symbolsList.flatMap {
                    listOf(it.symbol, it.enclosingSymbol) + it.relationshipsList.map { it.symbol }
                }
        assertTrue(
            allSymbols.filter { it.startsWith("local ") }.all { it.matches(Regex("local [0-9]+")) },
            "malformed locals: ${allSymbols.filter { it.startsWith("local ") }.distinct()}",
        )
        assertEquals(locals.size, locals.map { it.symbol }.distinct().size)
        val localDefinitions = definitions(document).filter { it.startsWith("local ") }.toSet()
        assertTrue(
            references(document).filter { it.startsWith("local ") }.all { it in localDefinitions }
        )

        for ((className, receiver) in listOf("First" to "first", "Second" to "second")) {
            val owner = locals.single { it.kind == Kind.Class && it.displayName == className }
            val members = locals.filter { it.enclosingSymbol == owner.symbol }
            val property =
                members.single { it.kind == Kind.Property && it.displayName == "version" }
            val accessors = locals.filter { it.enclosingSymbol == property.symbol }
            assertEquals(if (className == "First") 2 else 1, accessors.size)
            if (className == "First") {
                val setter = accessors.single { it.signatureDocumentation.text.contains("set(") }
                assertEquals(
                    1,
                    locals.count {
                        it.enclosingSymbol == setter.symbol && it.kind == Kind.Parameter
                    },
                )
            }
            val copy = members.single { it.kind == Kind.Method && it.displayName == "copy" }
            assertEquals(
                if (className == "First") setOf("version", "label") else setOf("version"),
                locals.filter { it.enclosingSymbol == copy.symbol }.map { it.displayName }.toSet(),
            )
            val expectedReferences =
                mapOf(
                    "$receiver.version" to (setOf(property.symbol) + accessors.map { it.symbol }),
                    "$receiver.copy()" to setOf(copy.symbol),
                    "$receiver.equals(null)" to
                        setOf(members.single { it.displayName == "equals" }.symbol),
                ) +
                    members
                        .filter {
                            it.displayName.startsWith("component") ||
                                it.displayName in setOf("hashCode", "toString")
                        }
                        .associate { "$receiver.${it.displayName}()" to setOf(it.symbol) }
            assertEquals(if (className == "First") 7 else 6, expectedReferences.size)
            for ((expression, expected) in expectedReferences) {
                val line = code.lines().indexOfFirst { expression in it }
                val start = code.lines()[line].indexOf(expression) + receiver.length + 1
                val actual =
                    document.occurrencesList
                        .filter {
                            it.singleLineRange.line == line &&
                                it.singleLineRange.startCharacter == start
                        }
                        .map { it.symbol }
                        .toSet()
                assertEquals(expected, actual, expression)
            }
        }
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
