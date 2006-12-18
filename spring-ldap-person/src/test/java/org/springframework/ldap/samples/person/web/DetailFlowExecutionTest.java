package org.springframework.ldap.samples.person.web;

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

    private MutableAttributeMap attributeMap;

    protected void setUp() throws Exception {
        super.setUp();

        personServiceControl = MockControl.createControl(PersonService.class);
        personServiceMock = (PersonService) personServiceControl.getMock();

        attributeMap = new LocalAttributeMap();
        attributeMap.put("country", "Sweden");
        attributeMap.put("company", "company1");
        attributeMap.put("name", "Keith Donald");
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
        expectFindByPrimaryKey(2);
        replay();

        startFlow(attributeMap);
        ApplicationView view = applicationView(signalEvent("edit"));

        verify();
        assertCurrentStateEquals("editDetails");
        assertViewNameEquals("editDetails", view);
        assertModelAttributeNotNull("person", view);
    }

    public void testCancel() {
        expectFindByPrimaryKey(3);
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
        expectFindByPrimaryKey(3);
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
    }
}