/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@file:Suppress("RemoveRedundantQualifierName") // Cannot use imports in some places.

import io.spine.dependency.lib.Kotlin
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Spine
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator
import io.spine.gradle.standardToSpineSdk

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `jvm-module`
    idea
    `gradle-doctor`
    `project-report`
}
LicenseReporter.generateReportIn(project)
CheckStyleConfig.applyTo(project)

apply(from = "$rootDir/version.gradle.kts")
group = "io.spine"
version = rootProject.extra["versionToPublish"]!!
apply<IncrementGuard>()

repositories.standardToSpineSdk()

spinePublishing {
    destinations = with(PublishingRepos) {
        setOf(
            cloudArtifactRegistry,
            gitHub("reflect")
        )
    }
    dokkaJar {
        java = true
    }
}

dependencies {
    api(Kotlin.reflect)
    testImplementation(Spine.testlib)
}

configurations.all {
    resolutionStrategy {
        force(Logging.lib, Logging.libJvm)
    }
}

tasks {
    /**
     * Prevents tasks with the type `Test` from loading any members from
     * the “unloaded” package hierarchy in advance.
     *
     * The test suite `io.spine.reflect.PackageAnnotationLookupSpec` needs
     * these members to be unloaded from the beginning.
     * This behavior matches the production runtime, in which
     * classes (and packages) are loaded as needed.
     *
     * JUnit loads test classes in advance to support its features.
     * For example, test-includes and excludes functionality.
     */
    withType<Test>().configureEach {
        filter.excludeTestsMatching("io.spine.reflect.given.unloaded*")
    }
}

// Apply Javadoc configuration here (and not right after the `plugins` block)
// because the `javadoc` task is added when the `kotlin` block `withJava` is applied.
JavadocConfig.applyTo(project)
LicenseReporter.mergeAllReports(project)
PomGenerator.applyTo(project)
