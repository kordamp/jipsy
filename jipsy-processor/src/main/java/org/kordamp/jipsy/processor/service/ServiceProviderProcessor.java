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

package org.kordamp.jipsy.processor.service;

import org.kordamp.jipsy.annotations.ServiceProviderFor;
import org.kordamp.jipsy.processor.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@SupportedAnnotationTypes("*")
@SupportedOptions({Options.SPI_DIR_OPTION, Options.SPI_LOG_OPTION, Options.SPI_VERBOSE_OPTION, Options.SPI_DISABLED_OPTION})
public class ServiceProviderProcessor extends AbstractSpiProcessor {
    public static final String NAME = ServiceProviderProcessor.class.getName()
        + " (" + ServiceProviderProcessor.class.getPackage().getImplementationVersion() + ")";

    private static final int MAX_SUPPORTED_VERSION = 8;

    private Persistence persistence;
    private ServiceCollector data;

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ServiceProviderFor.class;
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
    protected void initialize() {
        super.initialize();

        persistence = new ServicePersistence(NAME, options.dir(), processingEnv.getFiler(), logger);
        data = new ServiceCollector(persistence.getInitializer(), logger);

        // Initialize if possible
        for (String serviceName : persistence.tryFind()) {
            data.getService(serviceName);
        }
        data.cache();
    }

    @Override
    protected void writeData() {
        if (data.isModified()) {
            if (data.services().isEmpty()) {
                logger.note(LogLocation.LOG_FILE, "Writing output");
                try {
                    persistence.delete();
                } catch (IOException e) {
                    logger.warning(LogLocation.LOG_FILE, "An error occurred while deleting data file");
                }
            } else {
                logger.note(LogLocation.LOG_FILE, "Writing output");
                for (Service service : data.services()) {
                    try {
                        persistence.write(service.getName(), service.toProviderNamesList());
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
                    }
                }
                persistence.writeLog();
            }
        }
    }

    @Override
    protected void removeStaleData(RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getRootElements()) {
            if (e instanceof TypeElement) {
                TypeElement currentClass = (TypeElement) e;
                data.removeProvider(createProperQualifiedName(currentClass));
            }
        }
    }

    @Override
    protected void handleElement(Element e) {
        if (!(e instanceof TypeElement)) {
            return;
        }

        TypeElement currentClass = (TypeElement) e;

        CheckResult checkResult = checkCurrentClass(currentClass);
        if (checkResult.isError()) {
            reportError(currentClass, checkResult);
            return;
        }

        for (TypeElement service : findServices(currentClass)) {
            CheckResult implementationResult = isImplementation(currentClass, service);
            if (implementationResult.isError()) {
                reportError(currentClass, implementationResult);
            } else {
                register(createProperQualifiedName(service), currentClass);
            }
        }
    }

    /**
     * Check that the type element {@code currentClass} meets all the requirements that are needed to be a provider.
     * First of all it is a class, and it is public, not a non-static inner class, has public no-arg constructor and it
     * is not abstract.
     *
     * @param currentClass the class to check
     * @return the result, which is {@linkplain CheckResult#OK} if there is no problem.
     */
    private CheckResult checkCurrentClass(TypeElement currentClass) {
        if (currentClass.getKind() != ElementKind.CLASS) {
            return CheckResult.valueOf("is not a class");
        }

        if (!hasModifier(currentClass, Modifier.PUBLIC)) {
            return CheckResult.valueOf("is not a public class");
        }

        if (!isStaticClass(currentClass)) {
            return CheckResult.valueOf("is not a static class");
        }

        if (!hasPublicNoArgsConstructor(currentClass)) {
            return CheckResult.valueOf("has no public no-args constructor");
        }

        return CheckResult.OK;
    }

    private List<TypeElement> findServices(TypeElement classElement) {
        List<TypeElement> services = new ArrayList<TypeElement>();

        for (AnnotationMirror annotation : findAnnotationMirrors(classElement, getAnnotationClass().getName())) {
            for (AnnotationValue value : findCollectionValueMember(annotation, "value")) {
                services.add(toElement(value));
            }
        }

        return services;
    }

    private void register(String serviceName, TypeElement provider) {
        data.getService(serviceName).addProvider(createProperQualifiedName(provider));
    }
}