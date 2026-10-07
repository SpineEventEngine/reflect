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

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.spine.reflect.J2Kt.findKotlinMethod
import io.spine.reflect.given.MethodHolder
import io.spine.reflect.given.ObjMethodHolder
import kotlin.reflect.KParameter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`J2Kt` should")
internal class J2KtSpec {

    @Test
    fun `find a static method in a class`() {
        val name = "staticMethod"
        val method = MethodHolder::class.java.getDeclaredMethod(name, Int::class.java)
        findKotlinMethod(method) shouldBePresent {
            val ktMethod = it
            ktMethod.name shouldBe name
            val params: List<KParameter?> = ktMethod.parameters
            params shouldHaveSize 2
            params[0]!!.kind shouldBe KParameter.Kind.INSTANCE
            params[1]!!.kind shouldBe KParameter.Kind.VALUE
        }
    }

    @Test
    fun `find a instance method a single argument in a class`() {
        val name = "instanceMethod"
        val method = MethodHolder::class.java.getDeclaredMethod(name, String::class.java)
        findKotlinMethod(method) shouldBePresent {
            val ktMethod = it
            ktMethod.name shouldBe name
            val params = ktMethod.parameters
            params shouldHaveSize 2
            params[0].kind shouldBe KParameter.Kind.INSTANCE
            params[1].kind shouldBe KParameter.Kind.VALUE
        }
    }

    @Test
    fun `find a instance method with no arguments in a class`() {
        val name = "noParamMethod"
        val method = MethodHolder::class.java.getDeclaredMethod(name)
        findKotlinMethod(method) shouldBePresent {
            val ktMethod = it
            ktMethod.name shouldBe name
            val params = ktMethod.parameters
            params shouldHaveSize 1
            params[0].kind shouldBe KParameter.Kind.INSTANCE
        }
    }

    @Test
    fun `find a static method in an object`() {
        val name = "staticObjMethod"
        val method = ObjMethodHolder::class.java.getDeclaredMethod(name)
        findKotlinMethod(method) shouldBePresent {
            val ktMethod = it
            ktMethod.name shouldBe name
            val params: List<KParameter> = ktMethod.parameters
            params shouldHaveSize 1
            params[0].kind shouldBe KParameter.Kind.INSTANCE
        }
    }

    @Test
    fun `find a instance method in an object`() {
        val name = "instanceObjMethod"
        val method = ObjMethodHolder::class.java.getDeclaredMethod(name)
        findKotlinMethod(method) shouldBePresent {
            val ktMethod = it
            val params  = ktMethod.parameters
            params shouldHaveSize 1
            params[0].kind
        }
    }
}
