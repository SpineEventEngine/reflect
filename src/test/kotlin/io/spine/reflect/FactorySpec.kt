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

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


@DisplayName("`Factory` should")
internal class FactorySpec {

    private val classLoader = this.javaClass.classLoader

    private val topLevelJavaClass = "given.reflect.JavaClass"
    private val nestedJavaClass = "given.reflect.JavaClass\$NestedClass"

    private val topLevelKotlinClass = "given.reflect.KotlinClass"
    private val nestedKotlinClass = "given.reflect.KotlinClass\$NestedClass"

    @Test
    fun `create an instance of a top level Java class`() {
        Factory<Any>(classLoader).run {
            assertDoesNotThrow {
                create(topLevelJavaClass)
            }
        }
    }

    @Test
    fun `create an in an instance of a nested Java class`() {
        Factory<Any>(classLoader).run {
            assertDoesNotThrow {
                create(nestedJavaClass)
            }
        }
    }

    @Test
    fun `require binary name of a nested Java class`() {
        Factory<Any>(classLoader).run {
            assertThrows<Exception> {
                create(nestedJavaClass.replace("\$", "."))
            }
        }
    }

    @Test
    fun `create an instance of a top level Kotlin class`() {
        Factory<Any>(classLoader).run {
            assertDoesNotThrow {
                create(topLevelKotlinClass)
            }
        }
    }

    @Test
    fun `create an in an instance of a nested Kotlin class`() {
        Factory<Any>(classLoader).run {
            assertDoesNotThrow {
                create(nestedKotlinClass)
            }
        }
    }

    @Test
    fun `require binary name of a nested Kotlin class`() {
        Factory<Any>(classLoader).run {
            assertThrows<Exception> {
                create(nestedJavaClass.replace("\$", "."))
            }
        }
    }
}
