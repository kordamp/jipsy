/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2013 - 2022 Andres Almiray.
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

import java.util.*;

/**
 * Maintain a list of services. You can query the services by the name and in case service is not in the list it will
 * be loaded from the {@code META-INF/services} directory files.
 * <p>
 * At any time the list of the services can be cached, which is a kind of snapshot and later you can query if there was
 * any modification to the list of the service.
 */
public final class ServiceCollector {
    private final Map<String, Service> services = new LinkedHashMap<String, Service>();
    private final Map<String, Service> cached = new LinkedHashMap<String, Service>();

    private final List<String> removed = new ArrayList<String>();
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
        if (cached.size() != services.size()) {
            return true;
        }

        for (Map.Entry<String, Service> e : cached.entrySet()) {
            if (!services.containsKey(e.getKey())) {
                return true;
            }
            if (!e.getValue().equals(services.get(e.getKey()))) {
                return true;
            }
        }

        return false;
    }

    public Service getService(String service) {
        if (service == null) {
            throw new NullPointerException("service");
        }
        if (!services.containsKey(service)) {
            Service newService = new Service(logger, service);
            CharSequence initialData = initializer.initialData(service);
            if (initialData != null) {
                newService.fromProviderNamesList(initialData.toString());
                for (String provider : removed) {
                    newService.removeProvider(provider);
                }
            }
            services.put(service, newService);
        }
        return services.get(service);
    }

    public Collection<Service> services() {
        return Collections.unmodifiableMap(services).values();
    }

    public void removeProvider(String provider) {
        if (provider == null) {
            throw new NullPointerException("provider");
        }
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
