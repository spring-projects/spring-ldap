/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.repository.support;

import com.querydsl.apt.DefaultConfiguration;
import org.springframework.ldap.odm.annotations.Id;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 * @since 2.0
 */
class DefaultLdapAnnotationProcessorConfiguration extends DefaultConfiguration {
    public DefaultLdapAnnotationProcessorConfiguration(
            RoundEnvironment roundEnv,
            Map<String, String> options,
            Collection<String> keywords,
            Class<? extends Annotation> entitiesAnn,
            Class<? extends Annotation> entityAnn,
            Class<? extends Annotation> superTypeAnn,
            Class<? extends Annotation> embeddableAnn,
            Class<? extends Annotation> embeddedAnn,
            Class<? extends Annotation> skipAnn) {

        super(roundEnv, options, keywords, entitiesAnn, entityAnn, superTypeAnn, embeddableAnn, embeddedAnn, skipAnn);
    }

    @Override
    public boolean isBlockedField(VariableElement field) {
        return super.isBlockedField(field) || field.getAnnotation(Id.class) != null;
    }

    @Override
    public boolean isValidField(VariableElement field) {
        return super.isValidField(field) && field.getAnnotation(Id.class) == null;
    }
}
