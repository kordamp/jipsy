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

import org.kordamp.jipsy.processor.service.ServiceProviderProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Andres Almiray
 */
public abstract class AbstractSpiProcessor extends AbstractProcessor {
    protected static final Pattern RELEASE_PATTERN = Pattern.compile("^RELEASE_(\\d+)$");
    protected Options options;
    protected Logger logger;
    private static final int MAX_SUPPORTED_VERSION = 8;

    protected abstract Persistence getPersistence();

    protected abstract <T extends ProvidedCollector> T getData();

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);

        try {
            initialize();
        } catch (Exception e) {
            environment.getMessager().printMessage(Kind.ERROR, ProcessorLogger.exceptionToString(e));
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion[] svs = SourceVersion.values();
        for (int i = svs.length - 1; i >= 0; i--) {
            String name = svs[i].name();
            Matcher m = RELEASE_PATTERN.matcher(name);
            if (m.matches()) {
                int release = Integer.parseInt(m.group(1));
                if (release <= MAX_SUPPORTED_VERSION) return svs[i];
            }
        }

        return SourceVersion.RELEASE_6;
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

    protected abstract Stream<AnnotationValue> enrich(Stream<? extends AnnotationMirror> poor);

    protected abstract CheckResult checkCurrentClass(Element currentClass);

    /**
     * Register the provider implementing the interface {@code name} into the collection of services and their
     * implementations.
     *
     * @param name     the name of the service this provider implements
     * @param provider the provider that implements the interface
     */

    private void register(String name, TypeElement provider) {
        getData().get(name).addProvider(createProperQualifiedName(provider));
    }

    protected void handleElement(Element e) {
        CheckResult checkResult = checkCurrentClass(e);
        if (checkResult.isError()) {
            reportError(e, checkResult);
            return;
        }
        TypeElement te = (TypeElement) e;

        enrich(findAnnotationMirrors(te, getAnnotationClass().getName()))
            .map(AbstractSpiProcessor::toElement)
            .filter(type -> {
                    CheckResult implementationResult = isImplementation(te, type);
                    return !(implementationResult.isError() && reportError(te, implementationResult));
                }
            )
            .map(this::createProperQualifiedName)
            .forEach(name -> register(name, te));
    }

    private void removeStaleData(RoundEnvironment roundEnv) {
        roundEnv.getRootElements().stream()
            .filter(e -> e instanceof TypeElement)
            .map(e -> (TypeElement) e)
            .map(this::createProperQualifiedName)
            .forEach(getData()::removeProvider);
    }

    private void writeData() {
        if (getData().isModified()) {
            logger.note(LogLocation.LOG_FILE, "Writing output");
            if (getData().values().isEmpty()) {
                try {
                    getPersistence().delete();
                } catch (IOException e) {
                    logger.warning(LogLocation.LOG_FILE, "An error occurred while deleting data file");
                }
            } else {
                for (Provided type : getData().values()) {
                    try {
                        getPersistence().write(type.getName(), type.toProviderNamesList());
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
                    }
                }
                getPersistence().writeLog();
            }
        }
    }

    protected void initializeIfPossible(ProvidedCollector data, Persistence persistence) {
        persistence.tryFind().forEach(data::get);
        data.cache();
    }

    /**
     * Send a non-mandatory warning message to the service output-
     *
     * @param message the warning message to be printed
     */
    private void reportWarning(String message) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message);
    }

    /**
     * Send a mandatory warning message to the service output-
     *
     * @param message the warning message to be printed
     */
    private void reportMandatoryWarning(String message) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message);
    }

    /**
     * Report an error on a given element.
     *
     * @param element
     * @param result
     * @return {@code true}
     */
    protected boolean reportError(Element element, CheckResult result) {
        processingEnv.getMessager().printMessage(Kind.ERROR, element.getSimpleName() + " " + result.getMessage(), element);
        return true;
    }

    /**
     * General implementation of the initialization of the processor. It reads the options given to the processor from
     * the invoking tool. If the option  {@code spi_disabled} option is set and {@code true} then the method returns
     * without further processing.
     * <p>
     * If the processor is not disabled then it checks the the existence of some Javac annotation processing bug that
     * existed in certain version. If the annotation processor sees this type of behavior then it prints a warning and
     * continues.
     */
    protected void initialize() {
        options = new Options(ServiceProviderProcessor.NAME, processingEnv.getOptions());
        if (options.disabled()) {
            return;
        }
        logger = new ProcessorLogger(processingEnv.getMessager(), options);

        checkCompatibility();
    }

    /**
     * Perform some compatibility checks and print some warning in case there is some issue.
     */
    private void checkCompatibility() {
        logger.note(LogLocation.MESSAGER, "Testing for compatibility options");
        try {
            checkJavacOnLinux();
        } catch (Exception e) {
            reportWarning(ProcessorLogger.exceptionToString(e));
        }
        logger.note(LogLocation.MESSAGER, "Testing complete");
    }

    /**
     * Check if the bug 6647996 of Javac seem to manifest in this execution. If yes, it displays a warning about it and
     * how to fix.
     * <p>
     * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6647996 for further information.
     * <p>
     * If there is an exception during the test, then it is also printed.
     */
    private void checkJavacOnLinux() {
        try {
            FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", options.dir() + "a/b");
            if (resource.toUri().toString().equals("b")) {
                reportMandatoryWarning("Output files will be placed in the root of the output folder.\n" +
                    "  This is a known bug in the java compiler on Linux.\n" +
                    "  Please use the -d compiler option to circumvent this problem.\n" +
                    "  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6647996 for more information.");
            }
        } catch (IOException e) {
            reportWarning("IOException during testing Javac on Linux");
        }
    }

    /**
     * Get all the elements, which are annotated by the type returned by {@link #getAnnotationClass()} implemented in
     * the subclass of this abstract class and execute the {@link #handleElement(Element)} for them.
     *
     * @param roundEnv the execution processor environment for the actual round
     */
    private void handleAnnotations(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(getAnnotationClass()).stream().forEach(this::handleElement);
    }

    protected boolean hasModifier(Element element, Modifier modifier) {
        return element.getModifiers().contains(modifier);
    }

    /**
     * Checks that the class is either top level or a static inner class. Non-static inner classes cannot implement
     * services, because services need a public no-arg constructor. Non-static inner classes, however, has an implicit
     * reference to the outer class instance they belong to and they cannot be instantiated without that instance. This
     * is implemented in the JVM in a way that such classes have an extra first argument, which is a reference to the
     * enclosing class instance. That way a non-static inner class does not have a real no-arg constructor, because even
     * the no-arg constructor has a hidden argument.
     *
     * @param element the (class) element to check
     * @return true if the class is top level, or static inner class
     */
    protected boolean isStaticClass(Element element) {
        return element.getEnclosingElement().getKind() != ElementKind.CLASS ||
            hasModifier(element, Modifier.STATIC);
    }

    /**
     * Checks that a class is abstract.
     *
     * @param element the class that we need to check for being abstract
     * @return {@code true} if the class is an abstract class
     */
    protected boolean isAbstractClass(Element element) {
        return hasModifier(element, Modifier.ABSTRACT);
    }

    /**
     * Checks that the class is really a service class that implements the interface declaring the service.
     *
     * @param currentClass the class that needs to be checked that it really implements the interface defining the
     *                     service
     * @param type         the service interface
     * @return {@linkplain CheckResult#OK} if the class implements the interface otherwise an error result
     */
    protected CheckResult isImplementation(TypeElement currentClass, TypeElement type) {
        if (isAssignable(currentClass.asType(), type.asType())) {
            return CheckResult.OK;
        }

        final String message = "does not " + (type.getKind() == ElementKind.INTERFACE ? "implement" : "extend");
        return CheckResult.valueOf(message + " " + type.getQualifiedName());
    }

    /**
     * Checks that the class is really a service class that implements the interface declaring the service.
     *
     * @param currentClass the class that needs to be checked that it really implements the interface defining the
     *                     service
     * @param type         the service interface type
     * @return {@code true} if the class implements the interface otherwise {@code false}
     */
    private boolean isAssignable(TypeMirror currentClass, TypeMirror type) {
        Types typeUtils = processingEnv.getTypeUtils();
        return typeUtils.isAssignable(typeUtils.erasure(currentClass), typeUtils.erasure(type)) ||
            typeUtils.directSupertypes(currentClass).stream().anyMatch(superType -> isAssignable(superType, type));
    }

    /**
     * Get the name of the type. This is the fully qualified name and it is the binary name. That is the name where the
     * character between the name of the enclosing and the subclass is {@code $} and not {@code .}
     * <p>
     * This is the name, which is listed in the {@code META-INF/services} files.
     *
     * @param type for which we need the name
     * @return the binary name of the type / class
     */
    protected String createProperQualifiedName(TypeElement type) {
        return processingEnv.getElementUtils().getBinaryName(type).toString();
    }

    protected static Stream<? extends AnnotationMirror> findAnnotationMirrors(TypeElement element, String lookingFor) {
        return element.getAnnotationMirrors().stream()
            .filter(annotation -> annotationMirrorMatches(annotation, lookingFor));
    }

    /**
     * @param annotation the annotation of which the name to check
     * @param lookingFor the name we are looking for
     * @return {@code true} if the {@code annotation} has the name we are {@code lookingFor}.
     */
    protected static boolean annotationMirrorMatches(AnnotationMirror annotation, String lookingFor) {
        Name qualifiedName = ((TypeElement) (annotation.getAnnotationType()).asElement()).getQualifiedName();
        return qualifiedName.contentEquals(lookingFor);
    }

    protected static TypeElement toElement(AnnotationValue value) {
        return (TypeElement) ((DeclaredType) (value.getValue())).asElement();
    }
}
