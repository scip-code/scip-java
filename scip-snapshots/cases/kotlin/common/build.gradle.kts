import org.scip_code.scip_java.buildlogic.cleanDirectoryBeforeRunning
import org.scip_code.scip_java.buildlogic.publishDirectoryArtifact
import org.scip_code.scip_java.buildlogic.scipKotlincPluginArgs
import org.scip_code.scip_java.buildlogic.shadowJarArtifact
import org.scip_code.scip_java.buildlogic.useScipJavac
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("scip.java-library")
    id("scip.kotlin-jvm")
}

val javacShadowJar = shadowJarArtifact(":scip-javac", "javacShadowJar")
val kotlincShadowJar = shadowJarArtifact(":scip-kotlinc", "kotlincShadowJar")

dependencies {
    implementation(libs.kotlin.stdlib)
}

// Runtime classpath of the Analysis-API-based indexer, used by the
// scipIndexKotlinAnalysis comparison task below.
val scipKotlinAnalysis: Configuration by configurations.creating {
    isCanBeConsumed = false
}

dependencies {
    scipKotlinAnalysis(project(":scip-kotlin-analysis"))
}

val scipTargetroot = layout.buildDirectory.dir("scip-targetroot")
val sourceroot = rootProject.rootDir.absolutePath
val targetroot = scipTargetroot.get().asFile.absolutePath

tasks.named<KotlinCompile>("compileKotlin") {
    inputs.files(kotlincShadowJar)
    outputs.dir(scipTargetroot)
    compilerOptions.freeCompilerArgs.addAll(scipKotlincPluginArgs(kotlincShadowJar.elements, sourceroot, targetroot))
    cleanDirectoryBeforeRunning(scipTargetroot)
}

tasks.named<JavaCompile>("compileJava") {
    useScipJavac(rootDir, javacShadowJar, scipTargetroot)
    options.annotationProcessorPath = javacShadowJar
    options.compilerArgs.add("-Xplugin:scip -sourceroot:$sourceroot -targetroot:$targetroot")
}

publishDirectoryArtifact("scipTargetrootElements", scipTargetroot, tasks.named("classes"))

// Indexes the same Kotlin sources with the standalone Analysis API indexer into a
// separate targetroot, so its SCIP output can be compared against scip-kotlinc's.
val scipAnalysisTargetroot = layout.buildDirectory.dir("scip-targetroot-analysis")

tasks.register<JavaExec>("scipIndexKotlinAnalysis") {
    classpath = scipKotlinAnalysis
    mainClass.set("org.scip_code.scip_java.kotlin_analysis.MainKt")
    val kotlinSources = layout.projectDirectory.dir("src/main/kotlin")
    val compileClasspath = sourceSets.main.map { it.compileClasspath }
    inputs.dir(kotlinSources)
    inputs.files(compileClasspath)
    outputs.dir(scipAnalysisTargetroot)
    cleanDirectoryBeforeRunning(scipAnalysisTargetroot)
    // Locals only: the argument provider must not capture the build script
    // (configuration cache).
    val sourcerootArg = sourceroot
    val targetrootArg = scipAnalysisTargetroot
    val kotlinSourcesArg = kotlinSources.asFile.absolutePath
    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                "--sourceroot",
                sourcerootArg,
                "--targetroot",
                targetrootArg.get().asFile.absolutePath,
                "--classpath",
                compileClasspath.get().asPath,
                kotlinSourcesArg,
            )
        }
    )
}
