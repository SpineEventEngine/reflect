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

import java.lang.StackWalker.Option.SHOW_REFLECT_FRAMES
import java.lang.StackWalker.StackFrame
import java.util.stream.Stream
import kotlin.Long.Companion.MAX_VALUE

/**
 * StackWalker based implementation of the [StackGetter] interface.
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/StackWalkerStackGetter.java">
 *     Original Java code of Google Flogger</a> for historical reference.
 */
internal class StackWalkerStackGetter : StackGetter {

    init {
        // Due to b/241269335, we check in the constructor whether this implementation
        // crashes in runtime, and `CallerFinder` should catch any `Throwable` caused.
        @Suppress("UNUSED_VARIABLE", "unused")
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

    override fun stackForCaller(
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
            isTargetClass: (StackFrame) -> Boolean,
            skipFrames: Int,
            s: Stream<StackFrame>
        ): Stream<StackTraceElement> {
            // Need to skip + 1 because of the call to the method this method is being called from.
            return s.skip((skipFrames + 1).toLong())
                // Skip all classes that do not match the name we are looking for.
                .dropWhile { !isTargetClass(it) }
                // Then skip all that match.
                .dropWhile(isTargetClass)
                .map { frame -> frame.toStackTraceElement() }
        }
    }
}

private fun isTargetClass(target: Class<*>): (StackFrame) -> Boolean = {
    isTargetClass(it.className, target.name)
}

/**
 * A suffix used to identify companion objects in Kotlin in a fully-qualified class name.
 */
private const val COMPANION_SUFFIX = "\$Companion"

/**
 * Determines whether the given class name matches the target class name or its companion object.
 *
 * This function checks if the provided `className` matches the `targetClassName` directly or
 * corresponds to the companion object of the target class. In Kotlin, companion objects are
 * represented with a `$Companion` suffix in their class name.
 *
 * @param className The name of the class to check. Can be `null`.
 *        Typically, this is the name of a class obtained from a stack frame or reflection.
 *        It may be `null` if the stack frame does not contain a valid class name or if reflection
 *        fails to retrieve the class name due to certain runtime conditions.
 * @param targetClassName The name of the target class to match against.
 *        This is the fully qualified name of the class being searched for.
 * @return `true` if the `className` matches the `targetClassName` or its companion object,
 *         `false` otherwise.
 */
internal fun isTargetClass(className: String?, targetClassName: String): Boolean =
    if (className == null) {
        false
    } else {
        (className == targetClassName || className == "$targetClassName$COMPANION_SUFFIX")
    }
