/*

Copyright 2008-2019 TOPdesk, the Netherlands

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

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class CompilerErrorTest extends NoOutputTestBase {

    @Test
    public void testNoExtentionNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "NoExtentionTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("NoExtentionTestClass does not extend java.util.ArrayList", getMessage(diagnostic));
    }

    @Test
    public void testNoImplementationNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "NoImplementationTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("NoImplementationTestClass does not implement java.util.RandomAccess", getMessage(diagnostic));
    }

    @Test
    public void testNonStaticNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "NonStaticTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(8, diagnostic.getLineNumber());
        assertEquals("InnerNonStaticTestClass is not a static class", getMessage(diagnostic));
    }

    @Test
    public void testPackagePrivateNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "PackagePrivateTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("PackagePrivateTestClass is not a public class", getMessage(diagnostic));
    }

    @Test
    public void testEnumNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "EnumTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("EnumTestClass is not a class", getMessage(diagnostic));
    }

    @Test
    public void testInterfaceNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "InterfaceTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("InterfaceTestClass is not a class", getMessage(diagnostic));
    }

    @Test
    public void testNoPublicConstructorNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "NoPublicConstructorTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("NoPublicConstructorTestClass has no public no-args constructor", getMessage(diagnostic));
    }

    @Test
    public void testNoNoArgsConstructorNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "NoNoArgsConstructorTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("NoNoArgsConstructorTestClass has no public no-args constructor", getMessage(diagnostic));
    }

    @Test
    public void testAnnotationNotAllowed() throws IOException {
        List<Diagnostic<JavaFileObject>> list = compile(FileType.INVALID, "AnnotationTestClass");
        assertEquals(1, list.size());

        Diagnostic<JavaFileObject> diagnostic = list.get(0);
        assertEquals(Kind.ERROR, diagnostic.getKind());
        assertEquals(6, diagnostic.getLineNumber());
        assertEquals("AnnotationTestClass is not a class", getMessage(diagnostic));
    }

    private static String getMessage(Diagnostic<JavaFileObject> diagnostic) {
        String msg = diagnostic.getMessage(null);
        int first = msg.indexOf(':');
        int second = msg.indexOf(':', first + 1);
        return msg.substring(second + 1).trim();
    }

    private static List<Diagnostic<JavaFileObject>> compile(FileType type, String... fileNames) throws IOException {
        return TestDiagnosticListener.compile(new ServiceProviderProcessor(), type, fileNames);
    }
}
