/*
 * Copyright 2006-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.control;

import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * Unit tests for how {@link AbstractFallbackRequestAndResponseControlDirContextProcessor}
 * matches a response control to the expected response control class.
 */
class AbstractFallbackRequestAndResponseControlDirContextProcessorTests {

	private final LdapContext ldapContext = mock(LdapContext.class);

	@Test
	void postProcessWhenResponseIsExpectedClassThenHandles() throws Exception {
		RecordingProcessor processor = new RecordingProcessor(BaseControl.class);
		Control response = new BaseControl();
		given(this.ldapContext.getResponseControls()).willReturn(new Control[] { response });
		processor.postProcess(this.ldapContext);
		assertThat(processor.handled).isSameAs(response);
	}

	@Test
	void postProcessWhenResponseIsSubclassOfExpectedClassThenHandles() throws Exception {
		RecordingProcessor processor = new RecordingProcessor(BaseControl.class);
		Control response = new SubControl();
		given(this.ldapContext.getResponseControls()).willReturn(new Control[] { response });
		processor.postProcess(this.ldapContext);
		assertThat(processor.handled).isSameAs(response);
	}

	@Test
	void postProcessWhenResponseIsSuperclassOfExpectedClassThenHandles() throws Exception {
		RecordingProcessor processor = new RecordingProcessor(SubControl.class);
		Control response = new BaseControl();
		given(this.ldapContext.getResponseControls()).willReturn(new Control[] { response });
		processor.postProcess(this.ldapContext);
		assertThat(processor.handled).isSameAs(response);
	}

	@Test
	void postProcessWhenResponseIsUnrelatedClassThenIgnores() throws Exception {
		RecordingProcessor processor = new RecordingProcessor(SubControl.class);
		given(this.ldapContext.getResponseControls()).willReturn(new Control[] { new OtherControl() });
		processor.postProcess(this.ldapContext);
		assertThat(processor.handled).isNull();
	}

	private static final class RecordingProcessor extends AbstractFallbackRequestAndResponseControlDirContextProcessor {

		private Object handled;

		private RecordingProcessor(Class<?> responseControlClass) {
			setResponseControlClass(responseControlClass);
		}

		@Override
		public Control createRequestControl() {
			return null;
		}

		@Override
		protected void handleResponse(Object control) {
			this.handled = control;
		}

	}

	private static class BaseControl extends BasicControl {

		BaseControl() {
			super("1.2.3.4");
		}

	}

	private static final class SubControl extends BaseControl {

	}

	private static final class OtherControl extends BasicControl {

		OtherControl() {
			super("1.2.3.5");
		}

	}

}
