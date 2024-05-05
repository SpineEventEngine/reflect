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

package io.spine.reflect

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * An abstract base for testing concrete implementations of [StackGetter].
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/test/java/com/google/common/flogger/util/StackGetterTestUtil.java">
 *     Original Java code of Google Flogger</a>
 */
internal abstract class AbstractStackGetterSpec(
    private val stackGetter: StackGetter
) {

    @Test
    fun `find the stack trace element of the immediate caller of the specified class`() {
        // There are 2 internal methods (not including the log method itself)
        // in our fake library.
        val library = LoggerCode(skipCount = 2, stackGetter)
        val code = UserCode(library)
        code.invokeUserCode()
        library.caller shouldNotBe null
        library.caller!!.className shouldBe UserCode::class.java.name
        library.caller!!.methodName shouldBe "loggingMethod"
    }

    @Test
    fun `return 'null' due to wrong skip count`() {
        // If the minimum offset exceeds the number of internal methods, the find fails.
        val library = LoggerCode(skipCount = 3, stackGetter)
        val code = UserCode(library)
        code.invokeUserCode()
        library.caller shouldBe null
    }
}

/**
 * Fake class that emulates some code calling a log method.
 */
internal class UserCode(private val logger: LoggerCode) {

    fun invokeUserCode() {
        loggingMethod()
    }

    private fun loggingMethod() {
        logger.logMethod()
    }
}

/**
 * A fake class that emulates the logging library, which eventually
 * calls the given [StackGetter], if any, or [CallerFinder].
 */
internal class LoggerCode(
    private val skipCount: Int,
    private val stackGetter: StackGetter? = null
) {

    var caller: StackTraceElement? = null

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
