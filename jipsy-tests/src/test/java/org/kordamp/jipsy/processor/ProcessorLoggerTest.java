/*

Copyright 2008-2014 TOPdesk, the Netherlands

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
import org.kordamp.jipsy.processor.testutils.TestMessager;
import org.kordamp.jipsy.processor.testutils.TestMessager.Message;

import javax.tools.Diagnostic.Kind;
import java.io.PrintWriter;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ProcessorLoggerTest extends NoOutputTestBase {
    private static final String TEST_MESSAGE = "%%test message%%";

    private final TestMessager messager = new TestMessager();
    private final Options optionsVerbose = new Options(null, Collections.singletonMap(Options.SPI_VERBOSE_OPTION, (String) null));
    private final Options optionsLog = new Options(null, Collections.singletonMap(Options.SPI_LOG_OPTION, (String) null));
    private final Options options = new Options(null, new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(Options.SPI_LOG_OPTION, (String) null);
            put(Options.SPI_VERBOSE_OPTION, (String) null);
        }
    });


    @Test(expected = NullPointerException.class)
    public void testProcessLoggerMessagerNull() {
        new ProcessorLogger(null, optionsVerbose);
    }

    @Test(expected = NullPointerException.class)
    public void testProcessLoggerOptionsNull() {
        new ProcessorLogger(messager, null);
    }

    @Test
    public void testProcessLoggerOptionsNoWarnings() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        assertEquals("", logger.getFileContent());
        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals(options.report(), messages.get(0).msg);
    }

    @Test
    public void testProcessLoggerOptionsWithWarnings() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Options.SPI_LOG_OPTION, "true");
        map.put(Options.SPI_VERBOSE_OPTION, "true");
        map.put(Options.SPI_DISABLED_OPTION, "yes");
        Options o = new Options(null, map);

        ProcessorLogger logger = new ProcessorLogger(messager, o);

        List<Message> messages = messager.messages();
        List<String> warnings = new ArrayList<String>(o.getWarnings());
        assertEquals(o.report(), messages.get(0).msg);
        assertEquals(warnings.size() + 1, messages.size());

        StringBuilder expectedFileContent = new StringBuilder();
        for (int i = 0; i < warnings.size(); i++) {
            String warning = warnings.get(i);
            assertEquals(warning, messages.get(i + 1).msg);
            expectedFileContent.append("warning: ").append(warning).append("\n");
        }

        assertEquals(expectedFileContent.toString(), logger.getFileContent());
    }


    @Test(expected = NullPointerException.class)
    public void testNoteLocationNull() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        logger.note(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testNoteMessageNull() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        logger.note(LogLocation.BOTH, null);
    }

    @Test
    public void testNoteToMessagerNotVerbose() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsLog);
        messager.reset();
        logger.note(LogLocation.MESSAGER, "message");
        assertTrue(messager.messages().isEmpty());
    }

    @Test
    public void testNoteToMessagerVerbose() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsVerbose);
        messager.reset();
        logger.note(LogLocation.MESSAGER, "message");
        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals("message", messages.get(0).msg);
        assertEquals(Kind.NOTE, messages.get(0).kind);
    }

    @Test
    public void testNoteToFileNoLogging() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsVerbose);
        logger.note(LogLocation.LOG_FILE, "message");
        assertEquals("", logger.getFileContent());
    }

    @Test
    public void testNoteToFileLogging() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsLog);
        logger.note(LogLocation.LOG_FILE, "message");
        assertEquals("message\n", logger.getFileContent());
    }

    @Test
    public void testNoteToAll() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        messager.reset();
        logger.note(LogLocation.BOTH, "message");
        assertEquals("message\n", logger.getFileContent());

        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals("message", messages.get(0).msg);
        assertEquals(Kind.NOTE, messages.get(0).kind);
    }


    @Test(expected = NullPointerException.class)
    public void testWarningLocationNull() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        logger.warning(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testWarningMessageNull() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        logger.warning(LogLocation.BOTH, null);
    }

    @Test
    public void testWarningToMessagerNotVerbose() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsLog);
        messager.reset();
        logger.warning(LogLocation.MESSAGER, "message");
        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals("message", messages.get(0).msg);
        assertEquals(Kind.WARNING, messages.get(0).kind);
    }

    @Test
    public void testWarningToMessagerVerbose() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsVerbose);
        messager.reset();
        logger.warning(LogLocation.MESSAGER, "message");
        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals("message", messages.get(0).msg);
        assertEquals(Kind.WARNING, messages.get(0).kind);
    }

    @Test
    public void testWarningToFileNoLogging() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsVerbose);
        logger.warning(LogLocation.LOG_FILE, "message");
        assertEquals("", logger.getFileContent());
    }

    @Test
    public void testWarningToFileLogging() {
        ProcessorLogger logger = new ProcessorLogger(messager, optionsLog);
        logger.warning(LogLocation.LOG_FILE, "message");
        assertEquals("warning: message\n", logger.getFileContent());
    }

    @Test
    public void testWarningToAll() {
        ProcessorLogger logger = new ProcessorLogger(messager, options);
        messager.reset();
        logger.warning(LogLocation.BOTH, "message");
        assertEquals("warning: message\n", logger.getFileContent());

        List<Message> messages = messager.messages();
        assertEquals(1, messages.size());
        assertEquals("message", messages.get(0).msg);
        assertEquals(Kind.WARNING, messages.get(0).kind);
    }

    @Test(expected = NullPointerException.class)
    public void testNullExceptionToString() {
        ProcessorLogger.exceptionToString(null);
    }

    @Test
    public void testExceptionToString() {
        String result = ProcessorLogger.exceptionToString(new DummyException());
        assertEquals(TEST_MESSAGE, result);
    }

    private static class DummyException extends Exception {
        private static final long serialVersionUID = 1L;

        @Override
        public void printStackTrace(PrintWriter s) {
            s.append(TEST_MESSAGE);
        }
    }
}
