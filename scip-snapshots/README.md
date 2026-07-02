# SCIP snapshots

This directory contains end-to-end snapshot fixtures for `scip-javac`,
`scip-kotlin-analysis`, and future mixed Java/Kotlin cases.

## Layout

```text
cases/
  java/common/      Java fixtures compiled with `--release 17`.
  kotlin/common/    Kotlin fixtures, including Java interop consumers.
  mixed/            Reserved for first-class mixed-language integration cases.

expected/
  java/common/      Golden snapshots for `cases/java/common`.
  kotlin/common/    Golden snapshots for `cases/kotlin/common`.
```

`common` cases should stay stable across the supported host JDK matrix. Add
version-specific cases when a fixture needs newer language features, for example
`cases/java/release-17`, `cases/java/release-21`, or `cases/kotlin/kotlin-2.2`.

Run `gradle :scip-snapshots:test` to compare goldens and
`gradle :scip-snapshots:saveSnapshots` to regenerate them.
