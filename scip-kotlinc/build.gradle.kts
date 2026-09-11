import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("scip.java-library")
    id("scip.kotlin-jvm")
    id("scip.shadow-producer")
    id("scip.maven-publish")
}

description = "A kotlinc plugin to emit SCIP information"

dependencies {
    implementation(project(":scip-shared"))
    implementation(libs.scip.kotlin.bindings)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kctfork.core)
}

tasks.named<Test>("test") {
    maxHeapSize = "2g"
    dependsOn("testKotlin240")
}

// The plugin runs inside the indexed project's compiler, which can predate our build compiler.
val kotlin240TestRuntime = configurations.create("kotlin240TestRuntime") {
    extendsFrom(configurations.testRuntimeClasspath.get())
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
}

tasks.register<Test>("testKotlin240") {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().output + sourceSets.main.get().output + kotlin240TestRuntime
    maxHeapSize = "2g"
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
}
