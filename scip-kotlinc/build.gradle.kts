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
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
}
