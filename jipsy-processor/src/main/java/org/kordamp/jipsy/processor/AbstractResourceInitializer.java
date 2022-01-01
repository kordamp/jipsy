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

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Andres Almiray
 */
public abstract class AbstractResourceInitializer implements Initializer {
    protected final Filer filer;
    protected final String path;
    protected final Logger logger;

    public AbstractResourceInitializer(Logger logger, String path, Filer filer) {
        this.logger = logger;
        this.path = path;
        this.filer = filer;
    }

    @Override
    public CharSequence initialData(String name) {
        try {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path + name);

            CharSequence result;
            try {
                // Eclipse can't handle the getCharContent
                // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=246089
                // 2008-09-12 RoelS: I've posted a patch file
                result = tryWithReader(resource);
            } catch (FileNotFoundException e) {
                // Could happen
                return null;
            } catch (IOException e) {
                logger.note(LogLocation.MESSAGER, "Eclipse gave an IOException: " + e.getMessage());
                return null;
            } catch (Exception other) {
                try {
                    // Javac can't handle the openReader
                    // Filed as a bug at bugs.sun.com and received a review ID: 1339738
                    result = resource.getCharContent(true);
                } catch (FileNotFoundException e) {
                    // Could happen
                    return null;
                } catch (IOException e) {
                    logger.note(LogLocation.MESSAGER, "Javac gave an IOException: " + e.getMessage());
                    return null;
                }
            }
            return result;
        } catch (IOException e) {
            logger.note(LogLocation.MESSAGER, "getResource gave an IOException: " + e.getMessage());
        }
        return null;
    }

    protected CharSequence tryWithReader(FileObject resource) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(resource.openReader(true));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            reader.close();
        }
        return sb;
    }
}
