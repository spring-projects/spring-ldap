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

package org.springframework.ldap.core.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attribute name Range Option used for <em>Incremental Retrieval of Multi-valued
 * Properties</em>.
 *
 * @author Marius Scurtescu
 * @since 1.3.2
 * @see DefaultIncrementalAttributesMapper
 */
class RangeOption implements Comparable<RangeOption> {

	public static final int TERMINAL_END_OF_RANGE = -1;

	public static final int TERMINAL_MISSING = -2;

	private int initial = 0;

	private int terminal = TERMINAL_END_OF_RANGE;

	private static final Pattern RANGE_PATTERN = Pattern.compile("^Range=([0-9]+)(-([0-9]+|\\*))?$",
			Pattern.CASE_INSENSITIVE);

	RangeOption(int initial) {
		this(initial, TERMINAL_END_OF_RANGE);
	}

	RangeOption(int initial, int terminal) {
		if (terminal < 0 && (terminal != TERMINAL_END_OF_RANGE && terminal != TERMINAL_MISSING)) {
			throw new IllegalArgumentException("Illegal range-terminal: " + terminal);
		}

		if (initial < 0) {
			throw new IllegalArgumentException("Illegal range-initial: " + initial);
		}

		if (terminal >= 0 && terminal < initial) {
			throw new IllegalArgumentException(
					"range-terminal cannot be smaller than range-initial: " + initial + "-" + terminal);
		}

		this.initial = initial;
		this.terminal = terminal;
	}

	boolean isTerminalEndOfRange() {
		return this.terminal == TERMINAL_END_OF_RANGE;
	}

	boolean isTerminalMissing() {
		return this.terminal == TERMINAL_MISSING;
	}

	int getInitial() {
		return this.initial;
	}

	int getTerminal() {
		return this.terminal;
	}

	boolean isFullRange() {
		return getInitial() == 0 && getTerminal() == TERMINAL_END_OF_RANGE;
	}

	void appendTo(StringBuilder rangeBuilder) {
		rangeBuilder.append("Range=").append(this.initial);

		if (!isTerminalMissing()) {
			rangeBuilder.append('-');

			if (isTerminalEndOfRange()) {
				rangeBuilder.append('*');
			}
			else {
				rangeBuilder.append(this.terminal);
			}
		}
	}

	static RangeOption parse(String option) {
		Matcher rangeMatcher = RANGE_PATTERN.matcher(option);

		rangeMatcher.find();

		if (!rangeMatcher.matches()) {
			return null;
		}

		String initialStr = rangeMatcher.group(1);

		int initial = Integer.parseInt(initialStr);
		int terminal = TERMINAL_MISSING;

		if (rangeMatcher.group(2) != null) {
			String terminalStr = rangeMatcher.group(3);

			if ("*".equals(terminalStr)) {
				terminal = TERMINAL_END_OF_RANGE;
			}
			else {
				terminal = Integer.parseInt(terminalStr);
			}
		}

		return new RangeOption(initial, terminal);
	}

	@Override
	public int compareTo(RangeOption that) {
		if (this.getInitial() != that.getInitial()) {
			throw new IllegalStateException("Ranges cannot be compared, range-initial not the same: " + this.toString()
					+ " vs " + that.toString());
		}

		if (this.getTerminal() == that.getTerminal()) {
			return 0;
		}

		if (that.getTerminal() == TERMINAL_MISSING) {
			throw new IllegalStateException("Don't know how to deal with missing range-terminal: " + that.toString());
		}

		if (this.getTerminal() == TERMINAL_MISSING) {
			throw new IllegalStateException("Don't know how to deal with missing range-terminal: " + this.toString());
		}

		if (this.getTerminal() == TERMINAL_END_OF_RANGE) {
			return 1;
		}

		if (that.getTerminal() == TERMINAL_END_OF_RANGE) {
			return -1;
		}

		return (this.getTerminal() > that.getTerminal()) ? 1 : -1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RangeOption that = (RangeOption) o;

		if (this.initial != that.initial) {
			return false;
		}
		if (this.terminal != that.terminal) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = this.initial;
		result = 31 * result + this.terminal;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder rangeBuilder = new StringBuilder();
		appendTo(rangeBuilder);

		return rangeBuilder.toString();
	}

	RangeOption nextRange(int pageSize) {
		if (getTerminal() < 0) {
			throw new IllegalStateException("Cannot generate next range, range-terminal: " + getTerminal());
		}

		if (pageSize < 0 && pageSize != TERMINAL_END_OF_RANGE) {
			throw new IllegalArgumentException("Invalid page size: " + pageSize);
		}

		int initial = getTerminal() + 1;
		int terminal = (pageSize != TERMINAL_END_OF_RANGE) ? getTerminal() + pageSize : TERMINAL_END_OF_RANGE;

		return new RangeOption(initial, terminal);
	}

}
