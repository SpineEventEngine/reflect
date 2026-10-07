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

import io.spine.reflect.CallerFinder
import io.spine.reflect.StackGetter

/**
 * A fake class that emulates the logging library, which eventually
 * calls the given [StackGetter], if any, or [CallerFinder].
 */
internal class LoggerCode(
    private val skipCount: Int,
    private val stackGetter: StackGetter? = null
) {

    var caller: StackTraceElement? = null

    val logContext: LogContext = OtherChildContext()

    fun logMethod() {
        internalMethodOne()
    }

    private fun internalMethodOne() {
        internalMethodTwo()
    }

    private fun internalMethodTwo() {
        caller = if (stackGetter != null) {
            stackGetter.callerOf(LoggerCode::class.java, skipCount)
        } else {
            CallerFinder.findCallerOf(LoggerCode::class.java, skipCount)
        }
    }
}
