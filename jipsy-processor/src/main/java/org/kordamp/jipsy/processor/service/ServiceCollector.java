/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2013 - 2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

Copyright 2008-2020 TOPdesk, the Netherlands

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package org.kordamp.jipsy.processor.service;

import org.kordamp.jipsy.processor.Initializer;
import org.kordamp.jipsy.processor.LogLocation;
import org.kordamp.jipsy.processor.Logger;
import org.kordamp.jipsy.processor.ProvidedCollector;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Maintain a list of services. You can query the services by the name and in case service is not in the list it will
 * be loaded from the {@code META-INF/services} directory files.
 * <p>
 * At any time the list of the services can be cached, which is a kind of snapshot and later you can query if there was
 * any modification to the list of the service.
 */
public final class ServiceCollector implements ProvidedCollector {
    private final Map<String, Service> services = new LinkedHashMap<>();
    private final Map<String, Service> cached = new LinkedHashMap<>();

    private final List<String> removed = new ArrayList<>();
    private final Initializer initializer;
    private final Logger logger;

    public ServiceCollector(Initializer initializer, Logger logger) {
        this.initializer = initializer;
        this.logger = logger;
    }

    /**
     * Create a snapshot of the services into the cache.
     */
    public void cache() {
        this.cached.putAll(services);
    }

    /**
     *
     * @return {@code true} if there was a modification since the service was cached/snapshot made.
     */
    public boolean isModified() {
        return cached.size() != services.size() ||
            cached.entrySet().stream().anyMatch(
                e -> !services.containsKey(e.getKey()) || !e.getValue().equals(services.get(e.getKey())));
    }

    /**
     * Get a service for the name of the interface name. If the service is not in the list for this interface name then
     * load it looking at the accordingly named {@code META-INF/services/xxx} files.
     *
     * @param service the fully qualified name of the interface the service providers have to implement.
     * @return the service instance.
     */
    @Override
    public Service get(String service) {
        requireNonNull(service,"service");
        return services.computeIfAbsent(service, (s) -> {
            Service newService = new Service(logger, s);
            CharSequence initialData = initializer.initialData(s);
            if (initialData != null) {
                newService.fromProviderNamesList(initialData.toString());
                for (String provider : removed) {
                    newService.removeProvider(provider);
                }
            }
            return newService;
        });
    }

    /**
     * @return the collection of the services
     */
    @Override
    public Collection<Service> values() {
        return Collections.unmodifiableMap(services).values();
    }

    /**
     * Remove a provider from all the services that it provides.
     *
     * @param provider the fully qualified name of the class
     */
    public void removeProvider(String provider) {
        requireNonNull(provider,"provider");
        logger.note(LogLocation.LOG_FILE, "Removing " + provider);
        removed.add(provider);
        for (Service service : services.values()) {
            service.removeProvider(provider);
        }
    }

    @Override
    public String toString() {
        return services.values().toString();
    }
}
