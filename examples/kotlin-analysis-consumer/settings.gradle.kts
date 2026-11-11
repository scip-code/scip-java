// A standalone consumer of the published scip-kotlin-analysis artifact. It resolves
// the module only through its published metadata, as library users do, so it catches
// missing `api` dependencies and references to artifacts outside Maven Central.
//
// Run: gradle publishToMavenLocal && gradle -p examples/kotlin-analysis-consumer run
rootProject.name = "kotlin-analysis-consumer"

dependencyResolutionManagement {
    repositories {
        mavenLocal { content { includeGroup("org.scip-code") } }
        mavenCentral()
    }
}
