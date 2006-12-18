package webflow.templates;

import java.util.Map;

import org.springframework.webflow.definition.registry.FlowDefinitionResource;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;
import org.springframework.webflow.test.execution.MockFlowServiceLocator;

public class FlowExecutionTestTemplate extends AbstractXmlFlowExecutionTests {

	public void testStartFlow() {
		
	}
	
	@Override
	protected FlowDefinitionResource getFlowDefinitionResource() {
        return createFlowDefinitionResource("/path/to/myflow.xml");
	}

	@Override
	protected void registerMockServices(MockFlowServiceLocator serviceRegistry) {

	}
}