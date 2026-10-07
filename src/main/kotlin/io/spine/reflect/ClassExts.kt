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
 * Obtains the class of a generic type argument which is specified in the inheritance chain
 * of the passed class.
 *
 * @receiver the end class for which we find the generic argument.
 * @param [T] the type of superclass.
 * @param argNumber the index of the generic parameter in the superclass.
 * @return the class of the generic type argument
 */
public inline fun <reified T : Any> Class<out T>.argumentIn(argNumber: Int): Class<*> =
    Types.argumentIn(this, argNumber, T::class.java)
