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

package org.kordamp.jipsy.processor;

import org.junit.Test;
import org.kordamp.jipsy.processor.testutils.NoOutputTestBase;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class LogLocationTest extends NoOutputTestBase {

    @Test
    public void testMessager() {
        assertTrue(LogLocation.MESSAGER.toMessager());
        assertFalse(LogLocation.MESSAGER.toLogFile());
    }

    @Test
    public void testLogFile() {
        assertFalse(LogLocation.LOG_FILE.toMessager());
        assertTrue(LogLocation.LOG_FILE.toLogFile());
    }

    @Test
    public void testBoth() {
        assertTrue(LogLocation.BOTH.toMessager());
        assertTrue(LogLocation.BOTH.toLogFile());
    }
}
