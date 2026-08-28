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

import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DefaultModificationItemsCollector} directly, independent of
 * {@link DirContextAdapter}.
 *
 * @author Josh Cummings
 */
public class DefaultModificationItemsCollectorTests {

	private final DefaultModificationItemsCollector tested = new DefaultModificationItemsCollector();

	@Test
	public void collectWhenOriginalIsNullAndUpdatedHasValuesThenAdds() {
		NameAwareAttribute updated = new NameAwareAttribute("cn", "John Doe");

		Collection<ModificationItem> items = this.tested.collect(null, updated);

		assertThat(items).hasSize(1);
		ModificationItem item = items.iterator().next();
		assertThat(item.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		assertThat(item.getAttribute()).isSameAs(updated);
	}

	@Test
	public void collectWhenOriginalIsEmptyAndUpdatedHasValuesThenAdds() {
		NameAwareAttribute original = new NameAwareAttribute("cn");
		NameAwareAttribute updated = new NameAwareAttribute("cn", "John Doe");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(1);
		assertThat(items.iterator().next().getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
	}

	@Test
	public void collectWhenOriginalAndUpdatedAreEqualThenNoModifications() {
		NameAwareAttribute original = new NameAwareAttribute("cn", "John Doe");
		NameAwareAttribute updated = new NameAwareAttribute("cn", "John Doe");

		assertThat(this.tested.collect(original, updated)).isEmpty();
	}

	@Test
	public void collectWhenOriginalIsNullAndUpdatedIsEmptyThenNoModifications() {
		NameAwareAttribute updated = new NameAwareAttribute("cn");

		assertThat(this.tested.collect(null, updated)).isEmpty();
	}

	@Test
	public void collectWhenUpdatedIsEmptyThenRemoves() {
		NameAwareAttribute original = new NameAwareAttribute("cn", "John Doe");
		NameAwareAttribute updated = new NameAwareAttribute("cn");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(1);
		assertThat(items.iterator().next().getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
	}

	@Test
	public void collectWhenBothSingleValuedAndDifferentThenReplaces() throws Exception {
		NameAwareAttribute original = new NameAwareAttribute("cn", "John Doe");
		NameAwareAttribute updated = new NameAwareAttribute("cn", "Jane Doe");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(1);
		ModificationItem item = items.iterator().next();
		assertThat(item.getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		assertThat(item.getAttribute().get()).isEqualTo("Jane Doe");
	}

	@Test
	public void collectWhenUpdatedIsOrderedThenReplacesEvenWithMultipleValues() {
		NameAwareAttribute original = new NameAwareAttribute("cn", true);
		original.add("a");
		original.add("b");
		NameAwareAttribute updated = new NameAwareAttribute("cn", true);
		updated.add("b");
		updated.add("a");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(1);
		assertThat(items.iterator().next().getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
	}

	@Test
	public void collectWhenUnorderedMultiValuePartiallyChangedThenAddsAndRemovesByValue() {
		NameAwareAttribute original = new NameAwareAttribute("description");
		original.add("a");
		original.add("b");
		original.add("c");
		NameAwareAttribute updated = new NameAwareAttribute("description");
		updated.add("a");
		updated.add("b");
		updated.add("d");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(2);
		assertThat(items).anySatisfy((item) -> {
			assertThat(item.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
			assertThat(item.getAttribute().contains("c")).isTrue();
		});
		assertThat(items).anySatisfy((item) -> {
			assertThat(item.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
			assertThat(item.getAttribute().contains("d")).isTrue();
		});
	}

	@Test
	public void collectWhenUnorderedMultiValueCompletelyChangedThenReplaces() {
		NameAwareAttribute original = new NameAwareAttribute("description");
		original.add("a");
		original.add("b");
		NameAwareAttribute updated = new NameAwareAttribute("description");
		updated.add("c");
		updated.add("d");

		Collection<ModificationItem> items = this.tested.collect(original, updated);

		assertThat(items).hasSize(1);
		ModificationItem item = items.iterator().next();
		assertThat(item.getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		assertThat(item.getAttribute().contains("c")).isTrue();
		assertThat(item.getAttribute().contains("d")).isTrue();
	}

}
