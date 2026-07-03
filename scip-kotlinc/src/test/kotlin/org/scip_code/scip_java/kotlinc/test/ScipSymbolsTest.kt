package org.scip_code.scip_java.kotlinc.test

import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.TestFactory
import org.scip_code.scip.SymbolInformation.Kind
import org.scip_code.scip_java.kotlinc.*
import org.scip_code.scip_java.kotlinc.test.ExpectedSymbols.ScipData
import org.scip_code.scip_java.kotlinc.test.ExpectedSymbols.SymbolCacheData

@ExperimentalCompilerApi
class ScipSymbolsTest {
    @TestFactory
    fun `method disambiguator`() =
        listOf(
                ExpectedSymbols(
                    "Basic two methods",
                    SourceFile.testKt(
                        """
                        |class Test {
                        |    fun sample() {}
                        |    fun sample(x: Int) {}
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf("Test#sample().".symbol(), "Test#sample(+1).".symbol())
                        ),
                ),
                ExpectedSymbols(
                    "Inline class constructor",
                    SourceFile.testKt(
                        """
                        |class Test(val x: Int)
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(listOf("Test#`<init>`().(x)".symbol())),
                ),
                ExpectedSymbols(
                    "Inline + secondary class constructors",
                    SourceFile.testKt(
                        """
                        |class Test(val x: Int) {
                        |    constructor(y: Long): this(y.toInt())
                        |    constructor(z: String): this(z.toInt())
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "Test#`<init>`().(x)".symbol(),
                                "Test#`<init>`(+1).(y)".symbol(),
                                "Test#`<init>`(+2).(z)".symbol(),
                            )
                        ),
                ),
                ExpectedSymbols(
                    "Disambiguator number is not affected by different named methods",
                    SourceFile.testKt(
                        """
                        |class Test {
                        |    fun sample() {}
                        |    fun test() {}
                        |    fun test(x: Int) {}
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(listOf("Test#test().".symbol(), "Test#test(+1).".symbol())),
                ),
                ExpectedSymbols(
                    "Top level overloaded functions",
                    SourceFile.testKt(
                        """
                        |fun test() {}
                        |fun test(x: Int) {}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(listOf("test().".symbol(), "test(+1).(x)".symbol())),
                ),
                ExpectedSymbols(
                    "Annotations incl annotation type alias",
                    SourceFile.testKt(
                        """
                        |import kotlin.contracts.ExperimentalContracts
                        |import kotlin.test.Test
                        |
                        |@ExperimentalContracts
                        |class Banaan {
                        |    @Test
                        |    fun test() {}
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "kotlin/contracts/ExperimentalContracts#".symbol(),
                                "kotlin/test/Test#".symbol(),
                            )
                        ),
                ),
                // https://kotlinlang.slack.com/archives/C7L3JB43G/p1624995376114900
                /*ExpectedSymbols(
                    "Method call with type parameters",
                    SourceFile.testKt("""
                        import org.junit.jupiter.api.io.TempDir
                        val burger = LinkedHashMap<String, TempDir>()
                    """),
                    symbolsCacheData = SymbolCacheData(
                        listOf("kotlin/collection/TypeAliasesKt#LinkedHashMap#`<init>`().".symbol())
                    )
                )*/
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `check package symbols`() =
        listOf(
                ExpectedSymbols(
                    "single component package name",
                    SourceFile.testKt(
                        """
                        |package main
                        |
                        |class Test
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(listOf("main/Test#".symbol()), 0),
                ),
                ExpectedSymbols(
                    "multi component package name",
                    SourceFile.testKt(
                        """
                        |package test.sample.main
                        |
                        |class Test
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(listOf("test/sample/main/Test#".symbol()), 0),
                ),
                ExpectedSymbols(
                    "no package name",
                    SourceFile.testKt(
                        """
                        |class Test
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(listOf("Test#".symbol()), 0),
                ),
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `check locals counts`() =
        listOf(
                ExpectedSymbols(
                    "simple variables",
                    SourceFile.testKt(
                        """
                        |fun test() {
                        |    val x = "hello"
                        |    println(x)
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(localsCount = 1),
                )
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `builtin symbols`() =
        listOf(
                ExpectedSymbols(
                    "types",
                    SourceFile.testKt(
                        """
                        |var x: Int = 1
                        |lateinit var y: Unit
                        |lateinit var z: Any
                        |lateinit var w: Nothing
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "kotlin/Int#".symbol(),
                                "kotlin/Unit#".symbol(),
                                "kotlin/Any#".symbol(),
                                "kotlin/Nothing#".symbol(),
                            )
                        ),
                ),
                ExpectedSymbols(
                    "functions",
                    SourceFile.testKt(
                        """
                        |val x = mapOf<Void, Void>()
                        |fun main() {
                        |    println()
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "kotlin/collections/mapOf(+2).".symbol(),
                                "kotlin/io/println(+10).".symbol(),
                            )
                        ),
                ),
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `reference expressions`() =
        listOf(
                ExpectedSymbols(
                    "dot qualified expression",
                    SourceFile.testKt(
                        """
                        |import java.lang.System
                        |
                        |fun main() {
                        |    System.err
                        |}
                        |"""
                            .trimMargin()
                    ),
                    symbolsCacheData = SymbolCacheData(listOf("java/lang/System#err.".symbol())),
                )
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `properties with getters-setters`() =
        listOf(
                ExpectedSymbols(
                    "top level properties - implicit",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "x."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange { endCharacter = 14 }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "getX()."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange { endCharacter = 14 }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "setX()."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange { endCharacter = 14 }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "top level properties - explicit getter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    get() = field + 10
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "x."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange {
                                            endLine = 1
                                            endCharacter = 22
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "setX()."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange {
                                            endLine = 1
                                            endCharacter = 22
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "getX()."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 7
                                        }
                                        enclosingRange {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 22
                                        }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "top level properties - explicit setter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    set(value) { field = value + 5 }
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "x."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange {
                                            endLine = 1
                                            endCharacter = 36
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "getX()."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange {
                                            endLine = 1
                                            endCharacter = 36
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "setX()."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 7
                                        }
                                        enclosingRange {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 36
                                        }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "top level properties - explicit getter & setter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    get() = field + 10
                        |    set(value) { field = value + 10 }
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "x."
                                        range {
                                            startLine = 0
                                            startCharacter = 4
                                            endLine = 0
                                            endCharacter = 5
                                        }
                                        enclosingRange {
                                            endLine = 2
                                            endCharacter = 37
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "getX()."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 7
                                        }
                                        enclosingRange {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 22
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "setX()."
                                        range {
                                            startLine = 2
                                            startCharacter = 4
                                            endLine = 2
                                            endCharacter = 7
                                        }
                                        enclosingRange {
                                            startLine = 2
                                            startCharacter = 4
                                            endLine = 2
                                            endCharacter = 37
                                        }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "class constructor properties",
                    SourceFile.testKt(
                        """
                        |class Test(var sample: Int, text: String): Throwable(sample.toString()) {
                        |    fun test() {
                        |        println(sample)
                        |    }
                        |}
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Test#`<init>`().(sample)"
                                        range {
                                            startLine = 0
                                            startCharacter = 15
                                            endLine = 0
                                            endCharacter = 21
                                        }
                                        enclosingRange {
                                            startCharacter = 11
                                            endCharacter = 26
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Test#sample."
                                        range {
                                            startLine = 0
                                            startCharacter = 15
                                            endLine = 0
                                            endCharacter = 21
                                        }
                                        enclosingRange {
                                            startCharacter = 11
                                            endCharacter = 26
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Test#getSample()."
                                        range {
                                            startLine = 0
                                            startCharacter = 15
                                            endLine = 0
                                            endCharacter = 21
                                        }
                                        enclosingRange {
                                            startCharacter = 11
                                            endCharacter = 26
                                        }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Test#setSample()."
                                        range {
                                            startLine = 0
                                            startCharacter = 15
                                            endLine = 0
                                            endCharacter = 21
                                        }
                                        enclosingRange {
                                            startCharacter = 11
                                            endCharacter = 26
                                        }
                                    },
                                    scipOccurrence {
                                        role = REFERENCE
                                        symbol = "Test#`<init>`().(sample)"
                                        range {
                                            startLine = 0
                                            startCharacter = 53
                                            endLine = 0
                                            endCharacter = 59
                                        }
                                    },
                                    scipOccurrence {
                                        role = REFERENCE
                                        symbol = "Test#sample."
                                        range {
                                            startLine = 2
                                            startCharacter = 16
                                            endLine = 2
                                            endCharacter = 22
                                        }
                                    },
                                    scipOccurrence {
                                        role = REFERENCE
                                        symbol = "Test#getSample()."
                                        range {
                                            startLine = 2
                                            startCharacter = 16
                                            endLine = 2
                                            endCharacter = 22
                                        }
                                    },
                                )
                        ),
                ),
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `class constructors`() =
        listOf(
                ExpectedSymbols(
                    "implicit primary constructor",
                    SourceFile.testKt(
                        """
                        |class Banana
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 6
                                            endLine = 0
                                            endCharacter = 12
                                        }
                                        enclosingRange { endCharacter = 12 }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#`<init>`()."
                                        range {
                                            startLine = 0
                                            startCharacter = 6
                                            endLine = 0
                                            endCharacter = 12
                                        }
                                        enclosingRange { endCharacter = 12 }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "explicit primary constructor without keyword",
                    SourceFile.testKt(
                        """
                        |class Banana(size: Int)
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 6
                                            endLine = 0
                                            endCharacter = 12
                                        }
                                        enclosingRange { endCharacter = 23 }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#`<init>`()."
                                        range {
                                            startLine = 0
                                            startCharacter = 6
                                            endLine = 0
                                            endCharacter = 12
                                        }
                                        enclosingRange {
                                            startCharacter = 12
                                            endCharacter = 23
                                        }
                                    },
                                )
                        ),
                ),
                ExpectedSymbols(
                    "explicit primary constructor with keyword",
                    SourceFile.testKt(
                        """
                        |class Banana constructor(size: Int)
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 6
                                            endLine = 0
                                            endCharacter = 12
                                        }
                                        enclosingRange { endCharacter = 35 }
                                    },
                                    scipOccurrence {
                                        role = DEFINITION
                                        symbol = "Banana#`<init>`()."
                                        range {
                                            startLine = 0
                                            startCharacter = 13
                                            endLine = 0
                                            endCharacter = 24
                                        }
                                        enclosingRange {
                                            startCharacter = 13
                                            endCharacter = 35
                                        }
                                    },
                                )
                        ),
                ),
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `Single Abstract Method interface`() =
        listOf(
                ExpectedSymbols(
                    "basic java.lang.Runnable",
                    SourceFile.testKt(
                        """
                        |val x = Runnable { }.run()
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedOccurrences =
                                listOf(
                                    scipOccurrence {
                                        role = REFERENCE
                                        symbol = "java/lang/Runnable#"
                                        range {
                                            startLine = 0
                                            startCharacter = 8
                                            endLine = 0
                                            endCharacter = 16
                                        }
                                    },
                                    scipOccurrence {
                                        role = REFERENCE
                                        symbol = "java/lang/Runnable#run()."
                                        range {
                                            startLine = 0
                                            startCharacter = 21
                                            endLine = 0
                                            endCharacter = 24
                                        }
                                    },
                                )
                        ),
                )
            )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun kdoc() =
        listOf(
                ExpectedSymbols(
                    "empty kdoc line",
                    SourceFile.testKt(
                        """
                        |/**
                        |
                        |hello world
                        |* test content
                        |*/
                        |val x = ""
                        |"""
                            .trimMargin()
                    ),
                    scip =
                        ScipData(
                            expectedSymbols =
                                listOf(
                                    scipSymbol {
                                        symbol = "x."
                                        kind = Kind.Property
                                        displayName = "x"
                                        signatureText = "public final val x: String"
                                        documentation("hello world\n test content")
                                    },
                                    scipSymbol {
                                        symbol = "getX()."
                                        kind = Kind.Method
                                        displayName = "x"
                                        signatureText = "public get(): String"
                                        documentation("hello world\n test content")
                                    },
                                )
                        ),
                )
            )
            .mapCheckExpectedSymbols()
}
