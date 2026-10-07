/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

@file:Suppress("RemoveRedundantQualifierName") // Cannot use imports in some places.

import io.spine.dependency.build.JSpecify
import io.spine.dependency.kotlinx.Coroutines
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.local.Logging
import io.spine.dependency.local.TestLib
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.repo.standardToSpineSdk
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    id("org.jetbrains.dokka")
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
}

dependencies {
    api(Kotlin.reflect)
    api(JSpecify.annotations)
    testImplementation(TestLib.lib)
}

configurations.all {
    resolutionStrategy {
        force(
            Logging.lib,
            Logging.libJvm,
            // Requested at an older version by `TestLib.lib`.
            Coroutines.bom,
        )
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
     *
     * The class files are excluded from the candidates for test discovery.
     * A test filter (`filter.excludeTestsMatching`) is not enough: it still lets
     * the discovery load an excluded class, only to skip running it.
     */
    withType<Test>().configureEach {
        exclude("io/spine/reflect/given/unloaded/**")
    }
}

// Apply Javadoc configuration here (and not right after the `plugins` block)
// because the `javadoc` task is added when the `kotlin` block `withJava` is applied.
JavadocConfig.applyTo(project)
LicenseReporter.mergeAllReports(project)
PomGenerator.applyTo(project)

dependencies {
    productionModules.forEach {
        dokka(it)
    }
}
