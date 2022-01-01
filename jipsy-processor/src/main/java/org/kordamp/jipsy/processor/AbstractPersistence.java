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
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPersistence implements Persistence {
    protected final String name;
    protected final String path;
    protected final Logger logger;

    public AbstractPersistence(String name, Logger logger, String path) {
        this.name = name;
        this.logger = logger;
        this.path = path;
    }

    @Override
    public void writeLog() {
        try {
            String logContent = logger.getFileContent();
            if (logContent != null && !logContent.isEmpty()) {
                write("log" + System.currentTimeMillis() + ".log", logContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(String name, String value) throws IOException {
        logger.note(LogLocation.BOTH, "Generating file '" + path + name + "'");
        Writer writer = createWriter(name);
        try {
            writer.write("# Generated by " + this.name + "\n");
            writer.write(value);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public Collection<String> tryFind() {
        Collection<String> fileList;
        File dir = determineOutputLocation();
        if (dir == null) {
            fileList = Collections.emptyList();
        } else {
            fileList = listDiscoveredFiles(dir.listFiles(getFileFilter()));
        }
        return fileList;
    }

    @Override
    public Collection<String> listDiscoveredFiles(File[] list) {
        if (list == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (File file : list) {
            String fileName = file.getName();
            logger.note(LogLocation.LOG_FILE, "Discovered " + fileName);
            result.add(fileName);
        }
        return result;
    }

    protected abstract Writer createWriter(String name) throws IOException;

    protected abstract FileFilter getFileFilter();
}
