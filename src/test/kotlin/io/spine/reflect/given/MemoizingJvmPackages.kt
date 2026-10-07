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
