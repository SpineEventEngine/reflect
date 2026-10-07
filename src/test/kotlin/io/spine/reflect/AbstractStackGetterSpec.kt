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
import io.spine.reflect.given.ClassWithCompanion
import io.spine.reflect.given.CallingTheClassWithCompanion
import io.spine.reflect.given.LoggerCode
import io.spine.reflect.given.UserCode
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import org.junit.jupiter.api.Test

/**
 * An abstract base for testing concrete implementations of [StackGetter].
 *
 * @property stackGetter The [StackGetter] implementation to test.
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
        library.run {
            caller shouldNotBe null
            caller!!.className shouldBe UserCode::class.java.name
            caller!!.methodName shouldBe "loggingMethod"
        }
    }

    @Test
    fun `return 'null' due to wrong skip count`() {
        // If the minimum offset exceeds the number of internal methods, the find fails.
        val library = LoggerCode(skipCount = 3, stackGetter)
        val code = UserCode(library)
        code.invokeUserCode()
        library.caller shouldBe null
    }

    /**
     * Tests that [StackGetter.callerOf] can find the caller of a companion object method.
     */
    @Test
    @OptIn(ExperimentalAtomicApi::class)
    fun `find caller of companion object`() {
        val companionLibrary = ClassWithCompanion.Companion
        val userCode = CallingTheClassWithCompanion(companionLibrary, stackGetter)
        
        userCode.invokeCompanionFun()

        companionLibrary.run {
            caller.load() shouldNotBe null
            caller.load()!!.run {
                className shouldBe CallingTheClassWithCompanion::class.java.name
                methodName shouldBe "invokeCompanionFun"
            }
        }

        // Check the caller of an instance method also works assuming that the
        // `companion object` is declared in the class.
        val instanceCaller = userCode.invokeInstanceFun()
        instanceCaller shouldNotBe null
        instanceCaller!!.run {
            className shouldBe CallingTheClassWithCompanion::class.java.name
            methodName shouldBe "invokeInstanceFun"
        }
    }
}
