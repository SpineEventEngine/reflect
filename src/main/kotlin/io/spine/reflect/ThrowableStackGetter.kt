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

    override fun stackForCaller(
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
                val className = stack[frameIndex].className
                if (isTargetClass(className, targetClassName)) {
                    foundCaller = true
                } else if (foundCaller) {
                    return frameIndex
                }
            }
            return -1
        }
    }
}
