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
package org.kordamp.jipsy.processor;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

/**
 * @author Andres Almiray
 */
public abstract class AbstractResourcePersistence extends AbstractPersistence {
    protected final Filer filer;

    protected AbstractResourcePersistence(Filer filer, String name, Logger logger, String path) {
        super(name, logger, path);
        this.filer = filer;
    }

    @Override
    public Initializer getInitializer() {
        return new DefaultResourceInitializer(filer, path, logger);
    }

    @Override
    public File determineOutputLocation() {
        FileObject resource;
        try {
            resource = getResourceFile("locator");
        } catch (FileNotFoundException | IllegalArgumentException e) {
            // IllegalArgumentException happens when the path is invalid. For instance absolute or relative to a path
            // not part of the class output folder.
            // Due to a bug in javac for Linux, this also occurs when no output path is specified
            // for javac using the -d parameter.
            // See http://forums.sun.com/thread.jspa?threadID=5240999&tstart=45
            // and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6647996
            return null;
        } catch (IOException e) {
            logger.note(LogLocation.MESSAGER, "IOException while determining output location: " + e.getMessage());
            return null;
        }


        URI uri = resource.toUri();
        if (uri.isAbsolute()) {
            return new File(uri).getParentFile();
        }
        return new File(uri.toString()).getParentFile();
    }

    @Override
    public void delete() throws IOException {
        filer.getResource(StandardLocation.CLASS_OUTPUT, "", path + name).delete();
    }

    protected FileObject getResourceFile(String name) throws IOException {
        return filer.getResource(StandardLocation.CLASS_OUTPUT, "", path + name);
    }

    protected FileObject createResourceFile(String name) throws IOException {
        return filer.createResource(StandardLocation.CLASS_OUTPUT, "", path + name);
    }

    @Override
    protected Writer createWriter(String name) throws IOException {
        return createResourceFile(name).openWriter();
    }
}
