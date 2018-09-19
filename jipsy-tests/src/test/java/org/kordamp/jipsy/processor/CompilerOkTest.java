/*

Copyright 2008-2018 TOPdesk, the Netherlands

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
import org.kordamp.jipsy.processor.service.ServiceProviderProcessor;
import org.kordamp.jipsy.processor.testutils.FileType;
import org.kordamp.jipsy.processor.testutils.NoOutputTestBase;
import org.kordamp.jipsy.processor.testutils.TestDiagnosticListener;
import org.kordamp.jipsy.processor.testutils.TestOutput;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class CompilerOkTest extends NoOutputTestBase {

    @Test
    public void testGenerics() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "GenericsTestClass");
        TestOutput.out().bypass().println(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSimple() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "SimpleTestClass");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testMultipleInterfaces() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "MultipleInterfacesTestClass");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSuperClassImplementsInterface() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "SuperClass", "SubTestClass");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testStaticInnerClass() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "StaticInnerTestClass");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testProvidesClass() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.VALID, "ProvidesClassTestClass");
        assertTrue(list.isEmpty());
    }

    private static List<Diagnostic<JavaFileObject>> compile(FileType type, String... fileNames) throws IOException {
        return TestDiagnosticListener.compile(new ServiceProviderProcessor(), type, fileNames);
    }
}
