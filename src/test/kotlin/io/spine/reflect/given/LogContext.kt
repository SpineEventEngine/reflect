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
