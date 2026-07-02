plugins {
    id("scip.java-library")
    id("scip.kotlin-jvm")
}

description = "Standalone Kotlin SCIP indexer built on the Kotlin Analysis API"

// The Analysis API `*-for-ide` artifacts have unusable POM metadata (they drag in
// IDE-only dependencies), so every artifact is pinned non-transitively — the same
// approach used by Dokka's analysis-kotlin-symbols and KSP2. `kotlin-compiler`
// (the non-embeddable jar) provides the bundled IntelliJ core classes at runtime.
dependencies {
    implementation(project(":scip-shared"))
    implementation(libs.scip.kotlin.bindings)

    implementation(libs.kotlin.analysis.compiler) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.api) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.standalone) { isTransitive = false }

    runtimeOnly(libs.kotlin.analysis.api.impl.base) { isTransitive = false }
    runtimeOnly(libs.kotlin.analysis.api.k2) { isTransitive = false }
    // Provides org.jetbrains.kotlin.analysis.decompiler.* which is no longer
    // bundled in kotlin-compiler as of the 2.4 line.
    runtimeOnly(libs.kotlin.analysis.compiler.common) { isTransitive = false }
    runtimeOnly(libs.kotlin.analysis.low.level.api.fir) { isTransitive = false }
    runtimeOnly(libs.kotlin.analysis.api.platform.`interface`) { isTransitive = false }
    runtimeOnly(libs.kotlin.analysis.symbol.light.classes) { isTransitive = false }
    runtimeOnly(libs.caffeine)
    runtimeOnly(libs.kotlinx.coroutines.core.jvm)
    runtimeOnly(libs.kotlinx.collections.immutable.jvm)
    runtimeOnly(libs.kotlinx.serialization.json.jvm)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
}
