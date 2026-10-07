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
 * Interface for finding call site information.
 *
 * @see <a href="https://github.com/google/flogger/blob/cb9e836a897d36a78309ee8badf5cad4e6a2d3d8/api/src/main/java/com/google/common/flogger/util/StackGetter.java">
 *      Original Java code of Google Flogger</a> for historical reference.
 */
internal interface StackGetter {

    /**
     * Returns the first caller of a method on the [target] class that is *not* a member of
     * the `target` class.
     *
     * The caller is obtained by walking back on the stack.
     *
     * @param target The class to find the caller of.
     * @param skipFrames The number of frames to skip before looking for the caller.
     *        This can be used for optimization.
     * @return the first caller of the method or `null` if the `target` class
     *        cannot be found or is the last element of the stack.
     */
    fun callerOf(target: Class<*>, skipFrames: Int): StackTraceElement?

    /**
     * Returns up to `maxDepth` frames of the stack starting at the stack frame that
     * is a caller of a method on `target` class but is *not* itself a method
     * on `target` class.
     *
     * @param target The class to get the stack from.
     * @param maxDepth The maximum depth of the stack to return.
     *        A value of `-1` means to return the whole stack.
     * @param skipFrames The number of frames to skip before looking for the target class.
     *         Used for optimization.
     * @throws IllegalArgumentException if `maxDepth` is 0 or < -1 or `skipFrames` is < 0.
     */
    fun stackForCaller(
        target: Class<*>,
        maxDepth: Int,
        skipFrames: Int
    ): Array<StackTraceElement>
}

internal fun checkMaxDepth(maxDepth: Int) =
    require(maxDepth == -1 || maxDepth > 0) { "maxDepth must be > 0 or -1" }

internal fun checkSkipFrames(skipFrames: Int) =
    require(skipFrames >= 0) { "skipFrames must be >= 0" }
