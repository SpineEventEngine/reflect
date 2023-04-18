/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Spine
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.coverage.JacocoConfig
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.standardToSpineSdk


buildscript {
    standardSpineSdkRepositories()
    io.spine.internal.gradle.doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `kotlin-jvm-module`
    idea
    jacoco
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
            cloudRepo,
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

// Apply Javadoc configuration here (and not right after the `plugins` block)
// because the `javadoc` task is added when the `kotlin` block `withJava` is applied.
JavadocConfig.applyTo(project)
LicenseReporter.mergeAllReports(project)
PomGenerator.applyTo(project)
