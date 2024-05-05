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

import java.lang.StackWalker.Option.SHOW_REFLECT_FRAMES
import java.lang.StackWalker.StackFrame
import java.util.function.Predicate
import java.util.stream.Stream
import kotlin.Long.Companion.MAX_VALUE
import kotlin.collections.toTypedArray
import kotlin.streams.toList

/**
 * StackWalker based implementation of the [StackGetter] interface.
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/StackWalkerStackGetter.java">
 *     Original Java code of Google Flogger</a>
 */
internal class StackWalkerStackGetter : StackGetter {

    init {
        // Due to b/241269335, we check in constructor whether this implementation
        // crashes in runtime, and CallerFinder should catch any Throwable caused.
        @Suppress("UNUSED_VARIABLE")
        val unused = callerOf(StackWalkerStackGetter::class.java, 0)
    }

    override fun callerOf(target: Class<*>, skipFrames: Int): StackTraceElement? {
        checkSkipFrames(skipFrames)
        return STACK_WALKER.walk { stream ->
            filterStackTraceAfterTarget(isTargetClass(target), skipFrames, stream)
                .findFirst()
                .orElse(null)
        }
    }

    override fun getStackForCaller(
        target: Class<*>,
        maxDepth: Int,
        skipFrames: Int
    ): Array<StackTraceElement> {
        checkMaxDepth(maxDepth)
        checkSkipFrames(skipFrames)
        return STACK_WALKER.walk { stream ->
            filterStackTraceAfterTarget(isTargetClass(target), skipFrames, stream)
                .limit(if (maxDepth == -1) MAX_VALUE else maxDepth.toLong())
                .toList()
                .toTypedArray()
        }
    }

    companion object {

        private val STACK_WALKER: StackWalker = StackWalker.getInstance(SHOW_REFLECT_FRAMES)

        private fun filterStackTraceAfterTarget(
            isTargetClass: Predicate<StackFrame>,
            skipFrames: Int,
            s: Stream<StackFrame>
        ): Stream<StackTraceElement> {
            // need to skip + 1 because of the call to the method this method is being called from.
            return s.skip((skipFrames + 1).toLong())
                // Skip all classes which don't match the name we are looking for.
                .dropWhile(isTargetClass.negate())
                // Then skip all which matches.
                .dropWhile(isTargetClass)
                .map { frame -> frame.toStackTraceElement() }
        }
    }
}

private fun isTargetClass(target: Class<*>): Predicate<StackFrame> =
    Predicate { frame -> (frame.className == target.name) }
