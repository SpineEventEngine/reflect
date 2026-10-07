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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`IsDirectParent` predicate should")
class IsDirectParentTest {

    private static void assertDirectParent(Package parentCandidate, Package child) {
        assertTrue(IsDirectParent.of(child).test(parentCandidate));
    }

    private static void assertNotDirectParent(Package parentCandidate, Package child) {
        assertFalse(IsDirectParent.of(child).test(parentCandidate));
    }

    @Test
    @DisplayName("accept direct parent package")
    void directParent() {
        var javaUtilFunction = Predicate.class.getPackage();
        var javaUtil = Collection.class.getPackage();
        assertDirectParent(javaUtil, javaUtilFunction);
    }

    @Test
    @DisplayName("reject indirect parent package")
    void rejectIndirectParent() {
        var javaUtilConcurrentLocks = Lock.class.getPackage();
        var javaUtil = Collection.class.getPackage();
        assertNotDirectParent(javaUtil, javaUtilConcurrentLocks);
    }

    @Test
    @DisplayName("reject sibling package")
    void rejectSibling() {
        var javaUtilFunction = Predicate.class.getPackage();
        var javaUtilConcurrent = Callable.class.getPackage();
        assertNotDirectParent(javaUtilConcurrent, javaUtilFunction);
    }

    @Test
    @DisplayName("reject cousin package")
    void rejectCousins() {
        var javaLangAnnotation = Annotation.class.getPackage();
        var javaUtilFunction = Predicate.class.getPackage();
        assertNotDirectParent(javaUtilFunction, javaLangAnnotation);
    }
}
