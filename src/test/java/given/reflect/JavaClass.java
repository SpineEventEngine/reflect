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

package given.reflect;

/**
 * A stub Java class with public no-arg constructor to be used
 * by {@link io.spine.reflect.FactorySpec} tests.
 */
@SuppressWarnings("unused") // This class is used by its name.
public class JavaClass {

    public JavaClass() {
        // Do nothing.
    }

    /**
     * The class nested into another one.
     */
    public static class NestedClass {

        public NestedClass() {
            // Do nothing.
        }
    }

    /**
     * The class without a public no-arg constructor.
     */
    public static class WithoutRequiredConstructor {

        private WithoutRequiredConstructor() {
            // Do nothing.
        }
    }
}
