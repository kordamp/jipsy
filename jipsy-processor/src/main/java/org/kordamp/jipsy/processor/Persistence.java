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
package org.kordamp.jipsy.processor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface Persistence {
    void writeLog();

    Initializer getInitializer();

    /**
     * Write the value to the file named {@code name} and also a {@code # generated by...} line in front of it.
     *
     * @param name  the name of the file where to write
     * @param value the value to be written into the file
     * @throws IOException when the file cannot be written
     */
    void write(String name, String value) throws IOException;

    /**
     * @return the directory where the output should be persisted. The actual location is determined looking for the
     * resource named {@code "locator"}.
     */
    File determineOutputLocation();

    /**
     * Get the files from the output locations.
     *
     * @return the names of the file, which are in the output location and are not {@code .log} files.
     */
    Collection<String> tryFind();

    Collection<String> listDiscoveredFiles(File[] list);

    /**
     * Delete the file that should contain the resource.
     *
     * @throws IOException if the deletion of the resource is not possible for some reason
     */
    void delete() throws IOException ;
}
