plugins {
    id("scip.java-library")
    id("scip.maven-publish")
}

description = "Shared SCIP utilities used by scip-java compiler plugins"

dependencies {
    api(libs.scip.java.bindings)
    constraints {
        api(libs.guava) {
            because("CVE-2026-102554")
        }
    }
}
