import org.scip_code.scip_java.buildlogic.JavacInternals
import org.scip_code.scip_java.buildlogic.registerGeneratedFile
import org.scip_code.scip_java.buildlogic.shadowJarArtifact

plugins {
    id("scip.java-base")
    id("scip.kotlin-jvm")
    application
    id("scip.maven-publish")
}

description = "Java and Kotlin indexer for SCIP"

val javacShadowJar = shadowJarArtifact(":scip-javac", "javacShadowJar")
val gradlePluginShadowJar = shadowJarArtifact(":scip-gradle-plugin", "gradlePluginShadowJar")

dependencies {
    implementation(project(":scip-aggregator"))
    implementation(project(":scip-kotlin-analysis"))
    implementation(libs.clikt.jvm)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json.jvm)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
}

tasks.named<Test>("test") {
    jvmArgs(JavacInternals.jvmOptions(rootDir))
    systemProperty("scip.jdk.version", "11")
}

application {
    mainClass.set("org.scip_code.scip_java.ScipJava")
}

val generateEmbeddedResources = tasks.register<Sync>("generateEmbeddedResources") {
    from(javacShadowJar) {
        rename { "scip-plugin.jar" }
    }
    from(gradlePluginShadowJar) {
        rename { "gradle-plugin.jar" }
    }
    into(layout.buildDirectory.dir("generated/resources/embedded"))
}

tasks.named<ProcessResources>("processResources") {
    from(JavacInternals.propertiesFile(rootDir))
    from(generateEmbeddedResources)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

val generateDistributionVersion =
    registerGeneratedFile("generateDistributionVersion", "distribution/VERSION", "version:=${project.version}")

// Emit the javac --add-exports flags as a ready-to-use, space-separated string
// so the Docker wrapper script can read them with a plain `cat`, keeping
// gradle/javac-internals.properties the single source of truth.
val generateJavacJvmOptions =
    registerGeneratedFile(
        "generateJavacJvmOptions",
        "distribution/javac-jvm-options",
        JavacInternals.jvmOptions(rootDir).joinToString(" "),
    )

distributions {
    named("main") {
        contents {
            from(generateDistributionVersion)
            from(generateJavacJvmOptions)
        }
    }
}
