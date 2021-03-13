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
package org.kordamp.jipsy.processor.type;

import org.kordamp.jipsy.annotations.TypeProviderFor;
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
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Andres Almiray
 */
@SupportedAnnotationTypes("*")
@SupportedOptions({Options.SPI_DIR_OPTION, Options.SPI_LOG_OPTION, Options.SPI_VERBOSE_OPTION, Options.SPI_DISABLED_OPTION})
public class TypeProviderProcessor extends AbstractSpiProcessor {
    public static final String NAME = TypeProviderProcessor.class.getName()
        + " (" + TypeProviderProcessor.class.getPackage().getImplementationVersion() + ")";


    private Persistence persistence;
    private TypeCollector data;

    @Override
    protected Persistence getPersistence() {
        return persistence;
    }

    @Override
    protected TypeCollector getData() {
        return data;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return TypeProviderFor.class;
    }

    @Override
    protected void initialize() {
        super.initialize();

        persistence = new TypePersistence(NAME, options.dir(), processingEnv.getFiler(), logger);
        data = new TypeCollector(persistence.getInitializer(), logger);

        initializeIfPossible(data, persistence);
    }

    protected Stream<AnnotationValue> enrich(Stream<? extends AnnotationMirror> poor) {
        return poor.map(this::findSingleValueMember);
    }

    private AnnotationValue findSingleValueMember(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElseThrow(() -> new IllegalStateException("No value found in element"));
    }

    /**
     * Check that the type element {@code currentClass} meets all the requirements that are needed to be a provider.
     * First of all it is a class, and it is public, and nothing else is checked in this case.
     *
     * @param currentClass the class to check
     * @return the result, which is {@linkplain CheckResult#OK} if there is no problem.
     */
    protected CheckResult checkCurrentClass(Element currentClass) {
        if (currentClass.getKind() != ElementKind.CLASS || currentClass.getKind() != ElementKind.INTERFACE) {
            return CheckResult.valueOf("is not a class nor an interface");
        }

        if (!currentClass.getModifiers().contains(Modifier.PUBLIC)) {
            return CheckResult.valueOf("is not public");
        }

        return CheckResult.OK;
    }
}