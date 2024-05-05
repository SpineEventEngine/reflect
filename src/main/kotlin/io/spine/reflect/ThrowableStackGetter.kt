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

/**
 * Default implementation of [StackGetter] using [Throwable.getStackTrace].
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/ThrowableStackGetter.java">
 *       Original Java code of Google Flogger</a>
 */
@Suppress("ThrowingExceptionsWithoutMessageOrCause") // For obtaining current stacktrace.
internal class ThrowableStackGetter : StackGetter {

    override fun callerOf(target: Class<*>, skipFrames: Int): StackTraceElement? {
        checkSkipFrames(skipFrames)
        val stack = Throwable().stackTrace
        val callerIndex = findCallerIndex(stack, target, skipFrames + 1)
        if (callerIndex != -1) {
            return stack[callerIndex]
        }
        return null
    }

    override fun getStackForCaller(
        target: Class<*>,
        maxDepth: Int,
        skipFrames: Int
    ): Array<StackTraceElement> {
        checkMaxDepth(maxDepth)
        checkSkipFrames(skipFrames)
        val stack = Throwable().stackTrace
        val callerIndex = findCallerIndex(stack, target, skipFrames + 1)
        if (callerIndex == -1) {
            return EMPTY_STACK_TRACE
        }
        var elementsToAdd = stack.size - callerIndex
        if (maxDepth in 1..<elementsToAdd) {
            elementsToAdd = maxDepth
        }
        val stackTrace = stack.copyOfRange(callerIndex, elementsToAdd)
        return stackTrace
    }

    companion object {

        private val EMPTY_STACK_TRACE = arrayOf<StackTraceElement>()

        private fun findCallerIndex(
            stack: Array<StackTraceElement>,
            target: Class<*>,
            skipFrames: Int
        ): Int {
            var foundCaller = false
            val targetClassName = target.name
            for (frameIndex in skipFrames..<stack.size) {
                if (stack[frameIndex].className
                    == targetClassName
                ) {
                    foundCaller = true
                } else if (foundCaller) {
                    return frameIndex
                }
            }
            return -1
        }
    }
}
