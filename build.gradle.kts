plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

tasks.register("saveSnapshots") {
    group = "verification"
    description = "Regenerates all SCIP snapshot goldens."
    dependsOn(":scip-snapshots:saveSnapshots")
}

spotless {
    java {
        target("scip-*/src/**/*.java")
        targetExclude("scip-java/src/test/resources/fixtures/**")
        googleJavaFormat("1.36.1")
    }

    kotlin {
        target("build-logic/src/**/*.kt", "scip-*/src/**/*.kt")
        targetExclude("scip-java/src/test/resources/fixtures/**")
        ktfmt("0.61").kotlinlangStyle()
    }
}
