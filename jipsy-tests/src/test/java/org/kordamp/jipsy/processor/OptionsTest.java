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
import org.kordamp.jipsy.processor.testutils.OutputDir;
import org.kordamp.jipsy.processor.testutils.TestJavaFileObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.ToolProvider;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class OptionsTest extends NoOutputTestBase {

    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    @Test
    public void testDisabledDefault() {
        Options options = getOptions("-Aspi_disabled");
        assertTrue(options.disabled());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report("", null, null, null), options.report());
    }

    @Test
    public void testDisabledFalse() {
        Options options = getOptions("-Aspi_disabled=false");
        assertFalse(options.disabled());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report("false", null, null, null), options.report());
    }

    @Test
    public void testDisabledTrue() {
        Options options = getOptions("-Aspi_disabled=true");
        assertTrue(options.disabled());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report("true", null, null, null), options.report());
    }

    @Test
    public void testDisabledTrueCaseInsensitive() {
        Options options = getOptions("-Aspi_disabled=TrUe");
        assertTrue(options.disabled());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report("TrUe", null, null, null), options.report());
    }

    @Test
    public void testDisabledSomeValue() {
        Options options = getOptions("-Aspi_disabled=somevalue");
        assertFalse(options.disabled());
        Collection<String> warnings = options.getWarnings();
        assertEquals(1, warnings.size());
        assertEquals("Unrecognized value for parameter 'spi_disabled'. Found 'somevalue'.  Legal values: 'true', 'false'.", warnings.iterator().next());
        assertEquals(report("somevalue", null, null, null), options.report());
    }

    @Test
    public void testVerboseTrue() {
        Options options = getOptions("-Aspi_verbose");
        assertFalse(options.disabled());
        assertTrue(options.verbose());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, "", null, null), options.report());
    }

    @Test
    public void testLoggingTrue() {
        Options options = getOptions("-Aspi_log");
        assertTrue(options.logging());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, "", null), options.report());
    }

    @Test
    public void testLoggingAndVerboseTrue() {
        Options options = getOptions("-Aspi_log", "-Aspi_verbose");
        assertTrue(options.logging());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, "", "", null), options.report());
    }

    @Test
    public void testDirNotEmpty() {
        Options options = getOptions("-Aspi_dir=temp");
        assertEquals("temp/", options.dir());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, null, "temp"), options.report());
    }

    @Test
    public void testDirNotEmptyTrailingSlash() {
        Options options = getOptions("-Aspi_dir=temp/");
        assertEquals("temp/", options.dir());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, null, "temp/"), options.report());
    }

    @Test
    public void testDirNotEmptyTrailingBackSlash() {
        Options options = getOptions("-Aspi_dir=temp\\");
        assertEquals("temp/", options.dir());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, null, "temp\\"), options.report());
    }

    @Test
    public void testDirNotEmptyContainingBackSlash() {
        Options options = getOptions("-Aspi_dir=temp\\temp");
        assertEquals("temp/temp/", options.dir());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, null, "temp\\temp"), options.report());
    }

    @Test
    public void testDirEmpty() {
        Options options = getOptions("-Aspi_dir");
        assertEquals("", options.dir());
        assertTrue(options.getWarnings().isEmpty());
        assertEquals(report(null, null, null, ""), options.report());
    }

    @Test
    public void testDirMissing() {
        Options options = getOptions("-Aspi_disabled");
        assertEquals("", options.dir());
        assertTrue(options.getWarnings().isEmpty());
    }

    @Test
    public void testOptionsLogging() {
        String expected = report(null, null, "", null);

        assertEquals(expected, getOptions("-Aspi_log").report());
    }

    private String report(String disabled, String verbose, String log, String dir) {
        StringBuilder message = new StringBuilder();
        message
            .append("Initializing Annotation Processor ").append("MyProcessor").append("\n")
            .append("Used options:\n");

        appendValue(message, "spi_disabled", disabled);
        appendValue(message, "spi_verbose", verbose);
        appendValue(message, "spi_log", log);
        appendValue(message, "spi_dir", dir);
        return message.toString();
    }

    private void appendValue(StringBuilder message, String name, String value) {
        message.append(" - ").append(name).append(": ");
        if (value == null) {
            message.append("missing");
        } else {
            message.append("'").append(value).append("'");
        }
        message.append("\n");
    }

    private Options getOptions(String... parameters) {
        List<String> compilerOptions = new ArrayList<String>();
        compilerOptions.addAll(Arrays.asList(parameters));
        compilerOptions.addAll(OutputDir.getOptions());
        CompilationTask task = compiler.getTask(null, null, null, compilerOptions, null, TestJavaFileObject.ONLY_HELLO_WORLD);
        TestProcessor processor = new TestProcessor();
        task.setProcessors(Collections.singleton(processor));
        task.call();
        return processor.getOptions();
    }


    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_6)
    @SupportedOptions({Options.SPI_DIR_OPTION, Options.SPI_LOG_OPTION, Options.SPI_VERBOSE_OPTION, Options.SPI_DISABLED_OPTION})
    public static class TestProcessor extends AbstractProcessor {

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latestSupported();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            return false;
        }

        Options getOptions() {
            return new Options("MyProcessor", processingEnv.getOptions());
        }
    }
}
