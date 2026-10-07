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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`GenericTypeIndex` should")
class GenericTypeIndexTest {

    @Test
    @DisplayName("allow obtaining generic arguments via enums")
    void obtainGenericsViaEnums() {
        assertThat(Pair.GenericParam.FIRST.argumentIn(Tango.class))
             .isEqualTo(Float.class);
        assertThat(Pair.GenericParam.SECOND.argumentIn(Tango.class))
             .isEqualTo(Double.class);
    }

    @SuppressWarnings({"EmptyClass", "unused"})
    private static class Pair<A, B> {

        @SuppressWarnings("rawtypes")
        private enum GenericParam implements GenericTypeIndex<Pair> {
            FIRST(0),
            SECOND(1);

            private final int index;

            GenericParam(int index) {
                this.index = index;
            }

            @Override
            public int index() {
                return index;
            }
        }
    }

    private static class Tango extends Pair<Float, Double> {
    }
}
