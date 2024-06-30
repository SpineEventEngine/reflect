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

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import org.checkerframework.checker.signature.qual.FqBinaryName

/**
 * A utility class for creating instances of classes by their fully qualified names.
 *
 * The class must provide a `public` no-arg constructor. Otherwise, an exception will be thrown.
 *
 * The class is loaded via a `ClassLoader` and an instance is created.
 *
 * @param T the type of the objects created by this factory.
 * @param classLoader
 *         the class loader to be used for loading the class.
 */
public open class Factory<T : Any>(private val classLoader: ClassLoader) {

    /**
     * Creates an instance of `T`.
     *
     * It is necessary that the class defined by the [className] parameter is of type `T` or
     * is a subtype of `T`. Otherwise, a casting error occurs.
     *
     * @param className
     *         the binary name of the class to instantiate.
     */
    public fun create(className: @FqBinaryName String): T {
        val cls = classLoader.loadClass(className).kotlin

        @Suppress("UNCHECKED_CAST")
        val tClass = cls as KClass<T>
        return tClass.create()
    }
}

private fun <T : Any> KClass<T>.create(): T {
    val ctor = constructors.find { it.visibility.isPublic && it.parameters.isEmpty() }
    check(ctor != null) {
        "The class `${qualifiedName}` should have a public zero-parameter constructor."
    }
    return ctor.call()
}

/**
 * Checks if this [KVisibility] is [public][KVisibility.PUBLIC].
 */
private val KVisibility?.isPublic: Boolean
    get() = this == KVisibility.PUBLIC
