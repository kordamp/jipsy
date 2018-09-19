/*
 * Copyright 2010-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kordamp.jipsy.processor;

import org.kordamp.jipsy.processor.service.ServiceProviderProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Andres Almiray
 */
public abstract class AbstractSpiProcessor extends AbstractProcessor {
    protected static final Pattern RELEASE_PATTERN = Pattern.compile("^RELEASE_(\\d+)$");
    protected Options options;
    protected Logger logger;

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);

        try {
            initialize();
        } catch (Exception e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, ProcessorLogger.exceptionToString(e));
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (options.disabled()) {
            return false;
        }

        long start = System.currentTimeMillis();
        logger.note(LogLocation.LOG_FILE, "Starting round with " + roundEnv.getRootElements().size() + " elements");

        removeStaleData(roundEnv);

        handleAnnotations(roundEnv);

        long end = System.currentTimeMillis();
        logger.note(LogLocation.LOG_FILE, "Ending round in " + (end - start) + " milliseconds");
        if (roundEnv.processingOver()) {
            writeData();
        }
        return false;
    }

    protected abstract Class<? extends Annotation> getAnnotationClass();

    protected abstract void handleElement(Element e);

    protected abstract void removeStaleData(RoundEnvironment roundEnv);

    protected abstract void writeData();

    protected void warning(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    protected void initialize() {
        options = new Options(ServiceProviderProcessor.NAME, processingEnv.getOptions());
        if (options.disabled()) {
            return;
        }
        logger = new ProcessorLogger(processingEnv.getMessager(), options);

        checkCompatibility();
    }

    protected void checkCompatibility() {
        logger.note(LogLocation.MESSAGER, "Testing for compatibility options");
        try {
            checkJavacOnLinux();
        } catch (Exception e) {
            warning(ProcessorLogger.exceptionToString(e));
        }
        logger.note(LogLocation.MESSAGER, "Testing complete");
    }

    protected void checkJavacOnLinux() {
        try {
            FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", options.dir() + "a/b");
            if (resource.toUri().toString().equals("b")) {
                warning("Output files will be placed in the root of the output folder.\n  This is a known bug in the java compiler on Linux.\n  Please use the -d compiler option to circumvent this problem.\n  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6647996 for more information.");
            }
        } catch (IOException e) {
            warning("IOException during testing Javac on Linux");
        }
    }

    protected void handleAnnotations(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(getAnnotationClass());
        for (Element e : elements) {
            handleElement(e);
        }
    }

    protected void reportError(TypeElement element, CheckResult result) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element.getSimpleName() + " " + result.getMessage(), element);
    }

    protected boolean hasPublicNoArgsConstructor(TypeElement currentClass) {
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(currentClass.getEnclosedElements());
        for (ExecutableElement constructor : constructors) {
            if (hasModifier(constructor, Modifier.PUBLIC) && constructor.getParameters().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasModifier(Element element, Modifier modifier) {
        return element.getModifiers().contains(modifier);
    }

    protected boolean isStaticClass(TypeElement element) {
        return element.getEnclosingElement().getKind() != ElementKind.CLASS ||
            hasModifier(element, Modifier.STATIC);
    }

    protected boolean isAbstractClass(TypeElement element) {
        return element.getEnclosingElement().getKind() == ElementKind.CLASS &&
            hasModifier(element, Modifier.ABSTRACT);
    }

    protected CheckResult isImplementation(TypeElement currentClass, TypeElement type) {
        if (isAssignable(currentClass.asType(), type.asType())) {
            return CheckResult.OK;
        }

        String message;
        if (type.getKind() == ElementKind.INTERFACE) {
            message = "does not implement";
        } else {
            message = "does not extend";
        }
        return CheckResult.valueOf(message + " " + type.getQualifiedName());
    }

    protected boolean isAssignable(TypeMirror currentClass, TypeMirror type) {
        Types typeUtils = processingEnv.getTypeUtils();
        if (typeUtils.isAssignable(typeUtils.erasure(currentClass), typeUtils.erasure(type))) {
            return true;
        }

        for (TypeMirror superType : typeUtils.directSupertypes(currentClass)) {
            if (isAssignable(superType, type)) {
                return true;
            }
        }
        return false;
    }

    protected AnnotationValue findSingleValueMember(AnnotationMirror mirror, String memberName) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(memberName)) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("No value found in element");
    }

    @SuppressWarnings("unchecked")
    protected Collection<AnnotationValue> findCollectionValueMember(AnnotationMirror mirror, String memberName) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(memberName)) {
                return (Collection<AnnotationValue>) entry.getValue().getValue();
            }
        }
        throw new IllegalStateException("No value found in element");
    }

    protected String createProperQualifiedName(TypeElement type) {
        return processingEnv.getElementUtils().getBinaryName(type).toString();
    }

    protected static List<AnnotationMirror> findAnnotationMirrors(TypeElement element, String lookingFor) {
        List<AnnotationMirror> annotationMirrors = new ArrayList<AnnotationMirror>();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotationMirrorMatches(annotation, lookingFor)) {
                annotationMirrors.add(annotation);
            }
        }
        return annotationMirrors;
    }

    protected static boolean annotationMirrorMatches(AnnotationMirror annotation, String lookingFor) {
        Name qualifiedName = ((TypeElement) (annotation.getAnnotationType()).asElement()).getQualifiedName();
        return qualifiedName.contentEquals(lookingFor);
    }

    protected static TypeElement toElement(AnnotationValue value) {
        return (TypeElement) ((DeclaredType) ((TypeMirror) value.getValue())).asElement();
    }
}
