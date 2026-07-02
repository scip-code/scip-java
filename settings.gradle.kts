pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // Kotlin Analysis API (`*-for-ide`) artifacts used by scip-kotlin-analysis.
        maven("https://redirector.kotlinlang.org/maven/kotlin-ide-plugin-dependencies") {
            content { includeGroupAndSubgroups("org.jetbrains.kotlin") }
        }
    }
}

rootProject.name = "scip-java"

include(
    "scip-shared",
    "scip-javac",
    "scip-kotlinc",
    "scip-kotlin-analysis",
    "scip-aggregator",
    "scip-maven-plugin",
    "scip-gradle-plugin",
    "scip-java",
    "scip-snapshots",
    "scip-snapshots-java-common",
    "scip-snapshots-java-25",
    "scip-snapshots-kotlin-common",
)

project(":scip-snapshots-java-common").projectDir = file("scip-snapshots/cases/java/common")
project(":scip-snapshots-java-25").projectDir = file("scip-snapshots/cases/java-25")
project(":scip-snapshots-kotlin-common").projectDir = file("scip-snapshots/cases/kotlin/common")
