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

import java.util.Collections;

import org.easymock.MockControl;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.MappingContext;
import org.springframework.ldap.samples.person.domain.Group;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.service.GroupService;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class PersonManagerFlowExecutionTest extends
        AbstractXmlFlowExecutionTests {

    private MockControl personServiceControl;

    private PersonService personServiceMock;

    private MockControl groupServiceControl;

    private GroupService groupServiceMock;

    protected void setUp() throws Exception {
        super.setUp();

        personServiceControl = MockControl.createControl(PersonService.class);
        personServiceMock = (PersonService) personServiceControl.getMock();

        groupServiceControl = MockControl.createControl(GroupService.class);
        groupServiceMock = (GroupService) groupServiceControl.getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        personServiceControl = null;
        personServiceMock = null;
        groupServiceControl = null;
        groupServiceMock = null;
    }

    protected void replay() {
        personServiceControl.replay();
        groupServiceControl.replay();
    }

    protected void verify() {
        personServiceControl.verify();
        groupServiceControl.verify();
    }

    public void testStartFlow() {
        ApplicationView view = applicationView(startFlow());
        assertCurrentStateEquals("displayMenu");
        assertViewNameEquals("menu", view);
    }

    public void testFindPerson() {
        startFlow();
        ApplicationView view = applicationView(signalEvent("findPerson"));
        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);
    }

    public void testNewSearch() {
        startFlow();
        signalEvent("findPerson");
        ApplicationView view = applicationView(signalEvent("newSearch"));
        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);
    }

    public void testSelectValidResult() {
        startFlow();
        signalEvent("findPerson");

        MockParameterMap parameters = new MockParameterMap();
        parameters = new MockParameterMap();
        parameters.put("name", "Keith Donald");
        parameters.put("company", "company1");
        parameters.put("country", "Sweden");
        ApplicationView view = applicationView(signalEvent("select", parameters));

        assertCurrentStateEquals("displayResults");
        assertViewNameEquals("searchResult", view);
        assertModelAttributeCollectionSize(1, "results", view);
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#getFlowDefinitionResource()
     */
    protected FlowDefinitionResource getFlowDefinitionResource() {
        return createFlowDefinitionResource("src/main/webapp/WEB-INF/flows/personmanager-flow.xml");
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#registerMockServices(org.springframework.webflow.test.MockFlowServiceLocator)
     */
    protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {
        Flow mockSearchFlow = new Flow("search-flow");

        // test responding to finish result
        EndState endState = new EndState(mockSearchFlow, "finish");
        // Simulate a search result of one item
        endState.setOutputMapper(new AttributeMapper() {
            public void map(Object source, Object target, MappingContext context) {
                MutableAttributeMap outputMap = (MutableAttributeMap) target;
                outputMap.put("results", Collections
                        .singletonList(new Person()));
            }
        });

        serviceRegistry.registerSubflow(mockSearchFlow);

        Flow mockDetailFlow = new Flow("detail-flow");
        mockDetailFlow.setInputMapper(new AttributeMapper() {
            public void map(Object source, Object target, MappingContext context) {
                String name = "Keith Donald";
                String company = "company1";
                String country = "Sweden";
                assertEquals("name '" + name
                        + "' not provided as input by calling search flow",
                        name, ((AttributeMap) source).get("name"));
                assertEquals("company '" + company
                        + "' not provided as input by calling search flow",
                        company, ((AttributeMap) source).get("company"));
                assertEquals("company '" + country
                        + "' not provided as input by calling search flow",
                        country, ((AttributeMap) source).get("country"));
            }
        });
        // test responding to finish result
        new EndState(mockDetailFlow, "finish");

        serviceRegistry.registerSubflow(mockDetailFlow);

        Flow mockSearchGroupFlow = new Flow("search-group-flow");

        // test responding to finish result
        EndState endStateSearchGroup = new EndState(mockSearchGroupFlow, "finish");
        // Simulate a search result of one item
        endStateSearchGroup.setOutputMapper(new AttributeMapper() {
            public void map(Object source, Object target, MappingContext context) {
                MutableAttributeMap outputMap = (MutableAttributeMap) target;
                outputMap.put("groups", Collections
                        .singletonList(new Group()));
            }
        });

        serviceRegistry.registerSubflow(mockSearchGroupFlow);

        Flow mockDetailGroupFlow = new Flow("detail-group-flow");
        mockDetailGroupFlow.setInputMapper(new AttributeMapper() {
            public void map(Object source, Object target, MappingContext context) {
                String name = "Some Group";
                assertEquals("name '" + name
                        + "' not provided as input by calling search group flow",
                        name, ((AttributeMap) source).get("name"));
            }
        });
        // test responding to finish result
        new EndState(mockDetailGroupFlow, "finish");

        serviceRegistry.registerSubflow(mockDetailGroupFlow);

        serviceRegistry.registerBean("groupService", groupServiceMock);
        serviceRegistry.registerBean("personService", personServiceMock);
    }
}