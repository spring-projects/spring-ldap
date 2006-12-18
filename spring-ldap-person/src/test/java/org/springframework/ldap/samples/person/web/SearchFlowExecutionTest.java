package org.springframework.ldap.samples.person.web;

import java.util.Collections;

import org.easymock.MockControl;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;
import org.springframework.ldap.samples.person.service.PersonService;
import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

public class SearchFlowExecutionTest extends AbstractXmlFlowExecutionTests {

    private MockControl personServiceControl;

    private PersonService personServiceMock;

    protected void setUp() throws Exception {
        super.setUp();

        personServiceControl = MockControl.createControl(PersonService.class);
        personServiceMock = (PersonService) personServiceControl.getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        personServiceControl = null;
        personServiceMock = null;
    }

    protected void replay() {
        personServiceControl.replay();
    }

    protected void verify() {
        personServiceControl.verify();
    }

    public void testStartFlow() {
        ApplicationView view = applicationView(startFlow());
        assertCurrentStateEquals("enterCriteria");
        assertViewNameEquals("searchForm", view);
        assertModelAttributeNotNull("searchCriteria", view);
    }

    public void testCriteriaSubmitSuccess() {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setName("Keith");
        personServiceControl.expectAndReturn(personServiceMock
                .find(searchCriteria), Collections.singletonList(new Person()));
        replay();

        startFlow();
        MockParameterMap parameters = new MockParameterMap();
        parameters.put("name", "Keith");
        signalEvent("search", parameters);

        verify();

        assertFlowExecutionEnded();
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#getFlowDefinitionResource()
     */
    protected FlowDefinitionResource getFlowDefinitionResource() {
        return createFlowDefinitionResource("src/main/webapp/WEB-INF/flows/search-flow.xml");
    }

    /*
     * @see org.springframework.webflow.test.execution.AbstractExternalizedFlowExecutionTests#registerMockServices(org.springframework.webflow.test.MockFlowServiceLocator)
     */
    protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {
        serviceRegistry.registerBean("personService", personServiceMock);
    }
}