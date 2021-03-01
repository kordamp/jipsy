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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class CheckResultTest extends NoOutputTestBase {

    @Test(expected = NullPointerException.class)
    public void testNullMessage() {
        CheckResult.valueOf(null);
    }

    @Test
    public void testSomeMessage() {
        CheckResult result = CheckResult.valueOf("Error");
        assertTrue(result.isError());
        assertEquals("Error", result.getMessage());
    }

    @Test
    public void testOK() {
        CheckResult result = CheckResult.OK;
        assertFalse(result.isError());
        assertNull(result.getMessage());
    }
}
