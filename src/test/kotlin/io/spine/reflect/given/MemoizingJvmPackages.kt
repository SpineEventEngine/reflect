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

package io.spine.reflect.given

import io.spine.reflect.JvmPackages
import io.spine.reflect.PackageName

/**
 * [JvmPackages] that remembers number of calls to its methods.
 */
internal class MemoizingJvmPackages : JvmPackages() {

    private val mutableLoadings = mutableMapOf<PackageName, Int>()

    /**
     * Returns packages, for which [tryLoading] method
     * has been called one or more times.
     */
    val askedForceLoadings: Map<PackageName, Int> = mutableLoadings

    /**
     * Returns how many times [alreadyLoaded] packages has been called.
     */
    var traversedLoadedTimes = 0
        private set

    override fun alreadyLoaded(): Iterable<Package> {
        traversedLoadedTimes++
        return super.alreadyLoaded()
    }

    override fun tryLoading(name: PackageName): Package? {
        mutableLoadings[name] = (mutableLoadings[name] ?: 0) + 1
        return super.tryLoading(name)
    }

    /**
     * Tells whether the given package is loaded.
     *
     * This class does not count calls to this method.
     */
    fun isLoaded(name: PackageName) =
        super.alreadyLoaded().any { it.name == name }
}
