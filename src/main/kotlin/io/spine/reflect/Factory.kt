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

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import org.checkerframework.checker.signature.qual.FqBinaryName

/**
 * A utility class for creating instances of classes by their fully qualified binary class names.
 *
 * The class is loaded via the [classLoader] passed to the factory on creation.
 *
 * @param T the type of the objects created by this factory.
 * @param classLoader
 *         the class loader to be used for loading the class.
 */
public open class Factory<T : Any>(private val classLoader: ClassLoader) {

    /**
     * Creates an instance of [T].
     *
     * It is necessary that the class defined by the [className] parameter is
     * of type [T] or is a subtype of [T]. Otherwise, a casting error occurs.
     *
     * The class must provide a `public` no-arg constructor.
     * Otherwise, an exception will be thrown.
     *
     * @param className
     *         the binary name of the class to instantiate.
     */
    public fun create(className: @FqBinaryName String): T {
        val cls = loadClass<T>(className)
        return cls.create()
    }

    /**
     * Creates an instance of [T].
     *
     * It is necessary that the class defined by the [className] parameter is
     * of type [T] or is a subtype of [T]. Otherwise, a casting error occurs.
     *
     * The class must provide a `public` constructor with parameters matching given [args].
     * Otherwise, an exception will occur.
     *
     * @param className
     *         the binary name of the class to instantiate.
     * @param args
     *         the arguments passed to the constructor.
     */
    public fun create(className: @FqBinaryName String, vararg args: Any?): T =
        create(className, args.toList())

    /**
     * Creates an instance of [T].
     *
     * It is necessary that the class defined by the [className] parameter is
     * of type [T] or is a subtype of [T]. Otherwise, a casting error occurs.
     *
     * The class must provide a `public` constructor with parameters matching given [args].
     * Otherwise, an exception will occur.
     *
     * @param className
     *         the binary name of the class to instantiate.
     * @param args
     *         the arguments passed to the constructor.
     */
    public fun create(className: @FqBinaryName String, args: Iterable<Any?>): T {
        val cls = loadClass<T>(className)
        return cls.create(args.toList())
    }

    private fun <T: Any> loadClass(className: @FqBinaryName String): KClass<out T> {
        val cls = classLoader.loadClass(className).kotlin
        @Suppress("UNCHECKED_CAST")
        return cls as KClass<T>
    }
}

private fun <T : Any> KClass<T>.create(): T {
    val ctor = constructors.find { it.visibility.isPublic && it.parameters.isEmpty() }
    check(ctor != null) {
        "The class `$qualifiedName` should have a public zero-parameter constructor."
    }
    return ctor.call()
}

/**
 * Creates an instance of the class by locating the constructor matching the given arguments.
 */
private fun <T : Any> KClass<T>.create(args: List<Any?>): T {
    val ctor = constructors
        .filter { it.visibility.isPublic }
        .find { it.matches(this, args) }

    check(ctor != null) {
        val argTypes = args.map { it?.let { it::class } }
        val params = argTypes.map { it?.qualifiedName }.joinToString(", ")

        // If one of the arguments is `null`, we cannot reference its type by name.
        // Let's supply some hint on what `null` means in the list of parameter types
        // in the error message below.
        val suffix =
            if (args.any { it == null }) ", where `null` means a nullable parameter type."
            else "."

        "The class `$qualifiedName` should have a `public` constructor" +
                " with the parameters: `$params`$suffix"
    }
    val map = ctor.parameters.zip(args).toMap()
    return ctor.callBy(map)
}

/**
 * Tells if this function declared in the [declaringClass] matches the given arguments.
 */
private fun KFunction<*>.matches(declaringClass: KClass<*>, args: List<Any?>): Boolean {
    if (parameters.size != args.size) {
        // The number of parameters does not match that of arguments.
        return false
    }
    return parameters.zip(args).all { (p, a) ->
        if (a != null) {
            // For non-null argument, the class must recognize the value.
            // We assume that the `classifier` is not `null` because we do not expect
            // intersection types used with the `Factory` class.
            val cls = p.type.classifier!! as KClass<*>
            cls.isInstance(a)
        } else {
            if (declaringClass.isTrulyKotlin) {
                // If the argument value is `null`, the parameter type must be nullable.
                p.type.isMarkedNullable
            } else {
                // The class originates in Java code, which does not have `null` safety.
                // The type of the parameter returned by the Kotlin API is not nullable.
                // There's no sensible way for obtaining the annotation (if any) set in
                // the corresponding parameter in the Java code.
                // So, we simply assume that `null` passed as an argument matches any
                // parameter type in Java. If a `null` value cannot be passed for this parameter,
                // the Java code should throw anyway, and the instance would not be created.
                true
            }
        }
    }
}

/**
 * Checks if this [KVisibility] is [public][KVisibility.PUBLIC].
 */
private val KVisibility?.isPublic: Boolean
    get() = this == KVisibility.PUBLIC

/**
 * Tells if this class originates from the Kotlin code, returning `false` for classes
 * backed by Java code.
 */
private val KClass<*>.isTrulyKotlin: Boolean
    get() = java.getAnnotation(Metadata::class.java) != null
