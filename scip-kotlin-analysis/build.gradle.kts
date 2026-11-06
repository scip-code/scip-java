plugins {
    id("scip.java-library")
    id("scip.kotlin-jvm")
    id("scip.maven-publish")
}

description = "Standalone Kotlin SCIP indexer built on the Kotlin Analysis API"

// The Analysis API `*-for-ide` artifacts and the IntelliJ coroutines fork are only
// published to JetBrains' repository, not Maven Central. They are merged into this
// module's jar, so the published POM only references Maven Central artifacts.
val bundled: Configuration by configurations.creating {
    isCanBeConsumed = false
}
configurations.compileOnly { extendsFrom(bundled) }
configurations.testImplementation { extendsFrom(bundled) }

tasks.named<Jar>("jar") {
    from({ bundled.map { zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF")
    }
}

// The Analysis API `*-for-ide` artifacts have unusable POM metadata (they drag in
// IDE-only dependencies), so every artifact is pinned non-transitively — the same
// approach used by Dokka's analysis-kotlin-symbols and KSP2. `kotlin-compiler`
// (the non-embeddable jar) provides the bundled IntelliJ core classes at runtime.
dependencies {
    api(libs.scip.kotlin.bindings)
    implementation(project(":scip-shared"))
    implementation(libs.kotlin.stdlib)

    implementation(libs.kotlin.analysis.compiler) { isTransitive = false }
    bundled(libs.kotlin.analysis.api.api) { isTransitive = false }
    bundled(libs.kotlin.analysis.api.standalone) { isTransitive = false }
    bundled(libs.kotlin.analysis.api.impl.base) { isTransitive = false }
    bundled(libs.kotlin.analysis.api.k2) { isTransitive = false }
    bundled(libs.kotlin.analysis.low.level.api.fir) { isTransitive = false }
    bundled(libs.kotlin.analysis.api.platform.`interface`) { isTransitive = false }
    bundled(libs.kotlin.analysis.symbol.light.classes) { isTransitive = false }
    bundled(libs.kotlinx.coroutines.core.jvm) { isTransitive = false }

    runtimeOnly(libs.caffeine)
    runtimeOnly(libs.kotlinx.collections.immutable.jvm)
    runtimeOnly(libs.kotlinx.serialization.json.jvm)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
}
