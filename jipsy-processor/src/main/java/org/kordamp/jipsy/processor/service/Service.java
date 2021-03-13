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

import org.kordamp.jipsy.processor.LogLocation;
import org.kordamp.jipsy.processor.Logger;
import org.kordamp.jipsy.processor.Provided;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Textual representation of a service interface and the classes implementing it. The class maintains the name of the
 * service, which is the fully qualified name of the interface and the names of the classes that implement this
 * interface and are or should be listed in the {@code META-INF/services} resource directory.
 * <p>
 * The class is mutable and provides methods to add, remove providers to the service.
 */
public final class Service implements Provided {
    private final Logger logger;
    private final String serviceName;
    private final Set<String> providers = new HashSet<>();

    /**
     * Create a new service object.
     *
     * @param logger is the logger used to log operations
     * @param name   the name of the interface. It is not used in this class, except that it can be queried calling
     *               {@link #getName()} and it is also used in {@link #equals(Object)}.
     */
    public Service(Logger logger, String name) {
        requireNonNull(logger, "logger");
        requireNonNull(name, "name");
        this.logger = logger;
        logger.note(LogLocation.LOG_FILE, "Creating " + name);
        this.serviceName = name;
    }

    /**
     * Add a ne provider to the existing set of providers.
     *
     * @param provider the full name of the class implementing the service interface, a.k.a. the provider class
     */
    public void addProvider(String provider) {
        requireNonNull(provider, "provider");
        logger.note(LogLocation.LOG_FILE, "Adding " + provider + " to " + serviceName);
        providers.add(provider);
    }

    /**
     * Returns true if the service contains the specific provider
     *
     * @param provider the full name of the class implementing the service interface, a.k.a. the provider class
     * @return {@code true} iff the service contains the provider
     */
    public boolean contains(String provider) {
        return providers.contains(provider);
    }

    /**
     * Remove a provider from the service.
     * <p>
     * This method is called for all the known services when a provider is removed. It is invoked for the services that
     * the provider does not implement. Therefore this is absolutely normal case that the caller calls this method and
     * the service does NOT have the given provider. In that case just do not log it. In the structure we do not
     * maintain the interfaces a provider implements. There is no need.
     *
     * @param provider the full name of the class implementing the service interface, a.k.a. the provider class
     * @return {@code true} if the provider was registered in the class and {@code false} otherwise
     */
    public boolean removeProvider(String provider) {
        if (providers.remove(provider)) {
            logger.note(LogLocation.LOG_FILE, "Removing " + provider + " from " + serviceName);
            return true;
        }
        return false;
    }

    /**
     * Get the name of the service. This is the full name of the interface that represents the service.
     *
     * @return the name of the service.
     */
    public String getName() {
        return serviceName;
    }

    /**
     * Return the names of the service providers in a string as several lines, each line terminated with a {@code \n}
     * character including the last line.
     * <p>
     * The names of the classes are sorted and they are joined together. This form is appropriate to be written into the
     * {@code META-INF/services/service.interface.full.name} file.
     *
     * @return the list of the service names in a string, each terminated with a {@code \n}.
     */
    public String toProviderNamesList() {
        StringBuilder sb = new StringBuilder();
        List<String> names = new ArrayList<>(providers);
        Collections.sort(names);
        for (String provider : names) {
            sb.append(provider).append("\n");
        }
        return sb.toString();
    }

    /**
     * Read a string and add the providers that are listed in the string.
     * <p>
     * The format of the string is the same as the file {@code META-INF/services/service.interface.full.name} file.
     * Empty lines an the part of the lines that follow the {@code #} comment character are ignored.
     *
     * @param input the content presumably read from a {@code INF/services/service.interface.full.name} file
     */
    public void fromProviderNamesList(String input) {
        requireNonNull(input, "input");
        String[] lines = input.split("\\n");
        for (String line : lines) {
            final String[] content = line.split("#");
            if (content.length > 0) {
                String trimmed = content[0].trim();
                if (trimmed.length() > 0) {
                    addProvider(trimmed);
                }
            }
        }
    }

    @Override
    public String toString() {
        return serviceName + "=" + providers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        return serviceName.equals(service.serviceName) &&
            providers.containsAll(service.providers) && service.providers.containsAll(providers);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + providers.hashCode();
        return result;
    }
}