/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.IncrementalAttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that helps with reading all attribute values from Active Directory using
 * <em>Incremental Retrieval of Multi-valued Properties</em>.
 * <p>
 * Example usage of this attribute mapper: <pre>
 *	 List values = DefaultIncrementalAttributeMapper.lookupAttributeValues(ldapTemplate, theDn, "oneAttribute");
 *	 Attributes attrs = DefaultIncrementalAttributeMapper.lookupAttributeValues(ldapTemplate, theDn, new Object[]{"oneAttribute", "anotherAttribute"});
 * </pre> For greater control, e.g. explicitly specifying the requested page size, create
 * and use an instance yourself: <pre>
 *
 *	  IncrementalAttributesMapper incrementalAttributeMapper = new DefaultIncrementalAttributeMapper(10, "someAttribute");
 *	  while (incrementalAttributeMapper.hasMore()) {
 *		  ldap.lookup(entrDn, incrementalAttributeMapper.getAttributesForLookup(), incrementalAttributeMapper);
 *	  }
 *
 *	  List values = incrementalAttributeMapper.getValues("someAttribute");
 * </pre>
 * <p>
 * <b>NOTE:</b> Instances of this class are highly stateful and must not be reused or
 * shared between threads in any way.
 * <p>
 * <b>NOTE:</b> Instances of this class can only be used with <b>lookups</b>. No support
 * is given for searches.
 * <p>
 *
 * @author Marius Scurtescu
 * @author Mattias Hellborg Arthursson
 * @see <a href="https://tools.ietf.org/html/draft-kashi-incremental-00">Incremental
 * Retrieval of Multi-valued Properties</a>
 * @see #lookupAttributes(org.springframework.ldap.core.LdapOperations, javax.naming.Name,
 * String[])
 * @see #lookupAttributeValues(org.springframework.ldap.core.LdapOperations,
 * javax.naming.Name, String)
 * @since 1.3.2
 */
