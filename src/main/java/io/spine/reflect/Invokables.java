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

package io.spine.reflect;

import com.google.common.reflect.Invokable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * Utilities which streamline the usage of Java {@linkplain java.lang.reflect.Method methods} and
 * an instantiation of objects via reflectively-obtained {@linkplain Constructor constructors}.
 */
public final class Invokables {

    private static final MethodHandles.Lookup publicLookup = publicLookup();

    /** Prevents instantiation of this utility class. */
    private Invokables() {
    }

    /**
     * Converts the given {@link Method} into a {@link MethodHandle}.
     *
     * <p>The accessibility parameter of the input method, i.e. {@code method.isAccessible()}, is
     * preserved by this method. However, the attributes may be changes in a non-synchronized
     * manner, i.e., the {@code asHandle(..)} is not designed to operate concurrently.
     */
    public static MethodHandle asHandle(Method method) {
        checkNotNull(method);
        var result = invokePreservingAccessibility(
                method,
                Invokable::from,
                publicLookup::unreflect,
                () -> format(
                        "Unable to obtain method handle for `%s`." +
                                " The method's accessibility was probably changed concurrently.",
                        method));
        return checkNotNull(result);
    }

    /**
     * Attempts to create an instance of the specified type using a constructor without parameters.
     *
     * <p>If no such constructor exists, an {@code IllegalArgumentException} is thrown.
     *
     * <p>The access level does not matter: the constructor is made accessible during the method
     * execution. It is always restored after object instantiation or an error.
     *
     * @param type
     *         class to instantiate
     * @return the object created using a parameterless constructor
     * @throws IllegalStateException
     *         if the class is abstract, or an exception is thrown in the
     *         parameterless constructor
     * @throws IllegalArgumentException
     *         if the specified class does not have a parameterless constructor.
     *         Note that nested classes fall under this case.
     */
    public static <C> C callParameterlessCtor(Class<C> type) {
        checkNotNull(type);
        var ctor = ensureParameterlessCtor(type);
        var result = invokePreservingAccessibility(
                ctor,
                Invokable::from,
                Constructor::newInstance,
                () -> format(
                        "Could not instantiate the type `%s` using " +
                                "a parameterless constructor.",
                        type.getSimpleName()));
        return result;
    }

    /**
     * Invokes the given no-arg method on the target, ignoring the accessibility restrictions.
     *
     * <p>The target must be of the type that declares the given method, otherwise an
     * {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     *         if the target is not of the type that declares the given method, or
     *         if an exception is thrown during the method invocation
     * @return the result of method invocation
     */
    @CanIgnoreReturnValue
    public static @Nullable Object setAccessibleAndInvoke(Method method, Object target) {
        checkNotNull(method);
        checkNotNull(target);

        var result = invokePreservingAccessibility(
                method,
                Invokable::from,
                m -> m.invoke(target),
                () -> format(
                        "Method `%s` invocation on target `%s` of class `%s` failed.",
                        method.getName(), target,
                        target.getClass()
                              .getCanonicalName()));
        return result;
    }

    /**
     * Tries to find a constructor with no arguments in the specified class or its parents.
     *
     * <p>If no such constructor has been found, throws an {@code IllegalArgumentException}.
     *
     * @param type
     *         class to look for constructors in
     * @return a constructor with no parameters, if it exists
     * @throws IllegalArgumentException
     *         if the specified class does not declare a parameterless constructor
     */
    @CanIgnoreReturnValue
    private static <C> Constructor<C> ensureParameterlessCtor(Class<C> type) {
        checkNotNull(type);
        @SuppressWarnings("unchecked" /* safe, as `Class<C>` only declares `Constructor<C>`. */)
        var ctors = (Constructor<C>[]) type.getDeclaredConstructors();
        for (var ctor : ctors) {
            if (ctor.getParameterCount() == 0) {
                return ctor;
            }
        }
        throw newIllegalArgumentException(
                "No parameterless ctor found in class `%s`.",
                type.getSimpleName()
        );
    }

    /**
     * Performs a reflective operation regardless of its accessibility, returns its result.
     *
     * <p>Upon completion, whether successful or erroneous, returns the accessibility to its
     * initial state.
     *
     * @param reflectiveObject
     *         an object that can be used to make an {@code Invokable}
     * @param makeInvokable
     *         a function of {@code P} -> {@code Invokable}. {@code Invokable} is
     *         needed to manipulate the accessibility in a generic manner
     * @param fn
     *         a reflective function to perform
     * @param onError
     *         a supplier of the error message to include into the {@code
     *         IllegalStateException} should an error be thrown
     * @param <T>
     *         a type of reflection-related object to perform a function on
     * @param <R>
     *         a result type of the reflective function
     * @return a result of the reflective function
     * @throws IllegalStateException
     *         if a {@code ReflectiveOperationException} is thrown by {@code fn},
     *         or another error occurs during the reflective operation execution
     */
    /* catching any runtimes does not hurt here. */
    private static <T, R extends @Nullable Object>
    R invokePreservingAccessibility(T reflectiveObject,
                                    Function<T, Invokable<?, ?>> makeInvokable,
                                    ReflectiveFunction<T, R> fn,
                                    Supplier<String> onError) {
        var invokable = makeInvokable.apply(reflectiveObject);
        var accessible = invokable.isAccessible();
        try {
            invokable.setAccessible(true);
            var result = fn.apply(reflectiveObject);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            var message = onError.get();
            throw newIllegalStateException(e, message);
        } finally {
            invokable.setAccessible(accessible);
        }
    }

    /**
     * A function that may throw a reflection-related error on invocation.
     *
     * @param <T>
     *         function input type
     * @param <R>
     *         function output type
     */
    private interface ReflectiveFunction<T, R extends @Nullable Object> {

        R apply(T t) throws ReflectiveOperationException;
    }

    @CanIgnoreReturnValue
    private static IllegalStateException
    newIllegalStateException(Throwable cause, String format, Object... args) {
        var errMsg = formatMessage(format, args);
        throw new IllegalStateException(errMsg, cause);
    }

    @CanIgnoreReturnValue
    private static IllegalArgumentException
    newIllegalArgumentException(String format, Object... args) {
        var errMsg = formatMessage(format, args);
        throw new IllegalArgumentException(errMsg);
    }

    private static String formatMessage(String format, Object[] args) {
        return format(Locale.ROOT, format, args);
    }
}
