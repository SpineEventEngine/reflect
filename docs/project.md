# Project: Spine Reflect

## Overview

Spine Reflect is a utility library for working with reflection in Kotlin and Java
code. It bridges the Java and Kotlin reflection APIs, resolves generic type
arguments at runtime, invokes methods and constructors reflectively, finds the
callers of a class on the current stack, and looks up annotations of a package or
of any of its parental packages. It is published as `io.spine:spine-reflect`.

## Architecture

Role in the org: a foundational **library** at the bottom of the dependency graph.
`base-libraries`, `logging`, `compiler`, `validation`, `core-jvm-compiler`, and
`core-jvm` depend on it, while it depends on no other Spine repository at
runtime — `testlib` is a test-only dependency.

- A single Gradle module with sources in both Java and Kotlin, all in the
  `io.spine.reflect` package.
- Java has no true package nesting, so the library derives it from package names.
  `PackageGraph` exposes it as a Guava `Graph`. Independently of `PackageGraph`,
  the annotation lookups resolve parental packages by name: `AnnotatedPackages`
  scans the packages already loaded, while `PackageAnnotationLookup` force-loads
  a parental package through its `package-info` class when needed.
- The tests of `PackageAnnotationLookup` rely on the fixtures under
  `io.spine.reflect.given.unloaded` staying unloaded until a test touches them,
  so `build.gradle.kts` keeps that hierarchy out of test discovery.
- The stack-inspection code (`CallerFinder` and the `StackGetter`
  implementations) is adapted from Google Flogger.
- Being a low-level dependency, the library keeps its public surface small and
  stable. Of its runtime dependencies, only Kotlin reflection is specific to this
  repository; JSpecify annotations, Guava, and Protobuf come with the shared
  `jvm-module` convention.

Read [`.agents/guidelines/jvm-project.md`](../.agents/guidelines/jvm-project.md) for the
build stack, coding style, tests, and versioning.
