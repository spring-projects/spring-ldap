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

import java.util.HashMap;

import net.sf.chainedoptions.ChainedOptionManager;

import org.easymock.MockControl;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class DetailFlowExecutionTest extends AbstractXmlFlowExecutionTests {

    private MockControl personServiceControl;

    private PersonService personServiceMock;

    private MockControl chainedOptionManagerControl;

    private ChainedOptionManager chainedOptionManagerMock;

    private MutableAttributeMap attributeMap;

    protected void setUp() throws Exception {
        super.setUp();

        personServiceControl = MockControl.createControl(PersonService.class);
        personServiceMock = (PersonService) personServiceControl.getMock();

        chainedOptionManagerControl = MockControl.createControl(ChainedOptionManager.class);
        chainedOptionManagerMock = (ChainedOptionManager) chainedOptionManagerControl.getMock();

        attributeMap = new LocalAttributeMap();
        attributeMap.put("country", "Sweden");
        attributeMap.put("company", "company1");
        attributeMap.put("name", "Keith Donald");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        personServiceControl = null;
        personServiceMock = null;
        chainedOptionManagerControl = null;
        chainedOptionManagerMock = null;
    }

    protected void replay() {
        personServiceControl.replay();
        chainedOptionManagerControl.replay();
    }

    protected void verify() {
        personServiceControl.verify();
        chainedOptionManagerControl.verify();
    }

    public void testStartFlow() {
        expectFindByPrimaryKey();
        replay();

        ApplicationView view = applicationView(startFlow(attributeMap));

        verify();
        assertCurrentStateEquals("displayDetails");
        assertViewNameEquals("showDetails", view);
        assertModelAttributeNotNull("person", view);
    }

    public void testBack() {
        expectFindByPrimaryKey();
        replay();

        startFlow(attributeMap);
        signalEvent("back");

        verify();
        assertFlowExecutionEnded();
    }

    public void testEdit() {
        expectFindByPrimaryKey();
        expectReferenceData();
        replay();

        startFlow(attributeMap);
        ApplicationView view = applicationView(signalEvent("edit"));

        verify();
        assertCurrentStateEquals("editDetails");
        assertViewNameEquals("editDetails", view);
        assertModelAttributeNotNull("person", view);
    }

    public void testCancel() {
        expectFindByPrimaryKey();
        expectReferenceData();
        replay();

        startFlow(attributeMap);
        signalEvent("edit");
        ApplicationView view = applicationView(signalEvent("cancel"));

        verify();
        assertCurrentStateEquals("displayDetails");
        assertViewNameEquals("showDetails", view);
        assertModelAttributeNotNull("person", view);
    }

    public void testSubmit() {
        expectFindByPrimaryKey();
        expectReferenceData();
        expectUpdate();
        replay();

        startFlow(attributeMap);
        signalEvent("edit");
        ApplicationView view = applicationView(signalEvent("submit"));

        verify();
        assertCurrentStateEquals("displayDetails");
        assertViewNameEquals("showDetails", view);
        assertModelAttributeNotNull("person", view);
    }

    private void expectReferenceData() {
        Person person = setupPerson();
        HashMap map = new HashMap();
        chainedOptionManagerMock.referenceData(map, person, null);
    }

    private void expectFindByPrimaryKey() {
        expectFindByPrimaryKey(1);
    }

    private void expectFindByPrimaryKey(int times) {
        Person person = setupPerson();

        personServiceControl.expectAndReturn(personServiceMock
                .findByPrimaryKey("Sweden", "company1", "Keith Donald"),
                person, times);
    }

    private void expectUpdate() {
        Person person = setupPerson();
        personServiceMock.update(person);
    }

    private Person setupPerson() {
        Person person = new Person();
        person.setFullName("Keith Donald");
        person.setCompany("company1");
        person.setCountry("Sweden");
        person.setPhone("some phone");
        person.setLastName("Donald");
        return person;
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#getFlowDefinitionResource()
     */
    protected FlowDefinitionResource getFlowDefinitionResource() {
        return createFlowDefinitionResource("src/main/webapp/WEB-INF/flows/detail-flow.xml");
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#registerMockServices(org.springframework.webflow.test.MockFlowServiceLocator)
     */
    protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {
        serviceRegistry.registerBean("personService", personServiceMock);
        serviceRegistry.registerBean("editPersonChainedOptionManager", chainedOptionManagerMock);
    }
}