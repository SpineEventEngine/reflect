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

import io.spine.reflect.StackGetter
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A test library class with a companion object that uses [StackGetter] functionality.
 */
@Suppress("UtilityClassWithPublicConstructor")
internal class ClassWithCompanion {

    fun findInstanceCaller(stackGetter: StackGetter): StackTraceElement? {
        return stackGetter.callerOf(ClassWithCompanion::class.java, 0)
    }

    @OptIn(ExperimentalAtomicApi::class)
    companion object {

        val caller = AtomicReference<StackTraceElement?>(null)
        
        fun findCaller(stackGetter: StackGetter) {
            caller.store(stackGetter.callerOf(ClassWithCompanion::class.java, 0))
        }
    }
}

/**
 * A user code class calls companion object and instance functions.
 */
internal class CallingTheClassWithCompanion(
    private val viaCompanion: ClassWithCompanion.Companion,
    private val stackGetter: StackGetter
) {
    fun invokeCompanionFun() {
        viaCompanion.findCaller(stackGetter)
    }

    fun invokeInstanceFun(): StackTraceElement? {
        return ClassWithCompanion().findInstanceCaller(stackGetter)
    }
}
