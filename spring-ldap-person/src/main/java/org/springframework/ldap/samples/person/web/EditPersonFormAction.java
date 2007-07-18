/*
 * Copyright 2005-2007 the original author or authors.
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

package org.springframework.ldap.samples.person.web;

import net.sf.chainedoptions.ChainedOptionManager;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.Assert;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action implementation that handles edit of a person. Registers appropriate
 * property editors, and provides an action method for setting up reference
 * data.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class EditPersonFormAction extends FormAction {

    private ChainedOptionManager chainedOptionManager;

    /*
     * @see org.springframework.webflow.action.FormAction#registerPropertyEditors(org.springframework.webflow.execution.RequestContext,
     *      org.springframework.beans.PropertyEditorRegistry)
     */
    protected void registerPropertyEditors(RequestContext context,
            PropertyEditorRegistry registry) {
        registry.registerCustomEditor(String[].class,
                new StringArrayPropertyEditor());
    }

    public Event referenceData(RequestContext context) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing referenceData");
        }

        Object formObject = getFormObject(context);
        chainedOptionManager.referenceData(context.getRequestScope().asMap(),
                formObject, null);
        return success();
    }

    protected void initAction() {
        Assert.notNull(chainedOptionManager,
                "The property 'chainedOptionManager' must not be null");
    }

    public void setChainedOptionManager(
            ChainedOptionManager chainedOptionManager) {
        this.chainedOptionManager = chainedOptionManager;
    }
}
