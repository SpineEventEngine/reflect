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
 * A collection of packages annotated using the annotation of the type [T].
 */
public class AnnotatedPackages<T: Annotation>(
    /**
     * The class of annotation [T] applied to the packages in this collection.
     */
    public val annotationClass: Class<T>
) {

    /**
     * A list of packages annotated with [annotationClass] listed in reverse
     * alphabetical order of the package names.
     */
    public val packages: List<Package>

    init {
        val allPackages = Package.getPackages()
        packages = allPackages.filter { it.findAnnotation(annotationClass) != null }
            .sortedBy { it.name }
            .reversed()
            .toList()
    }

    /**
     * Tells if the annotated packages has the given package directly or as a sub-package
     * of one in the collection.
     */
    public fun findWithNesting(p: Package): T? {
        val found = packages.firstOrNull {
            p.name.startsWith(it.name)
        }
        return found?.findAnnotation(annotationClass)
    }
}

private fun <T: Annotation> Package.findAnnotation(cls: Class<in T>): T? {
    @Suppress("UNCHECKED_CAST")
    return annotations.firstOrNull { cls.isInstance(it) } as T?
}