public class DefaultIncrementalAttributesMapper
		implements IncrementalAttributesMapper<DefaultIncrementalAttributesMapper> {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultIncrementalAttributesMapper.class);

	private Map<String, IncrementalAttributeState> stateMap = new LinkedHashMap<String, IncrementalAttributeState>();

	private Set<String> rangedAttributesInNextIteration = new LinkedHashSet<String>();

	/**
	 * This guy will be used when an unmapped attribute is encountered. This really should
	 * never happen, but this saves us a number of null checks.
	 */
	private static final IncrementalAttributeState NOT_FOUND_ATTRIBUTE_STATE = new IncrementalAttributeState() {
		@Override
		public String getRequestedAttributeName() {
			throw new UnsupportedOperationException("This method should never be called");
		}

		@Override
		public boolean hasMore() {
			return false;
		}

		@Override
		public void calculateNextRange(RangeOption responseRange) {
			// Nothing to do here
		}

		@Override
		public String getAttributeNameForQuery() {
			throw new UnsupportedOperationException("This method should never be called");
		}

		@Override
		public void processValues(Attributes attributes, String attributeName) throws NamingException {
			// Nothing to do here
		}

		@Override
		public List<Object> getValues() {
			return null;
		}
	};

	/**
	 * Create an instance for the requested attribute.
	 * @param attributeName the name of the attribute that this instance handles. This is
	 * the attribute name that will be requested, and whose values are managed.
	 */
	public DefaultIncrementalAttributesMapper(String attributeName) {
		this(RangeOption.TERMINAL_END_OF_RANGE, attributeName);
	}

	/**
	 * Create an instance for the requested attributes.
	 * @param attributeNames the name of the attributes that this instance handles. These
	 * are the attribute names that will be requested, and whose values are managed.
	 */
	public DefaultIncrementalAttributesMapper(String[] attributeNames) {
		this(RangeOption.TERMINAL_END_OF_RANGE, attributeNames);
	}

	/**
	 * Create an instance for the requested attribute with a specific page size.
	 * @param pageSize the requested page size that will be included in range query
	 * attribute names.
	 * @param attributeName the name of the attribute that this instance handles. This is
	 * the attribute name that will be requested, and whose values are managed.
	 */
	public DefaultIncrementalAttributesMapper(int pageSize, String attributeName) {
		this(pageSize, new String[] { attributeName });
	}

	/**
	 * Create an instance for the requested attributes with a specific page size.
	 * @param pageSize the requested page size that will be included in range query
	 * attribute names.
	 * @param attributeNames the name of the attributes that this instance handles. These
	 * are the attribute names that will be requested, and whose values are managed.
	 */
	public DefaultIncrementalAttributesMapper(int pageSize, String[] attributeNames) {
		for (String attributeName : attributeNames) {
			this.stateMap.put(attributeName, new DefaultIncrementalAttributeState(attributeName, pageSize));
			this.rangedAttributesInNextIteration.add(attributeName);
		}
	}

	@Override
	public final DefaultIncrementalAttributesMapper mapFromAttributes(Attributes attributes) throws NamingException {
		if (!hasMore()) {
			throw new IllegalStateException("No more attributes!");
		}

		// Reset the affected attributes.
		this.rangedAttributesInNextIteration = new HashSet<String>();

		NamingEnumeration<String> attributeNameEnum = attributes.getIDs();
		while (attributeNameEnum.hasMore()) {
			String attributeName = attributeNameEnum.next();

			String[] attributeNameSplit = attributeName.split(";");
			IncrementalAttributeState state = getState(attributeNameSplit[0]);
			if (attributeNameSplit.length == 1) {
				// No range specification for this attribute
				state.processValues(attributes, attributeName);
			}
			else {
				for (String option : attributeNameSplit) {
					RangeOption responseRange = RangeOption.parse(option);

					if (responseRange != null) {
						state.processValues(attributes, attributeName);
						state.calculateNextRange(responseRange);
						if (state.hasMore()) {
							this.rangedAttributesInNextIteration.add(state.getRequestedAttributeName());
						}
					}
				}
			}
		}

		return this;
	}

	private IncrementalAttributeState getState(String attributeName) {
		Object mappedState = this.stateMap.get(attributeName);
		if (mappedState == null) {
			LOG.warn("Attribute '" + attributeName + "' is not handled by this instance");
			mappedState = NOT_FOUND_ATTRIBUTE_STATE;
		}

		return (IncrementalAttributeState) mappedState;
	}

	@Override
	public final List<Object> getValues(String attributeName) {
		return getState(attributeName).getValues();
	}

	@Override
	public final Attributes getCollectedAttributes() {
		BasicAttributes attributes = new BasicAttributes();

		Set<String> attributeNames = this.stateMap.keySet();
		for (String attributeName : attributeNames) {
			BasicAttribute oneAttribute = new BasicAttribute(attributeName);
			List<Object> values = getValues(attributeName);
			if (values != null) {
				for (Object oneValue : values) {
					oneAttribute.add(oneValue);
				}
			}

			attributes.put(oneAttribute);
		}

		return attributes;
	}

	@Override
	public final boolean hasMore() {
		return this.rangedAttributesInNextIteration.size() > 0;
	}

	@Override
	public final String[] getAttributesForLookup() {
		String[] result = new String[this.rangedAttributesInNextIteration.size()];
		int index = 0;
		for (String next : this.rangedAttributesInNextIteration) {
			IncrementalAttributeState state = this.stateMap.get(next);
			result[index++] = state.getAttributeNameForQuery();
		}

		return result;
	}

	/**
	 * Lookup all values for the specified attribute, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attribute name of the attribute to request.
	 * @return an Attributes instance, populated with all found values for the requested
	 * attribute. Never <code>null</code>, though the actual attribute may not be set if
	 * it was not set on the requested object.
	 */
	public static Attributes lookupAttributes(LdapOperations ldapOperations, String dn, String attribute) {
		return lookupAttributes(ldapOperations, LdapUtils.newLdapName(dn), attribute);
	}

	/**
	 * Lookup all values for the specified attributes, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attributes names of the attributes to request.
	 * @return an Attributes instance, populated with all found values for the requested
	 * attributes. Never <code>null</code>, though the actual attributes may not be set if
	 * they was not set on the requested object.
	 */
	public static Attributes lookupAttributes(LdapOperations ldapOperations, String dn, String[] attributes) {
		return lookupAttributes(ldapOperations, LdapUtils.newLdapName(dn), attributes);
	}

	/**
	 * Lookup all values for the specified attribute, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attribute name of the attribute to request.
	 * @return an Attributes instance, populated with all found values for the requested
	 * attribute. Never <code>null</code>, though the actual attribute may not be set if
	 * it was not set on the requested object.
	 */
	public static Attributes lookupAttributes(LdapOperations ldapOperations, Name dn, String attribute) {
		return lookupAttributes(ldapOperations, dn, new String[] { attribute });
	}

	/**
	 * Lookup all values for the specified attributes, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attributes names of the attributes to request.
	 * @return an Attributes instance, populated with all found values for the requested
	 * attributes. Never <code>null</code>, though the actual attributes may not be set if
	 * they was not set on the requested object.
	 */
	public static Attributes lookupAttributes(LdapOperations ldapOperations, Name dn, String[] attributes) {
		return loopForAllAttributeValues(ldapOperations, dn, attributes).getCollectedAttributes();
	}

	/**
	 * Lookup all values for the specified attribute, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attribute name of the attribute to request.
	 * @return a list with all attribute values found for the requested attribute. Never
	 * <code>null</code>, an empty list indicates that the attribute was not set or empty.
	 */
	public static List<Object> lookupAttributeValues(LdapOperations ldapOperations, String dn, String attribute) {
		return lookupAttributeValues(ldapOperations, LdapUtils.newLdapName(dn), attribute);
	}

	/**
	 * Lookup all values for the specified attribute, looping through the results
	 * incrementally if necessary.
	 * @param ldapOperations The instance to use for performing the actual lookup.
	 * @param dn The distinguished name of the object to find.
	 * @param attribute name of the attribute to request.
	 * @return a list with all attribute values found for the requested attribute. Never
	 * <code>null</code>, an empty list indicates that the attribute was not set or empty.
	 */
	public static List<Object> lookupAttributeValues(LdapOperations ldapOperations, Name dn, String attribute) {
		List<Object> values = loopForAllAttributeValues(ldapOperations, dn, new String[] { attribute })
				.getValues(attribute);
		if (values == null) {
			values = Collections.emptyList();
		}

		return values;
	}

	private static DefaultIncrementalAttributesMapper loopForAllAttributeValues(LdapOperations ldapOperations, Name dn,
			String[] attributes) {
		DefaultIncrementalAttributesMapper mapper = new DefaultIncrementalAttributesMapper(attributes);
		while (mapper.hasMore()) {
			ldapOperations.lookup(dn, mapper.getAttributesForLookup(), mapper);
		}
		return mapper;
	}

	/**
	 * This class keeps track of the state of an individual attribute in the process of
	 * collecting multi-value attributes using ranges. Holds the values collected thus
	 * far, the next applicable range, and the actual (requested) attribute name.
	 */
	private static final class DefaultIncrementalAttributeState implements IncrementalAttributeState {

		private final String actualAttributeName;

		private List<Object> values = null;

		private final int pageSize;

		boolean more = true;

		private RangeOption requestRange;

		private DefaultIncrementalAttributeState(String actualAttributeName, int pageSize) {
			this.actualAttributeName = actualAttributeName;
			this.pageSize = pageSize;
			this.requestRange = new RangeOption(0, pageSize);
		}

		@Override
		public boolean hasMore() {
			return this.more;
		}

		@Override
		public String getRequestedAttributeName() {
			return this.actualAttributeName;
		}

		@Override
		public void calculateNextRange(RangeOption responseRange) {
			this.more = this.requestRange.compareTo(responseRange) > 0;

			if (this.more) {
				this.requestRange = responseRange.nextRange(this.pageSize);
			}
		}

		@Override
		public String getAttributeNameForQuery() {
			StringBuilder attributeBuilder = new StringBuilder(this.actualAttributeName);

			if (!(this.requestRange.isFullRange())) {
				attributeBuilder.append(';');
				this.requestRange.appendTo(attributeBuilder);
			}

			return attributeBuilder.toString();
		}

		@Override
		public void processValues(Attributes attributes, String attributeName) throws NamingException {
			Attribute attribute = attributes.get(attributeName);
			NamingEnumeration<?> valueEnum = attribute.getAll();

			initValuesIfApplicable();
			while (valueEnum.hasMore()) {
				this.values.add(valueEnum.next());
			}
		}

		private void initValuesIfApplicable() {
			if (this.values == null) {
				this.values = new LinkedList<Object>();
			}
		}

		@Override
		public List<Object> getValues() {
			if (this.values != null) {
				return new ArrayList<Object>(this.values);
			}
			else {
				return null;
			}
		}

	}

	/**
	 * @author Mattias Hellborg Arthursson
	 */
	private interface IncrementalAttributeState {

		boolean hasMore();

		void calculateNextRange(RangeOption responseRange);

		String getAttributeNameForQuery();

		String getRequestedAttributeName();

		void processValues(Attributes attributes, String attributeName) throws NamingException;

		List<Object> getValues();

	}

}
