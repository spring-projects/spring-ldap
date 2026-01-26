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

import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;

import com.sun.jndi.ldap.ctl.DirSyncResponseControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Josh Cummings
 */
class PagedResultsControlExchangeDirContextProcessorTests {

	private LdapContext ldapContextMock;

	private PagedResultsControlExchangeDirContextProcessor tested;

	@BeforeEach
	void setUp() {
		this.tested = new PagedResultsControlExchangeDirContextProcessor(20);
		this.ldapContextMock = mock(LdapContext.class);
	}

	@Test
	void preProcessWhenPagedRequestControlsThenReplaces() throws Exception {
		Control[] request = new Control[] { new PagedResultsControl(15, false) };
		given(this.ldapContextMock.getRequestControls()).willReturn(request);
		this.tested.preProcess(this.ldapContextMock);
		ArgumentCaptor<Control[]> captor = ArgumentCaptor.forClass(Control[].class);
		verify(this.ldapContextMock).setRequestControls(captor.capture());
		Control[] controls = captor.getValue();
		assertThat(controls).hasSize(1);
		assertThat(controls[0]).isInstanceOf(PagedResultsControl.class);
		assertThat(this.tested.getExchange().getRequest()).isSameAs(controls[0]);
	}

	@Test
	void preProcessWhenNoRequestControlsThenAdds() throws Exception {
		this.tested.preProcess(this.ldapContextMock);
		ArgumentCaptor<Control[]> captor = ArgumentCaptor.forClass(Control[].class);
		verify(this.ldapContextMock).setRequestControls(captor.capture());
		Control[] controls = captor.getValue();
		assertThat(controls).hasExactlyElementsOfTypes(PagedResultsControl.class);
	}

	@Test
	void preProcessWhenRequestControlsThenAdds() throws Exception {
		Control[] request = new Control[] { new SortControl("key", true) };
		given(this.ldapContextMock.getRequestControls()).willReturn(request);
		this.tested.preProcess(this.ldapContextMock);
		ArgumentCaptor<Control[]> captor = ArgumentCaptor.forClass(Control[].class);
		verify(this.ldapContextMock).setRequestControls(captor.capture());
		Control[] controls = captor.getValue();
		assertThat(controls).hasExactlyElementsOfTypes(SortControl.class, PagedResultsControl.class);
		assertThat(controls[1]).isInstanceOf(PagedResultsControl.class);
		assertThat(this.tested.getExchange().getRequest()).isSameAs(controls[1]);
	}

	@Test
	void postProcessWhenNoResponseThenIgnores() throws Exception {
		given(this.ldapContextMock.getResponseControls()).willReturn(null);
		ControlExchange<?, ?> exchange = spy(this.tested.getExchange());
		this.tested.postProcess(this.ldapContextMock);
		verify(exchange, times(0)).withResponse(any());
	}

	@Test
	void postProcessWhenResponsesThenUsesPagedResultsResponseControl() throws Exception {
		byte resultSize = 64;
		byte[] cookie = new byte[3];
		byte[] prefix = new byte[] { 30, 5, 2, 1, resultSize, 4, (byte) cookie.length };
		byte[] value = new byte[prefix.length + cookie.length];
		System.arraycopy(prefix, 0, value, 0, prefix.length);
		PagedResultsResponseControl response = new PagedResultsResponseControl(PagedResultsResponseControl.OID, true,
				value);
		given(this.ldapContextMock.getResponseControls())
			.willReturn(new Control[] { response, new DirSyncResponseControl("dummy", true, null) });
		this.tested.postProcess(this.ldapContextMock);
		assertThat(this.tested.getExchange().getResponse()).isNotNull();
		assertThat(this.tested.hasMore()).isTrue();
		assertThat(this.tested.getExchange().getResponse().getResultSize()).isEqualTo(resultSize);
		assertThat(this.tested.getExchange().getResponse().getCookie()).isEqualTo(cookie);
	}

}
