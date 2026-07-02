import org.scip_code.scip_java.buildlogic.cleanDirectoryBeforeRunning
import org.scip_code.scip_java.buildlogic.publishDirectoryArtifact
import org.scip_code.scip_java.buildlogic.shadowJarArtifact
import org.scip_code.scip_java.buildlogic.useScipJavac

plugins {
    id("scip.java-library")
    id("scip.kotlin-jvm")
}

val javacShadowJar = shadowJarArtifact(":scip-javac", "javacShadowJar")

dependencies {
    implementation(libs.kotlin.stdlib)
}

// Runtime classpath of the Analysis-API-based Kotlin indexer.
val scipKotlinAnalysis: Configuration by configurations.creating {
    isCanBeConsumed = false
}

dependencies {
    scipKotlinAnalysis(project(":scip-kotlin-analysis"))
}

val scipTargetroot = layout.buildDirectory.dir("scip-targetroot")
val sourceroot = rootProject.rootDir.absolutePath
val targetroot = scipTargetroot.get().asFile.absolutePath

// Kotlin sources are indexed by the standalone scip-kotlin-analysis indexer;
// compileKotlin only produces the classes that compileJava needs.
val scipIndexKotlin = tasks.register<JavaExec>("scipIndexKotlin") {
    classpath = scipKotlinAnalysis
    mainClass.set("org.scip_code.scip_java.kotlin_analysis.MainKt")
    val kotlinSources = layout.projectDirectory.dir("src/main/kotlin")
    val compileClasspath = sourceSets.main.map { it.compileClasspath }
    inputs.dir(kotlinSources)
    inputs.files(compileClasspath)
    outputs.dir(scipTargetroot)
    cleanDirectoryBeforeRunning(scipTargetroot)
    // Locals only: the argument provider must not capture the build script
    // (configuration cache).
    val sourcerootArg = sourceroot
    val targetrootArg = scipTargetroot
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

tasks.named<JavaCompile>("compileJava") {
    useScipJavac(rootDir, javacShadowJar, scipTargetroot)
    options.annotationProcessorPath = javacShadowJar
    options.compilerArgs.add("-Xplugin:scip -sourceroot:$sourceroot -targetroot:$targetroot")
    // The Kotlin indexer cleans the targetroot before writing its shards; javac
    // must add its shards afterwards.
    mustRunAfter(scipIndexKotlin)
}

tasks.named("classes") {
    dependsOn(scipIndexKotlin)
}

publishDirectoryArtifact("scipTargetrootElements", scipTargetroot, tasks.named("classes"))
