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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base interface for enumerations on generic parameters of types.
 *
 * <p>Such enumeration are convenient when it is needed to obtain generic arguments
 * of a class on runtime.
 *
 * <p>Example of implementing an enumeration for generic parameters:
 * <pre>
 * {@code
 * public abstract class Tuple<K, V> {
 *     ...
 *     public enum GenericParameter implements GenericTypeIndex<Tuple> {
 *
 *         // <K> param has index 0
 *         KEY(0),
 *
 *         // <V> param has index 1
 *         VALUE(1);
 *
 *         private final int index;
 *
 *         GenericParameter(int index) { this.index = index; }
 *
 *         {@literal @}Override
 *         public int index() { return index; }
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Then the usage of these enum entries would be like this:
 * <pre>
 *    var keyClass = Tuple.GenericParameter.argumentIn(ClassExtendingTuple.class);
 *    var valueClass = Tuple.GenericParameter.argumentIn(ClassExtendingTuple.class);
 * </pre>
 *
 * @param <C>
 *         the type for which class the generic index is declared
 */
public interface GenericTypeIndex<C> {

    /**
     * Obtains a zero-based index of the generic type parameter.
     */
    int index();

    /**
     * Obtains the class of the generic type argument.
     *
     * @param cls
     *         the class to inspect
     * @return the argument class
     * @implNote Obtain the superclass of the passed one by inspecting the class which
     *         implements {@code GenericTypeIndex}.
     */
    default Class<?> argumentIn(Class<? extends C> cls) {
        checkNotNull(cls);
        var indexClass = getClass();
        @SuppressWarnings("unchecked") /* The type cast is ensured by the declaration of
            the `GenericTypeIndex` interface. */
        var superclassOfPassed = (Class<C>) Types.argumentIn(indexClass, 0, GenericTypeIndex.class);
        var result = Types.argumentIn(cls, index(), superclassOfPassed);
        return result;
    }
}
