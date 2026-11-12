plugins {
    kotlin("jvm") version "2.4.21"
    application
}

val scipVersion = providers.gradleProperty("scipVersion").orElse("0.0.0-SNAPSHOT").get()

dependencies {
    // Deliberately the only dependency: everything the indexer's public API needs
    // must come from scip-kotlin-analysis's published metadata.
    implementation("org.scip-code:scip-kotlin-analysis:$scipVersion")
}

application {
    mainClass.set("MainKt")
}
