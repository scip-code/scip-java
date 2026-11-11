import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.writeText
import kotlin.system.exitProcess
import org.scip_code.scip.Document
import org.scip_code.scip_java.kotlin_analysis.KotlinAnalysisIndexer

fun main() {
    val sourceroot = Files.createTempDirectory("kotlin-analysis-consumer").toRealPath()
    val src = Files.createDirectories(sourceroot.resolve("src"))
    src.resolve("Greeter.kt")
        .writeText("package demo\n\nclass Greeter(val name: String) { fun greet() = \"Hi \$name\" }\n")
    val stdlib = Paths.get(KotlinVersion::class.java.protectionDomain.codeSource.location.toURI())

    val streamed = mutableListOf<Document>()
    val documents: List<Document> =
        KotlinAnalysisIndexer(
                sourceroot = sourceroot,
                targetroot = sourceroot.resolve("scip-targetroot"),
                sourceRoots = listOf(src),
                classpath = listOf(stdlib),
                callback = { streamed += it },
            )
            .run()

    val symbols = documents.flatMap { doc -> doc.occurrencesList.map { it.symbol } }
    if (documents.map { it.relativePath } != listOf("src/Greeter.kt") ||
        streamed != documents ||
        "demo/Greeter#" !in symbols ||
        "kotlin/String#" !in symbols
    ) {
        System.err.println("Unexpected indexer output:\n$documents")
        exitProcess(1)
    }
    println("Indexed ${documents.size} document with ${symbols.size} occurrences")
    // The Analysis API leaves non-daemon threads behind; exit explicitly like scip-java.
    exitProcess(0)
}
