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

@file:JvmName("CallerFinder")

package io.spine.reflect

/**
 * A helper object for determining callers of a specified class currently on the stack.
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/CallerFinder.java">
 *      Original Java code of Google Flogger</a>
 */
public object CallerFinder {

    private val stackGetter by lazy {
        createBestStackGetter()
    }

    /**
     * Returns the stack trace element of the immediate caller of the specified class.
     *
     * @param target The target class whose callers we are looking for.
     * @param skip The minimum number of calls known to have occurred between the first call to the
     *        target class and the point at which the specified throwable was created.
     *        If in doubt, specify zero here to avoid accidentally skipping past the caller.
     *        This is particularly important for code which might be used in Android, since you
     *        cannot know whether a tool such as Proguard has merged methods or classes and
     *        reduced the number of intermediate stack frames.
     * @return the stack trace element representing the immediate caller of the specified class, or
     *        `null` if no caller was found (due to incorrect target, wrong skip count or
     *        use of JNI).
     */
    @JvmStatic
    public fun findCallerOf(target: Class<*>?, skip: Int): StackTraceElement? {
        checkSkipCount(skip)
        return stackGetter.callerOf(target!!, skip + 1)
    }

    /**
     * Returns a synthetic stack trace starting at the immediate caller of the specified target.
     *
     * @param target The class who is the caller the returned stack trace will start at.
     * @param maxDepth The maximum size of the returned stack (pass -1 for the complete stack).
     * @param skip The minimum number of stack frames to skip before looking for callers.
     * @return a synthetic stack trace starting at the immediate caller of the specified target, or
     *        the empty array if no caller was found (due to incorrect target, wrong skip count or
     *        use of JNI).
     */
    @JvmStatic
    public fun stackForCallerOf(
        target: Class<*>,
        maxDepth: Int,
        skip: Int
    ): Array<StackTraceElement> {
        require((maxDepth > 0 || maxDepth == -1)) { "invalid maximum depth: $maxDepth." }
        checkSkipCount(skip)
        return stackGetter.stackForCaller(target, maxDepth, skip + 1)
    }

    /**
     * Returns the first available class implementing the [StackGetter] methods.
     * The implementation returned is dependent on the current Java version.
     */
    private fun createBestStackGetter(): StackGetter {
        return try {
            StackWalkerStackGetter()
        } catch (_: Throwable) {
            // We may not be able to create `StackWalkerStackGetter` sometimes,
            // for example, on Android. This is not a problem because we have
            // `ThrowableStackGetter` as a fallback option.
            ThrowableStackGetter()
        }
    }

    private fun checkSkipCount(skip: Int) =
        require(skip >= 0) { "skip can't be negative: $skip." }
}
