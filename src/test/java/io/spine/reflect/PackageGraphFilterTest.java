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

import com.google.common.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertHasPrivateParameterlessCtor;
import static io.spine.testing.DisplayNames.HAVE_PARAMETERLESS_CTOR;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PackageGraph.Filter should")
class PackageGraphFilterTest {

    private PackageGraph.Filter filter;

    @BeforeEach
    void setUp() {
        filter = PackageGraph.newFilter()
                .include("io.spine.reflect")
                .exclude("java");
    }

    @Test
    @DisplayName("accept included packages")
    void inclusion() {
        assertTrue(filter.test(getClass().getPackage()));
    }

    @Test
    @DisplayName("reject excluded packages")
    void exclusion() {
        assertFalse(filter.test(String.class.getPackage()));
    }

    @Test
    @DisplayName("accept by default")
    void acceptances() {
        assertTrue(filter.test(Graph.class.getPackage()));
    }

    @Test
    @DisplayName(HAVE_PARAMETERLESS_CTOR)
    void privateCtor() {
        assertHasPrivateParameterlessCtor(PackageGraph.Filter.class);
    }
}
