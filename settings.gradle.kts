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
        // Kotlin Analysis API (`*-for-ide`) artifacts and the IntelliJ coroutines
        // fork used by scip-kotlin-analysis.
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") {
            content {
                includeGroupAndSubgroups("org.jetbrains.kotlin")
                includeGroup("com.intellij.platform")
            }
        }
    }
}

rootProject.name = "scip-java"

include(
    "scip-shared",
    "scip-javac",
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
