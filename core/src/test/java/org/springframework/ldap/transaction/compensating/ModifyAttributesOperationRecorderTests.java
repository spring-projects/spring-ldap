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

package org.springframework.ldap.transaction.compensating;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.core.IncrementalAttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

public class ModifyAttributesOperationRecorderTests {

	private LdapOperations ldapOperationsMock;

	private IncrementalAttributesMapper attributesMapperMock;

	private ModifyAttributesOperationRecorder tested;

	@BeforeEach
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);
		this.attributesMapperMock = mock(IncrementalAttributesMapper.class);

		this.tested = new ModifyAttributesOperationRecorder(this.ldapOperationsMock);
	}

	@Test
	public void testRecordOperation() {
		final ModificationItem incomingItem = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("attribute1"));
		ModificationItem[] incomingMods = new ModificationItem[] { incomingItem };
		final ModificationItem compensatingItem = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("attribute2"));

		final Attributes expectedAttributes = new BasicAttributes();

		this.tested = new ModifyAttributesOperationRecorder(this.ldapOperationsMock) {
			IncrementalAttributesMapper getAttributesMapper(String[] attributeNames) {
				return ModifyAttributesOperationRecorderTests.this.attributesMapperMock;
			}

			protected ModificationItem getCompensatingModificationItem(Attributes originalAttributes,
					ModificationItem modificationItem) {
				assertThat(originalAttributes).isSameAs(expectedAttributes);
				assertThat(modificationItem).isSameAs(incomingItem);
				return compensatingItem;
			}
		};

		LdapName expectedName = LdapUtils.newLdapName("cn=john doe");

		given(this.attributesMapperMock.hasMore()).willReturn(true, false);
		given(this.attributesMapperMock.getAttributesForLookup()).willReturn(new String[] { "attribute1" });
		given(this.ldapOperationsMock.lookup(expectedName, new String[] { "attribute1" }, this.attributesMapperMock))
			.willReturn(expectedAttributes);
		given(this.attributesMapperMock.getCollectedAttributes()).willReturn(expectedAttributes);

		// Perform test
		CompensatingTransactionOperationExecutor operation = this.tested
			.recordOperation(new Object[] { expectedName, incomingMods });

		// Verify outcome
		assertThat(operation instanceof ModifyAttributesOperationExecutor).isTrue();
		ModifyAttributesOperationExecutor rollbackOperation = (ModifyAttributesOperationExecutor) operation;
		assertThat(rollbackOperation.getDn()).isSameAs(expectedName);
		assertThat(rollbackOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
		ModificationItem[] actualModifications = rollbackOperation.getActualModifications();
		assertThat(actualModifications.length).isEqualTo(incomingMods.length);
		assertThat(actualModifications[0]).isEqualTo(incomingMods[0]);
		assertThat(rollbackOperation.getCompensatingModifications().length).isEqualTo(1);
		assertThat(rollbackOperation.getCompensatingModifications()[0]).isSameAs(compensatingItem);
	}

	@Test
	public void testGetCompensatingModificationItem_RemoveFullExistingAttribute() throws NamingException {
		BasicAttribute attribute = new BasicAttribute("someattr");
		attribute.add("value1");
		attribute.add("value2");
		Attributes attributes = new BasicAttributes();
		attributes.put(attribute);

		ModificationItem originalItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("someattr"));

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		Object object = resultAttribute.get(0);
		assertThat(object).isEqualTo("value1");
		assertThat(resultAttribute.get(1)).isEqualTo("value2");
	}

	@Test
	public void testGetCompensatingModificationItem_RemoveTwoAttributeValues() throws NamingException {
		BasicAttribute attribute = new BasicAttribute("someattr");
		attribute.add("value1");
		attribute.add("value2");
		attribute.add("value3");
		Attributes attributes = new BasicAttributes();
		attributes.put(attribute);

		BasicAttribute modificationAttribute = new BasicAttribute("someattr");
		modificationAttribute.add("value1");
		modificationAttribute.add("value2");
		ModificationItem originalItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, modificationAttribute);

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		Object object = resultAttribute.get(0);
		assertThat(object).isEqualTo("value1");
		assertThat(resultAttribute.get(1)).isEqualTo("value2");
	}

	@Test
	public void testGetCompensatingModificationItem_ReplaceExistingAttribute() throws NamingException {
		BasicAttribute attribute = new BasicAttribute("someattr");
		attribute.add("value1");
		attribute.add("value2");
		Attributes attributes = new BasicAttributes();
		attributes.put(attribute);

		BasicAttribute modificationAttribute = new BasicAttribute("someattr");
		modificationAttribute.add("newvalue1");
		modificationAttribute.add("newvalue2");
		ModificationItem originalItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("someattr"));

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		Object object = resultAttribute.get(0);
		assertThat(object).isEqualTo("value1");
		assertThat(resultAttribute.get(1)).isEqualTo("value2");
	}

	@Test
	public void testGetCompensatingModificationItem_ReplaceNonExistingAttribute() throws NamingException {
		Attributes attributes = new BasicAttributes();

		BasicAttribute modificationAttribute = new BasicAttribute("someattr");
		modificationAttribute.add("newvalue1");
		modificationAttribute.add("newvalue2");
		ModificationItem originalItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, modificationAttribute);

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		assertThat(resultAttribute.size()).isEqualTo(0);
	}

	@Test
	public void testGetCompensatingModificationItem_AddNonExistingAttribute() throws NamingException {
		Attributes attributes = new BasicAttributes();

		BasicAttribute modificationAttribute = new BasicAttribute("someattr");
		modificationAttribute.add("newvalue1");
		modificationAttribute.add("newvalue2");
		ModificationItem originalItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, modificationAttribute);

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		assertThat(resultAttribute.size()).isEqualTo(0);
	}

	@Test
	public void testGetCompensatingModificationItem_AddExistingAttribute() throws NamingException {
		BasicAttribute attribute = new BasicAttribute("someattr");
		attribute.add("value1");
		attribute.add("value2");
		Attributes attributes = new BasicAttributes();
		attributes.put(attribute);

		BasicAttribute modificationAttribute = new BasicAttribute("someattr");
		modificationAttribute.add("newvalue1");
		modificationAttribute.add("newvalue2");
		ModificationItem originalItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("someattr"));

		// Perform test
		ModificationItem result = this.tested.getCompensatingModificationItem(attributes, originalItem);

		// Verify result
		assertThat(result.getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute resultAttribute = result.getAttribute();
		assertThat(resultAttribute.getID()).isEqualTo("someattr");
		assertThat(result.getAttribute().get(0)).isEqualTo("value1");
		assertThat(result.getAttribute().get(1)).isEqualTo("value2");
	}

}
