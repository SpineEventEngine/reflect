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

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Verifiers if a package is a direct "parent" of the specified child.
 */
@Immutable
final class IsDirectParent implements Predicate<Package> {

    private final String childName;

    private IsDirectParent(Package child) {
        this.childName = child.getName();
    }

    static Predicate<Package> of(Package child) {
        checkNotNull(child);
        var result = new IsDirectParent(child);
        return result;
    }

    @Override
    public boolean test(Package candidate) {
        var commonPrefix = Strings.commonPrefix(candidate.getName(), childName);
        if (commonPrefix.isEmpty()) {
            return false;
        }
        var remainingPath = childName.substring(commonPrefix.length());
        var hasOnlyOneDot = remainingPath.lastIndexOf('.') == 0;
        return hasOnlyOneDot;
    }
}
