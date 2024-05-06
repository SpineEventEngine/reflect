/*
 * Copyright 2024, TeamDev. All rights reserved.
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

import io.spine.reflect.CallerFinder.stackForCallerOf

/**
 * A stub mimicking the behavior of a real `LogContext` for the purpose of tests.
 *
 * Real [`LogContext`](https://github.com/SpineEventEngine/logging/blob/master/flogger/middleware/src/main/java/io/spine/logging/flogger/LogContext.java)
 * class is the consumer of
 * [CallerFinder.stackForCallerOf][io.spine.reflect.CallerFinder.stackForCallerOf] method.
 *
 * We recreate the behavior described in comments inside the real `LogContext.postProcess()` method
 * to match the expected behavior.
 */
internal abstract class LogContext {

    var message: String? = null
    var callerStack: Array<StackTraceElement>? = null

    fun log(message: String) {
        if (shouldLog()) {
            this.message = message
        }
    }

    private fun shouldLog(): Boolean {
        val shouldLog = postProcess()
        return shouldLog
    }

    protected open fun postProcess(): Boolean {
        // Remember the call stack as if we get it in real `LogContext`.
        // We pass `maxDepth` at maximum as the most demanding case.
        callerStack = stackForCallerOf(LogContext::class.java, maxDepth = -1, skip = 1)
        return true
    }
}

internal open class ChildContext: LogContext() {

    override fun postProcess(): Boolean {
        val fromSuper = super.postProcess()
        // Simulate overriding.
        return fromSuper
    }
}

internal class OtherChildContext: ChildContext() {
    override fun postProcess(): Boolean {
        val deeperWeGo = super.postProcess()
        // Override yet more.
        return deeperWeGo
    }
}
