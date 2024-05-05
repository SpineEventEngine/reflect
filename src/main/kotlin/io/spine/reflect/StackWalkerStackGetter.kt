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

package io.spine.reflect;

import java.lang.StackWalker.StackFrame;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.spine.reflect.Checks.checkMaxDepth;
import static io.spine.reflect.Checks.checkSkipFrames;

/**
 * StackWalker based implementation of the {@link StackGetter} interface.
 *
 * <p>Note, that since this is using Java 9 api, it is being compiled separately from the rest of
 * the source code.
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/StackWalkerStackGetter.java">
 *         Original Java code from Google Flogger</a>
 */
final class StackWalkerStackGetter implements StackGetter {

    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.SHOW_REFLECT_FRAMES);

    StackWalkerStackGetter() {
        // Due to b/241269335, we check in constructor whether this implementation
        // crashes in runtime, and CallerFinder should catch any Throwable caused.
        @SuppressWarnings("unused")
        var unused = callerOf(StackWalkerStackGetter.class, 0);
    }

    @Override
    public StackTraceElement callerOf(Class<?> target, int skipFrames) {
        checkSkipFrames(skipFrames);
        return STACK_WALKER.walk(
                s -> filterStackTraceAfterTarget(isTargetClass(target), skipFrames, s)
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public StackTraceElement[] getStackForCaller(Class<?> target, int maxDepth, int skipFrames) {
        checkMaxDepth(maxDepth);
        checkSkipFrames(skipFrames);
        return STACK_WALKER.walk(
                s -> filterStackTraceAfterTarget(isTargetClass(target), skipFrames, s)
                        .limit(maxDepth == -1 ? Long.MAX_VALUE : maxDepth)
                        .toArray(StackTraceElement[]::new));
    }

    private static Predicate<StackFrame> isTargetClass(Class<?> target) {
        var name = target.getName();
        return f -> f.getClassName()
                     .equals(name);
    }

    private static Stream<StackTraceElement> filterStackTraceAfterTarget(
            Predicate<StackFrame> isTargetClass, int skipFrames, Stream<StackFrame> s) {
        // need to skip + 1 because of the call to the method this method is being called from
        return s.skip(skipFrames + 1)
                // skip all classes which don't match the name we are looking for
                .dropWhile(isTargetClass.negate())
                // then skip all which matches
                .dropWhile(isTargetClass)
                .map(StackFrame::toStackTraceElement);
    }
}
