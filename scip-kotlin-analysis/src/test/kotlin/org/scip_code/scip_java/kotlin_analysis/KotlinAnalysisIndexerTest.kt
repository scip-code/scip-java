package org.scip_code.scip_java.kotlin_analysis

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.scip_code.scip.Document
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
