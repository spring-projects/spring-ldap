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

import org.jspecify.annotations.NullUnmarked;

import org.springframework.ldap.support.LdapUtils;

/**
 * @deprecated As of 2.0 it is recommended to use {@link javax.naming.ldap.LdapName} along
 * with utility methods in {@link LdapUtils} instead.
 */
@Deprecated
@NullUnmarked
public class DnParserImpl implements DnParser, DnParserImplConstants {

	/**
	 * input -&gt; dn dn -&gt; rdn ( ( "," | ";" ) rdn )* rdn -&gt; attributeTypeAndValue
	 * ( "+" attributeTypeAndValue )* attributeTypeAndValue -&gt; ( &lt;SPACE&gt; )*
	 * AttributeType SpacedEquals AttributeValue ( &lt;SPACE&gt; )* SpacedEquals -&gt;
	 * &lt;SPACED_EQUALS&gt; AttributeType -&gt; &lt;LDAP_OID&gt; |
	 * &lt;ATTRIBUTE_TYPE_STRING&gt; AttributeValue -&gt; &lt;ATTRVALUE&gt;
	 */
	public final void input() throws ParseException {
		dn();
	}

	public final DistinguishedName dn() throws ParseException {
		DistinguishedName dn = new DistinguishedName();
		LdapRdn rdn;
		rdn = rdn();
		dn.add(0, rdn);
		label_1: while (true) {
			switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
				case 19:
				case 20:
					break;
				default:
					this.jj_la1[0] = this.jj_gen;
					break label_1;
			}
			switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
				case 19:
					jj_consume_token(19);
					break;
				case 20:
					jj_consume_token(20);
					break;
				default:
					this.jj_la1[1] = this.jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
			}
			rdn = rdn();
			dn.add(0, rdn);
		}
		return dn;
	}

	public final LdapRdn rdn() throws ParseException {
		LdapRdnComponent rdnComponent;
		LdapRdn rdn = new LdapRdn();
		rdnComponent = attributeTypeAndValue();
		rdn.addComponent(rdnComponent);
		label_2: while (true) {
			switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
				case 21:
					break;
				default:
					this.jj_la1[2] = this.jj_gen;
					break label_2;
			}
			jj_consume_token(21);
			rdnComponent = attributeTypeAndValue();
			rdn.addComponent(rdnComponent);
		}
		return rdn;
	}

	public final LdapRdnComponent attributeTypeAndValue() throws ParseException {
		String attributeType;
		String value;
		label_3: while (true) {
			switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
				case SPACE:
					break;
				default:
					this.jj_la1[3] = this.jj_gen;
					break label_3;
			}
			jj_consume_token(SPACE);
		}
		attributeType = AttributeType();
		SpacedEquals();
		value = AttributeValue();
		label_4: while (true) {
			switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
				case SPACE:
					break;
				default:
					this.jj_la1[4] = this.jj_gen;
					break label_4;
			}
			jj_consume_token(SPACE);
		}
		return new LdapRdnComponent(attributeType, value, true);
	}

	public final void SpacedEquals() throws ParseException {
		this.token_source.SwitchTo(SPACED_EQUALS_S);
		jj_consume_token(SPACED_EQUALS);
	}

	public final String AttributeType() throws ParseException {
		Token t;
		switch ((this.jj_ntk == -1) ? jj_ntk() : this.jj_ntk) {
			case LDAP_OID:
				t = jj_consume_token(LDAP_OID);
				break;
			case ATTRIBUTE_TYPE_STRING:
				t = jj_consume_token(ATTRIBUTE_TYPE_STRING);
				break;
			default:
				this.jj_la1[5] = this.jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
		}
		return t.image;
	}

	public final String AttributeValue() throws ParseException {
		Token t;
		this.token_source.SwitchTo(ATTRVALUE_S);
		t = jj_consume_token(ATTRVALUE);
		this.token_source.SwitchTo(DEFAULT);
		return t.image;
	}

	/** Generated Token Manager. */
	public DnParserImplTokenManager token_source;

	SimpleCharStream jj_input_stream;

	/** Current token. */
	public Token token;

	/** Next token. */
	public Token jj_nt;

	private int jj_ntk;

	private int jj_gen;

	private final int[] jj_la1 = new int[6];

	private static int[] jj_la1_0;
	static {
		jj_la1_init_0();
	}

	private static void jj_la1_init_0() {
		jj_la1_0 = new int[] { 0x180000, 0x180000, 0x200000, 0x10000, 0x10000, 0xc000, };
	}

	/** Constructor with InputStream. */
	public DnParserImpl(java.io.InputStream stream) {
		this(stream, null);
	}

	/** Constructor with InputStream and supplied encoding */
	public DnParserImpl(java.io.InputStream stream, String encoding) {
		try {
			this.jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
		}
		catch (java.io.UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		this.token_source = new DnParserImplTokenManager(this.jj_input_stream);
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream) {
		ReInit(stream, null);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream, String encoding) {
		try {
			this.jj_input_stream.ReInit(stream, encoding, 1, 1);
		}
		catch (java.io.UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		this.token_source.ReInit(this.jj_input_stream);
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	/** Constructor. */
	public DnParserImpl(java.io.Reader stream) {
		this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
		this.token_source = new DnParserImplTokenManager(this.jj_input_stream);
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader stream) {
		this.jj_input_stream.ReInit(stream, 1, 1);
		this.token_source.ReInit(this.jj_input_stream);
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	/** Constructor with generated Token Manager. */
	public DnParserImpl(DnParserImplTokenManager tm) {
		this.token_source = tm;
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	/** Reinitialise. */
	public void ReInit(DnParserImplTokenManager tm) {
		this.token_source = tm;
		this.token = new Token();
		this.jj_ntk = -1;
		this.jj_gen = 0;
		for (int i = 0; i < 6; i++) {
			this.jj_la1[i] = -1;
		}
	}

	private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken = this.token;
		if (oldToken.next != null) {
			this.token = this.token.next;
		}
		else {
			this.token.next = this.token_source.getNextToken();
			this.token = this.token.next;
		}

		this.jj_ntk = -1;
		if (this.token.kind == kind) {
			this.jj_gen++;
			return this.token;
		}
		this.token = oldToken;
		this.jj_kind = kind;
		throw generateParseException();
	}

	/** Get the next Token. */
	public final Token getNextToken() {
		if (this.token.next != null) {
			this.token = this.token.next;
		}
		else {
			this.token.next = this.token_source.getNextToken();
			this.token = this.token.next;
		}
		this.jj_ntk = -1;
		this.jj_gen++;
		return this.token;
	}

	/** Get the specific Token. */
	public final Token getToken(int index) {
		Token t = this.token;
		for (int i = 0; i < index; i++) {
			if (t.next != null) {
				t = t.next;
			}
			else {
				t.next = this.token_source.getNextToken();
				t = t.next;
			}
		}
		return t;
	}

	private int jj_ntk() {
		this.jj_nt = this.token.next;
		if (this.jj_nt == null) {
			this.token.next = this.token_source.getNextToken();
			this.jj_ntk = this.token.next.kind;
			return this.jj_ntk;
		}
		else {
			this.jj_ntk = this.jj_nt.kind;
			return this.jj_ntk;
		}
	}

	private java.util.List jj_expentries = new java.util.ArrayList();

	private int[] jj_expentry;

	private int jj_kind = -1;

	/** Generate ParseException. */
	public ParseException generateParseException() {
		this.jj_expentries.clear();
		boolean[] la1tokens = new boolean[22];
		if (this.jj_kind >= 0) {
			la1tokens[this.jj_kind] = true;
			this.jj_kind = -1;
		}
		for (int i = 0; i < 6; i++) {
			if (this.jj_la1[i] == this.jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 22; i++) {
			if (la1tokens[i]) {
				this.jj_expentry = new int[1];
				this.jj_expentry[0] = i;
				this.jj_expentries.add(this.jj_expentry);
			}
		}
		int[][] exptokseq = new int[this.jj_expentries.size()][];
		for (int i = 0; i < this.jj_expentries.size(); i++) {
			exptokseq[i] = (int[]) this.jj_expentries.get(i);
		}
		return new ParseException(this.token, exptokseq, tokenImage);
	}

	/** Enable tracing. */
	public final void enable_tracing() {
	}

	/** Disable tracing. */
	public final void disable_tracing() {
	}

}
