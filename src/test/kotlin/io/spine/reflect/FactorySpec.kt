/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


@DisplayName("`Factory` should")
internal class FactorySpec {

    private val classLoader = this.javaClass.classLoader

    // The names of the classes under the `test/given/` directories in
    // `java` and `kotlin` source sets.

    private val topLevelJavaClass = "given.reflect.JavaClass"
    private val nestedJavaClass = "given.reflect.JavaClass\$NestedClass"

    private val topLevelKotlinClass = "given.reflect.KotlinClass"
    private val nestedKotlinClass = "given.reflect.KotlinClass\$NestedClass"

    private val classWithoutRequiredConstructor =
        "given.reflect.JavaClass\$WithoutRequiredConstructor"

    private val javaClassWithParameters = "given.reflect.WithParametersJava"

    private fun <T : Any> factory() = Factory<T>(classLoader)

    private fun <T: Any> assertCompletes(action: Factory<T>.() -> T) {
        factory<T>().run {
            assertDoesNotThrow {
                action()
            }
        }
    }

    private fun <T: Any> assertRejects(action: Factory<T>.() -> Unit) {
        factory<T>().run {
            assertThrows<IllegalStateException> {
                action()
            }
        }
    }

    private fun <T: Any> assertCouldNotFind(action: Factory<T>.() -> T) {
        factory<T>().run {
            assertThrows<ClassNotFoundException> {
                action()
            }
        }
    }

    @Nested
    inner class `create an instance of` {

        @Test
        fun `a top level Java class`() = assertCompletes<Any> {
            create(topLevelJavaClass)
        }

        @Test
        fun `a nested Java class`() = assertCompletes<Any> {
            create(nestedJavaClass)
        }

        @Test
        fun `a top level Kotlin class`() = assertCompletes<Any> {
            create(topLevelKotlinClass)
        }

        @Test
        fun `a nested Kotlin class`() = assertCompletes<Any> {
            create(nestedKotlinClass)
        }
    }

    @Test
    fun `require binary name of a nested Java class`() = assertCouldNotFind<Any> {
        create(nestedJavaClass.replace("\$", "."))
    }

    @Test
    fun `require binary name of a nested Kotlin class`() = assertCouldNotFind<Any> {
        create(nestedJavaClass.replace("\$", "."))
    }

    @Test
    fun `require public no-arg constructor`() = assertRejects<Any> {
        create(classWithoutRequiredConstructor)
    }

    @Nested
    inner class `create instances with parameters` {

        @Test
        fun `in Kotlin code`() = assertCompletes<WithParameters> {
            create(
                WithParameters::class.qualifiedName!!,
                "Foo", null, listOf("1", null, "3")
            )
        }

        @Test
        fun `in Java code`() = assertCompletes<Any> {
            create(
                javaClassWithParameters,
                "Foo", null, listOf("Jungle", "Guerilla", null, "Banana")
            )
        }
    }

    @Nested
    inner class `reject arguments` {

        @Test
        fun `with wrong types`() = assertRejects<Any> {
            create(javaClassWithParameters, "1", null, 3)
        }

        @Test
        fun `with wrong number`() = assertRejects<Any> {
            create(javaClassWithParameters, "1", null)
        }
    }
}

/**
 * The class with public constructor with parameters.
 */
data class WithParameters(
    val str: String,
    val any: Any?,
    val list: List<String?>
)
