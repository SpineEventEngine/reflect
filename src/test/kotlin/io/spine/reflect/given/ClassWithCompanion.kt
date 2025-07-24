/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.reflect.given

import io.spine.reflect.StackGetter

/**
 * A test library class with a companion object that uses [StackGetter] functionality.
 */
@Suppress("UtilityClassWithPublicConstructor")
internal class ClassWithCompanion {

    fun findInstanceCaller(stackGetter: StackGetter): StackTraceElement? {
        return stackGetter.callerOf(ClassWithCompanion::class.java, 0)
    }

    companion object {
        var caller: StackTraceElement? = null
            private set
        
        fun findCaller(stackGetter: StackGetter) {
            caller = stackGetter.callerOf(ClassWithCompanion::class.java, 0)
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
