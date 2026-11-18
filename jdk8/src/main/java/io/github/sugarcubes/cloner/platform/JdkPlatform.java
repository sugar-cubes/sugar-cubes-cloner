/*
 * Copyright 2017-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner.platform;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.github.sugarcubes.cloner.ModuleOpening;
import io.github.sugarcubes.cloner.spi.ContainerModel;
import io.github.sugarcubes.cloner.spi.FieldAdapterFactory;
import io.github.sugarcubes.cloner.spi.Reflection;
import io.github.sugarcubes.cloner.spi.ValuesModel;
import io.github.sugarcubes.cloner.unsafe.UnsafeBridge;
import io.github.sugarcubes.cloner.util.Checks;

/**
 * Everything which differs between JDK versions. The implementation for the running JDK is chosen by the
 * multi-release jar: {@code JdkPlatformImpl$Jdk8}, {@code $Jdk9}, {@code $Jdk15}, {@code $Jdk16}, each listing the
 * differences of its version.
 *
 * @author Maxim Butov
 */
public interface JdkPlatform {

    /**
     * Returns the platform of the running JDK.
     *
     * @return platform
     */
    static JdkPlatform current() {
        return Current.PLATFORM;
    }

    /**
     * Creates reflection which opens packages to the cloner with the mechanism before making members accessible (JDK
     * 9+), fails with {@link io.github.sugarcubes.cloner.ClonerException} if the mechanism is not available.
     *
     * @param opening module opening mechanism
     * @param unsafe Unsafe bridge, requested only if the mechanism needs it
     * @return reflection
     */
    Reflection reflection(ModuleOpening opening, Supplier<UnsafeBridge> unsafe);

    /**
     * Creates reflection which opens nothing.
     *
     * @return reflection
     */
    default Reflection reflection() {
        return reflection(ModuleOpening.NONE, () -> {
            throw Checks.mustNotHappen();
        });
    }

    /**
     * Returns the factory of field adapters using {@code VarHandle}, JDK 9+.
     *
     * @return factory or empty on JDK 8
     */
    Optional<FieldAdapterFactory> varHandleFieldAdapters();

    /**
     * Returns the container model of a well-known JDK collection or map.
     *
     * @param <T> type
     * @param type type
     * @return model or empty if the type is not a supported container
     */
    <T> Optional<ContainerModel<T>> containerModel(Class<T> type);

    /**
     * Creates the best Unsafe bridge available.
     *
     * @param reflection reflection to access {@code sun.misc.Unsafe}
     * @return Unsafe bridge
     */
    UnsafeBridge unsafeBridge(Reflection reflection);

    /**
     * Tells whether the class is hidden (a hidden class on JDK 15+ or a lambda class): it cannot be found by name, its
     * instances cannot be allocated by name and their final fields are not writable.
     *
     * @param type class
     * @return {@code true} if the class is hidden
     */
    boolean isHidden(Class<?> type);

    /**
     * Returns the values model of the type, e.g. of a record (JDK 16+).
     *
     * @param <T> type
     * @param type type
     * @param reflection reflection to access members of the type
     * @return model or empty if instances of the type are not created from values
     */
    <T> Optional<ValuesModel<T>> valuesModel(Class<T> type, Reflection reflection);

    /**
     * Tells whether instances of the type are immutable and may be shared: values, canonical instances (e.g. locales,
     * currencies, charsets, zones), classes of stateless singletons.
     *
     * @param type type
     * @return {@code true} if instances are immutable
     */
    boolean isImmutable(Class<?> type);

    /**
     * Tells whether instances of the type are copied with their public {@code clone()} method.
     *
     * @param type type
     * @return {@code true} if instances are copied with {@code clone()}
     */
    boolean isCloneable(Class<?> type);

    /**
     * Tells whether instances of the type are objects of the running JVM (threads, class loaders, modules, reflection
     * objects, references), which cannot be copied meaningfully.
     *
     * @param type type
     * @return {@code true} for objects of the running JVM
     */
    boolean isRuntime(Class<?> type);

    /**
     * Returns system-wide JDK singletons whose classes have other instances, e.g. empty unmodifiable wrappers.
     *
     * @return system-wide JDK singletons
     */
    Set<Object> getSystemWideSingletons();

    /**
     * Holder of the current platform, not for direct use.
     */
    final class Current {

        static final JdkPlatform PLATFORM = new JdkPlatformImpl();

        private Current() {
        }

    }

}
