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

import org.kordamp.jipsy.processor.Logger;
import org.kordamp.jipsy.processor.Provided;

/**
 * Textual representation of a service interface and the classes implementing it. The class maintains the name of the
 * service, which is the fully qualified name of the interface and the names of the classes that implement this
 * interface and are or should be listed in the {@code META-INF/services} resource directory.
 * <p>
 * The class is mutable and provides methods to add, remove providers to the service.
 * @author Andres Almiray
 */
public final class Service extends Provided {

    /**
     * Create a new service object.
     *
     * @param logger is the logger used to log operations
     * @param name   the name of the interface. It is not used in this class, except that it can be queried calling
     *               {@link #getName()} and it is also used in {@link #equals(Object)}.
     */
    public Service(Logger logger, String name) {
        super(logger, name);
    }
}