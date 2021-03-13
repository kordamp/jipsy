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
import org.kordamp.jipsy.processor.AbstractSpiProcessor;
import org.kordamp.jipsy.processor.CheckResult;
import org.kordamp.jipsy.processor.Options;
import org.kordamp.jipsy.processor.Persistence;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

@SupportedAnnotationTypes("*")
@SupportedOptions({Options.SPI_DIR_OPTION, Options.SPI_LOG_OPTION, Options.SPI_VERBOSE_OPTION, Options.SPI_DISABLED_OPTION})
public class ServiceProviderProcessor extends AbstractSpiProcessor {
    public static final String NAME = ServiceProviderProcessor.class.getName()
        + " (" + ServiceProviderProcessor.class.getPackage().getImplementationVersion() + ")";

    private Persistence persistence;
    private ServiceCollector data;

    @Override
    protected ServiceCollector getData() {
        return data;
    }

    @Override
    protected Persistence getPersistence() {
        return persistence;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ServiceProviderFor.class;
    }

    @Override
    protected void initialize() {
        super.initialize();

        persistence = new ServicePersistence(NAME, options.dir(), processingEnv.getFiler(), logger);
        data = new ServiceCollector(persistence.getInitializer(), logger);

        initializeIfPossible(data, persistence);
    }

    protected Stream<AnnotationValue> enrich(Stream<? extends AnnotationMirror> poor) {
        return poor.flatMap(mirror -> findCollectionValueMember(mirror).stream());
    }

    @SuppressWarnings("unchecked")
    protected Collection<AnnotationValue> findCollectionValueMember(AnnotationMirror mirror) {
        return
            mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
                .findFirst()
                .map(entry -> (Collection<AnnotationValue>) entry.getValue().getValue())
                .orElseThrow(() -> new IllegalStateException("No value found in element"));
    }

    /**
     * Check that the type element {@code currentClass} meets all the requirements that are needed to be a provider.
     * First of all it is a class, and it is public, not a non-static inner class, has public no-arg constructor and it
     * is not abstract.
     *
     * @param currentClass the class to check
     * @return the result, which is {@linkplain CheckResult#OK} if there is no problem.
     */
    protected CheckResult checkCurrentClass(Element currentClass) {
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

        if (isAbstractClass(currentClass)) {
            return CheckResult.valueOf("is abstract");
        }
        return CheckResult.OK;
    }

    /**
     * @param currentClass the class that we check for the appropriate constructor
     * @return {@code true} if the class has {@code public} no-arg constructor
     */
    private boolean hasPublicNoArgsConstructor(Element currentClass) {
        return ElementFilter.constructorsIn(currentClass.getEnclosedElements()).stream().anyMatch(
            constructor -> hasModifier(constructor, Modifier.PUBLIC) && constructor.getParameters().isEmpty());
    }
}