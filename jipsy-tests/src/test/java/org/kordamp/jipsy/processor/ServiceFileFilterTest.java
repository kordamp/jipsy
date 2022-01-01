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

package org.kordamp.jipsy.processor;

import junit.framework.Assert;
import org.junit.Test;
import org.kordamp.jipsy.processor.testutils.NoOutputTestBase;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ServiceFileFilterTest extends NoOutputTestBase {

    @Test
    public void testNotNull() {
        Assert.assertNotNull(SimpleFileFilter.INSTANCE);
    }

    @Test
    public void testLogFile() throws Exception {
        File file = File.createTempFile("test", ".log");
        try {
            FileFilter filter = SimpleFileFilter.INSTANCE;
            Assert.assertFalse(filter.accept(file));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testDirectory() throws Exception {
        File file = File.createTempFile("test", ".log");
        try {
            FileFilter filter = SimpleFileFilter.INSTANCE;
            Assert.assertFalse(filter.accept(file.getParentFile()));
        } finally {
            file.delete();
        }
    }

    @Test
    public void testServiceFile() throws Exception {
        File file = File.createTempFile("test", ".tmp");
        try {
            FileFilter filter = SimpleFileFilter.INSTANCE;
            Assert.assertTrue(filter.accept(file));
        } finally {
            file.delete();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testInstantiation() throws Throwable {
        Constructor<SimpleFileFilter> constructor = SimpleFileFilter.class
            .getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
