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

package io.spine.reflect.given;

import static java.lang.String.format;

/**
 * This is an environment for {@linkplain io.spine.reflect.InvokablesTest testing
 * constructor-related} utilities.
 */
@SuppressWarnings("unused" /* need unused members for reflection lookup. */)
public final class ConstructorsTestEnv {

    /** Prevents instantiation of this test env class. */
    private ConstructorsTestEnv() {
    }

    public static class NoParameterlessConstructors {

        @SuppressWarnings("FieldCanBeLocal")
        private final int id;

        public NoParameterlessConstructors(int id) {
            this.id = id;
        }
    }

    public static class ClassWithDefaultCtor {

        public boolean instantiated() {
            return true;
        }
    }

    public abstract static class Animal {

        public static final String MISSING = "missing";
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        Animal() {
            this.name = MISSING;
        }

        public abstract String makeSound();

        public final String greet() {
            return makeSound() + format(". My name is %s.", name);
        }
    }

    public static final class Cat extends Animal {

        public Cat(String name) {
            super(name);
        }

        public Cat() {
            super();
        }

        @Override
        public String makeSound() {
            return "Meow";
        }
    }

    public final class Chicken extends Animal {

        @Override
        public String makeSound() {
            return "Cluck";
        }
    }

    public static class ClassWithPrivateCtor {

        private ClassWithPrivateCtor() {
        }

        public boolean instantiated() {
            return true;
        }
    }

    public static class ThrowingConstructor {

        private ThrowingConstructor() {
            throw new IllegalStateException("");
        }

        public boolean instantiated() {
            return true;
        }
    }
}
