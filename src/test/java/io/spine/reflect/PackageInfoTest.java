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

import com.google.common.testing.EqualsTester;
import given.reflect.annotation.ValueAnnotation;
import given.reflect.root.branch1.bar.LastVisitor;
import given.reflect.root.branch1.foo.sub1.Sub1Class;
import given.reflect.root.branch1.foo.sub2.Sub2Class;
import given.reflect.root.branch2.lorem.ipsum.Sub3Class;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`PackageInfo` should")
class PackageInfoTest {

    private final PackageInfo javaUtil = PackageInfo.of(Collection.class.getPackage());
    private final PackageInfo javaUtilConcurrent = PackageInfo.of(Callable.class.getPackage());

    @Test
    @DisplayName("return package name in `toString()`")
    void stringify() {
        assertEquals(javaUtil.getValue().getName(), javaUtil.toString());
    }

    @Test
    @DisplayName("have `equals()` and `hashCode()`")
    void hashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(javaUtil, PackageInfo.of(Collection.class.getPackage()))
                .addEqualityGroup(javaUtilConcurrent)
                .testEquals();
    }

    @Nested
    @DisplayName("obtain `Annotation`")
    class FindAnnotation {

        @Test
        @DisplayName("present directly in the package")
        void presentDirectly() {
            var pkg = Sub1Class.class.getPackage();
            var annotation = assertAnnotated(pkg);

            var packageInfo = PackageInfo.of(pkg);
            var optional = packageInfo.findAnnotation(ValueAnnotation.class);
            assertTrue(optional.isPresent());
            assertEquals(annotation, optional.get());
        }

        @Test
        @DisplayName("present in immediate parent package")
        void fromImmediateParent() {
            var pkg = Sub2Class.class.getPackage();
            assertNotAnnotated(pkg);
            assertFound(pkg);
        }

        @Test
        @DisplayName("present in a parent above")
        void fromParentAbove() {
            var pkg = Sub3Class.class.getPackage();
            assertNotAnnotated(pkg);
            assertFound(pkg);
        }

        private void assertFound(Package pkg) {
            var packageInfo = PackageInfo.of(pkg);
            var optional = packageInfo.findAnnotation(ValueAnnotation.class);
            assertTrue(optional.isPresent());
        }

        private ValueAnnotation assertAnnotated(Package pkg) {
            var annotation = pkg.getAnnotation(ValueAnnotation.class);
            // Make sure that the package is annotated in the test environment.
            assertNotNull(annotation);
            return annotation;
        }

    }

    @Test
    @DisplayName("tell if there is not annotation")
    void notFound() {
        var pkg = LastVisitor.class.getPackage();
        assertNotAnnotated(pkg);
        assertFalse(PackageInfo.of(pkg)
                               .findAnnotation(ValueAnnotation.class)
                               .isPresent());
    }

    private static void assertNotAnnotated(Package pkg) {
        var annotation = pkg.getAnnotation(ValueAnnotation.class);
        // Make sure that the package is NOT annotated in the test environment.
        assertNull(annotation);
    }
}
