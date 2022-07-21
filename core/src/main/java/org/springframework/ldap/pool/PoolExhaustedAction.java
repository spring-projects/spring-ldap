package org.springframework.ldap.pool;

/**
 * @author Mattias Hellborg Arthursson
 */
public enum PoolExhaustedAction {
	FAIL((byte)0),
	BLOCK((byte)1),
	GROW((byte)2);

	private final byte value;

	private PoolExhaustedAction(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}
}
