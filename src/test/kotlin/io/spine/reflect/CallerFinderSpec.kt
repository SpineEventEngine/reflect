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

package io.spine.reflect

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.reflect.given.AnybodyHome
import io.spine.reflect.given.Elvis
import io.spine.reflect.given.LoggerCode
import io.spine.reflect.given.UserCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Tests for [CallerFinder].
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/test/java/com/google/common/flogger/util/CallerFinderTest.java">
 *     Original Java code of Google Flogger</a>
 */
@DisplayName("`CallerFinder` should")
internal class CallerFinderSpec {

    /**
     * A sanity check if we ever discover a platform where the class name
     * in the stack trace does not match [Class.getName] – this is never quite
     * guaranteed by the JavaDoc in the JDK but is relied upon during log site analysis.
     */
    @Test
    fun `use the class name that matches one in the stack trace`() {
        // Simple case for a top-level named class.
        Throwable().stackTrace[0].className shouldBe CallerFinderSpec::class.java.name

        // Anonymous inner class.
        val obj = object {
            override fun toString(): String {
                return Throwable().stackTrace[0].className
            }
        }

        "$obj" shouldBe obj::class.java.name
    }

    @Test
    fun `find the stack trace element of the immediate caller of the specified class`() {
        // There are 2 internal methods (not including the log method itself)
        // in our fake library.
        val library = LoggerCode(skipCount = 2)
        val code = UserCode(library)
        code.invokeUserCode()
        library.run {
            caller shouldNotBe null
            caller!!.className shouldBe UserCode::class.java.name
            caller!!.methodName shouldBe "loggingMethod"
        }
    }

    @Test
    fun `return 'null' due to wrong skip count`() {
        // If the minimum offset exceeds the number of internal methods, the find fails.
        val library = LoggerCode(skipCount = 3)
        val code = UserCode(library)
        code.invokeUserCode()
        library.caller shouldBe null
    }

    /**
     * This is a test of obtaining the caller from inside a class that is
     * interested in knowing the caller.
     */
    @Test
    fun `obtain the caller of a class`() {
        Elvis.sign() shouldBe this::class.java
        AnybodyHome.call() shouldBe AnybodyHome::class.java
    }

    /**
     * Please see [io.spine.reflect.given.LogContext] stub for details.
     */
    @Test
    fun `obtain trimmed call stack for a class`() {
        // We don't care about the skip count here since we call stub `LogContext` directly.
        val library = LoggerCode(skipCount = 0)
        val code = UserCode(library)
        code.someMethod()

        val stack = library.logContext.callerStack
        stack shouldNotBe null
        stack!!
        // This is the element we want to be the caller.
        stack[0].className shouldBe UserCode::class.java.name
        // This means that logging internals are skipped in the stack trace.
        stack[1].className shouldBe CallerFinderSpec::class.java.name
    }
}
