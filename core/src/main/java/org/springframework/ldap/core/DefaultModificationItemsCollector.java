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

package org.springframework.ldap.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * The default engine for determining the {@link ModificationItem} list for a given
 * attribute update. If the original and updated size of an attribute is 1, it is replaced
 * in its entirety. If the updated attribute is empty, it is removed. Otherwise, the
 * attribute is a multi-value attribute; if {@link #mayRemoveByValuePredicate} disallows
 * removing individual values for this attribute pair, it is replaced in its entirety,
 * otherwise all modifications to the original value (removals and additions) are
 * collected individually.
 *
 * @author Josh Cummings
 */
final class DefaultModificationItemsCollector {

	/**
	 * Attribute types that RFC 4519 (and common aliases/OIDs) define without an equality
	 * matching rule. Per RFC 2251 §4.6, clients must REPLACE remaining values rather than
	 * REMOVE individual values for these types.
	 */
	private static final Set<String> ATTRS_WITHOUT_EQUALITY_MATCHING_RULE = Set.of("facsimiletelephonenumber", "fax",
			"2.5.4.23", "telexnumber", "2.5.4.21", "teletexterminalidentifier", "2.5.4.22");

	private static final BiPredicate<@Nullable NameAwareAttribute, NameAwareAttribute> DEFAULT_MAY_REMOVE_BY_VALUE = (
			original, updated) -> !updated.isOrdered() && hasEqualityMatchingRule(updated.getID());

	private final BiPredicate<@Nullable NameAwareAttribute, NameAwareAttribute> mayRemoveByValuePredicate;

	DefaultModificationItemsCollector() {
		this(DEFAULT_MAY_REMOVE_BY_VALUE);
	}

	DefaultModificationItemsCollector(
			BiPredicate<@Nullable NameAwareAttribute, NameAwareAttribute> mayRemoveByValuePredicate) {
		Assert.notNull(mayRemoveByValuePredicate, "mayRemoveByValuePredicate must not be null");
		this.mayRemoveByValuePredicate = mayRemoveByValuePredicate;
	}

	private static boolean hasEqualityMatchingRule(String attributeId) {
		return attributeId == null
				|| !ATTRS_WITHOUT_EQUALITY_MATCHING_RULE.contains(attributeId.toLowerCase(Locale.ROOT));
	}

	/**
	 * Collect all modifications for the changed attribute. If no changes have been made,
	 * return immediately. If modifications have been made, and the original size as well
	 * as the updated size of the attribute is 1, replace the attribute. If the size of
	 * the updated attribute is 0, remove the attribute. Otherwise, the attribute is a
	 * multi-value attribute.
	 *
	 * By default, if it's an ordered one, or it has no equality matching rule, it should
	 * be replaced in its entirety (RFC 2251 §4.6). If not, all modifications to the
	 * original value (removals and additions) will be collected individually. This is
	 * mediated by {@link #mayRemoveByValuePredicate}.
	 */
	Collection<ModificationItem> collect(@Nullable NameAwareAttribute original, NameAwareAttribute updated) {
		if ((original == null || original.size() == 0) && updated.size() > 0) {
			return List.of(new ModificationItem(DirContext.ADD_ATTRIBUTE, updated));
		}
		if (original == null) {
			return Collections.emptyList();
		}
		if (original.equals(updated)) {
			return Collections.emptyList();
		}
		if (updated.size() == 0) {
			return List.of(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, updated));
		}
		if (original.size() == 1 && updated.size() == 1) {
			return List.of(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, updated));
		}
		if (!this.mayRemoveByValuePredicate.test(original, updated)) {
			return List.of(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, updated));
		}
		List<ModificationItem> items = removeByValue(original, updated);
		if (items.isEmpty()) {
			// This means that the attributes are not equal, but the
			// actual values are the same - thus the order must have
			// changed. This should result in a REPLACE_ATTRIBUTE operation.
			return List.of(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, updated));
		}
		return items;
	}

	private List<ModificationItem> removeByValue(NameAwareAttribute original, NameAwareAttribute updated) {
		List<ModificationItem> items = new LinkedList<>();
		Attribute originalClone = (Attribute) original.clone();
		Attribute addedValuesAttribute = new NameAwareAttribute(original.getID());
		for (Object value : updated) {
			if (!originalClone.remove(value)) {
				addedValuesAttribute.add(value);
			}
		}
		// We have now traversed and removed all values from the original that
		// were also present in the new values. The remaining values in the
		// original must be the ones that were removed.
		if (originalClone.size() > 0 && originalClone.size() == original.size()) {
			// This is actually a complete replacement of the attribute values.
			// Fall back to REPLACE
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, addedValuesAttribute));
		}
		else {
			if (originalClone.size() > 0) {
				items.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, originalClone));
			}
			if (addedValuesAttribute.size() > 0) {
				items.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, addedValuesAttribute));
			}
		}
		return items;
	}

}
