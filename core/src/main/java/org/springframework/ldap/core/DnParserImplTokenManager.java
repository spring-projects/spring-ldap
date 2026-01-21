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
public class DnParserImplTokenManager implements DnParserImplConstants {

	/** Debug output. */
	public java.io.PrintStream debugStream = System.out;

	/** Set debug output. */
	public void setDebugStream(java.io.PrintStream ds) {
		this.debugStream = ds;
	}

	private int jjMoveStringLiteralDfa0_1() {
		return jjMoveNfa_1(0, 0);
	}

	static final long[] jjbitVec0 = { 0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL,
			0xffffffffffffffffL };
	static final long[] jjbitVec2 = { 0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL };

	private int jjMoveNfa_1(int startState, int curPos) {
		int startsAt = 0;
		this.jjnewStateCnt = 17;
		int i = 1;
		this.jjstateSet[0] = startState;
		int kind = 0x7fffffff;
		for (;;) {
			if (++this.jjround == 0x7fffffff) {
				ReInitRounds();
			}
			if (this.curChar < 64) {
				long l = 1L << this.curChar;
				do {
					switch (this.jjstateSet[--i]) {
						case 0:
							if ((0xa7ffe7f2ffffffffL & l) != 0L) {
								if (kind > 17) {
									kind = 17;
								}
								jjCheckNAddStates(0, 2);
							}
							else if (this.curChar == 35) {
								jjCheckNAdd(1);
							}
							break;
						case 1:
							if ((0x3ff000000000000L & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 2;
							}
							break;
						case 2:
							if ((0x3ff000000000000L & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAdd(1);
							break;
						case 3:
							if ((0xa7ffe7f2ffffffffL & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						case 4:
							if ((0xa7ffe7fbffffffffL & l) != 0L) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 5:
							if ((0xa7ffe7faffffffffL & l) != 0L && kind > 17) {
								kind = 17;
							}
							break;
						case 7:
							if ((0x7800180d00002000L & l) != 0L) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 8:
							if ((0x3ff000000000000L & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 9;
							}
							break;
						case 9:
							if ((0x3ff000000000000L & l) != 0L) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 10:
							if ((0x7800180d00002000L & l) != 0L && kind > 17) {
								kind = 17;
							}
							break;
						case 11:
							if ((0x3ff000000000000L & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 12;
							}
							break;
						case 12:
							if ((0x3ff000000000000L & l) != 0L && kind > 17) {
								kind = 17;
							}
							break;
						case 14:
							if ((0x7800180d00002000L & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						case 15:
							if ((0x3ff000000000000L & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 16;
							}
							break;
						case 16:
							if ((0x3ff000000000000L & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else if (this.curChar < 128) {
				long l = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						case 0:
							if ((0xffffffffefffffffL & l) != 0L) {
								if (kind > 17) {
									kind = 17;
								}
								jjCheckNAddStates(0, 2);
							}
							else if (this.curChar == 92) {
								jjAddStates(3, 4);
							}
							break;
						case 1:
							if ((0x7e0000007eL & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 2;
							}
							break;
						case 2:
							if ((0x7e0000007eL & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							this.jjstateSet[this.jjnewStateCnt++] = 1;
							break;
						case 3:
							if ((0xffffffffefffffffL & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						case 4:
							if ((0xffffffffefffffffL & l) != 0L) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 5:
							if ((0xffffffffefffffffL & l) != 0L && kind > 17) {
								kind = 17;
							}
							break;
						case 6:
							if (this.curChar == 92) {
								jjAddStates(5, 8);
							}
							break;
						case 7:
							if (this.curChar == 92) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 8:
							if ((0x7e0000007eL & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 9;
							}
							break;
						case 9:
							if ((0x7e0000007eL & l) != 0L) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 10:
							if (this.curChar == 92 && kind > 17) {
								kind = 17;
							}
							break;
						case 11:
							if ((0x7e0000007eL & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 12;
							}
							break;
						case 12:
							if ((0x7e0000007eL & l) != 0L && kind > 17) {
								kind = 17;
							}
							break;
						case 13:
							if (this.curChar == 92) {
								jjAddStates(3, 4);
							}
							break;
						case 14:
							if (this.curChar != 92) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						case 15:
							if ((0x7e0000007eL & l) != 0L) {
								this.jjstateSet[this.jjnewStateCnt++] = 16;
							}
							break;
						case 16:
							if ((0x7e0000007eL & l) == 0L) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else {
				int hiByte = (int) (this.curChar >> 8);
				int i1 = hiByte >> 6;
				long l1 = 1L << (hiByte & 077);
				int i2 = (this.curChar & 0xff) >> 6;
				long l2 = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						case 0:
							if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
								break;
							}
							if (kind > 17) {
								kind = 17;
							}
							jjCheckNAddStates(0, 2);
							break;
						case 4:
							if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
								jjCheckNAddStates(0, 2);
							}
							break;
						case 5:
							if (jjCanMove_0(hiByte, i1, i2, l1, l2) && kind > 17) {
								kind = 17;
							}
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			if (kind != 0x7fffffff) {
				this.jjmatchedKind = kind;
				this.jjmatchedPos = curPos;
				kind = 0x7fffffff;
			}
			++curPos;
			i = this.jjnewStateCnt;
			this.jjnewStateCnt = startsAt;
			startsAt = 17 - this.jjnewStateCnt;
			if (i == startsAt) {
				return curPos;
			}
			try {
				this.curChar = this.input_stream.readChar();
			}
			catch (java.io.IOException ex) {
				return curPos;
			}
		}
	}

	private int jjStopStringLiteralDfa_0(int pos, long active0) {
		switch (pos) {
			default:
				return -1;
		}
	}

	private int jjStartNfa_0(int pos, long active0) {
		return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
	}

	private int jjStopAtPos(int pos, int kind) {
		this.jjmatchedKind = kind;
		this.jjmatchedPos = pos;
		return pos + 1;
	}

	private int jjMoveStringLiteralDfa0_0() {
		switch (this.curChar) {
			case 32:
				return jjStopAtPos(0, 16);
			case 34:
				return jjStopAtPos(0, 12);
			case 35:
				return jjStopAtPos(0, 13);
			case 43:
				return jjStopAtPos(0, 21);
			case 44:
				return jjStopAtPos(0, 19);
			case 59:
				return jjStopAtPos(0, 20);
			default:
				return jjMoveNfa_0(0, 0);
		}
	}

	private int jjMoveNfa_0(int startState, int curPos) {
		int startsAt = 0;
		this.jjnewStateCnt = 5;
		int i = 1;
		this.jjstateSet[0] = startState;
		int kind = 0x7fffffff;
		for (;;) {
			if (++this.jjround == 0x7fffffff) {
				ReInitRounds();
			}
			if (this.curChar < 64) {
				long l = 1L << this.curChar;
				do {
					switch (this.jjstateSet[--i]) {
						case 0:
						case 2:
							if ((0x3ff000000000000L & l) == 0L) {
								break;
							}
							if (kind > 15) {
								kind = 15;
							}
							jjCheckNAddTwoStates(2, 3);
							break;
						case 1:
							if ((0x3ff200000000000L & l) == 0L) {
								break;
							}
							if (kind > 14) {
								kind = 14;
							}
							this.jjstateSet[this.jjnewStateCnt++] = 1;
							break;
						case 3:
							if (this.curChar == 46) {
								jjCheckNAdd(4);
							}
							break;
						case 4:
							if ((0x3ff000000000000L & l) == 0L) {
								break;
							}
							if (kind > 15) {
								kind = 15;
							}
							jjCheckNAddTwoStates(3, 4);
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else if (this.curChar < 128) {
				long l = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						case 0:
						case 1:
							if ((0x7fffffe07fffffeL & l) == 0L) {
								break;
							}
							if (kind > 14) {
								kind = 14;
							}
							jjCheckNAdd(1);
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else {
				int hiByte = (int) (this.curChar >> 8);
				int i1 = hiByte >> 6;
				long l1 = 1L << (hiByte & 077);
				int i2 = (this.curChar & 0xff) >> 6;
				long l2 = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			if (kind != 0x7fffffff) {
				this.jjmatchedKind = kind;
				this.jjmatchedPos = curPos;
				kind = 0x7fffffff;
			}
			++curPos;
			i = this.jjnewStateCnt;
			this.jjnewStateCnt = startsAt;
			startsAt = 5 - this.jjnewStateCnt;
			if (i == startsAt) {
				return curPos;
			}
			try {
				this.curChar = this.input_stream.readChar();
			}
			catch (java.io.IOException ex) {
				return curPos;
			}
		}
	}

	private int jjMoveStringLiteralDfa0_2() {
		return jjMoveNfa_2(3, 0);
	}

	private int jjMoveNfa_2(int startState, int curPos) {
		int startsAt = 0;
		this.jjnewStateCnt = 3;
		int i = 1;
		this.jjstateSet[0] = startState;
		int kind = 0x7fffffff;
		for (;;) {
			if (++this.jjround == 0x7fffffff) {
				ReInitRounds();
			}
			if (this.curChar < 64) {
				long l = 1L << this.curChar;
				do {
					switch (this.jjstateSet[--i]) {
						case 3:
							if (this.curChar == 61) {
								if (kind > 18) {
									kind = 18;
								}
								jjCheckNAdd(2);
							}
							else if (this.curChar == 32) {
								jjCheckNAddTwoStates(0, 1);
							}
							break;
						case 0:
							if (this.curChar == 32) {
								jjCheckNAddTwoStates(0, 1);
							}
							break;
						case 1:
							if (this.curChar != 61) {
								break;
							}
							kind = 18;
							jjCheckNAdd(2);
							break;
						case 2:
							if (this.curChar != 32) {
								break;
							}
							if (kind > 18) {
								kind = 18;
							}
							jjCheckNAdd(2);
							break;
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else if (this.curChar < 128) {
				long l = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			else {
				int hiByte = (int) (this.curChar >> 8);
				int i1 = hiByte >> 6;
				long l1 = 1L << (hiByte & 077);
				int i2 = (this.curChar & 0xff) >> 6;
				long l2 = 1L << (this.curChar & 077);
				do {
					switch (this.jjstateSet[--i]) {
						default:
							break;
					}
				}
				while (i != startsAt);
			}
			if (kind != 0x7fffffff) {
				this.jjmatchedKind = kind;
				this.jjmatchedPos = curPos;
				kind = 0x7fffffff;
			}
			++curPos;
			i = this.jjnewStateCnt;
			this.jjnewStateCnt = startsAt;
			startsAt = 3 - this.jjnewStateCnt;
			if (i == startsAt) {
				return curPos;
			}
			try {
				this.curChar = this.input_stream.readChar();
			}
			catch (java.io.IOException ex) {
				return curPos;
			}
		}
	}

	static final int[] jjnextStates = { 4, 5, 6, 14, 15, 7, 8, 10, 11, };

	private static boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2) {
		switch (hiByte) {
			case 0:
				return ((jjbitVec2[i2] & l2) != 0L);
			default:
				if ((jjbitVec0[i1] & l1) != 0L) {
					return true;
				}
				return false;
		}
	}

	/** Token literal values. */
	public static final String[] jjstrLiteralImages = { "", null, null, null, null, null, null, null, null, null, null,
			null, "\42", "\43", null, null, "\40", null, null, "\54", "\73", "\53", };

	/** Lexer state names. */
	public static final String[] lexStateNames = { "DEFAULT", "ATTRVALUE_S", "SPACED_EQUALS_S", };

	/** Lex State array. */
	public static final int[] jjnewLexState = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, };

	protected SimpleCharStream input_stream;

	private final int[] jjrounds = new int[17];

	private final int[] jjstateSet = new int[34];

	protected char curChar;

	/** Constructor. */
	public DnParserImplTokenManager(SimpleCharStream stream) {
		if (SimpleCharStream.staticFlag) {
			throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
		}
		this.input_stream = stream;
	}

	/** Constructor. */
	public DnParserImplTokenManager(SimpleCharStream stream, int lexState) {
		this(stream);
		SwitchTo(lexState);
	}

	/** Reinitialise parser. */
	public void ReInit(SimpleCharStream stream) {
		this.jjmatchedPos = 0;
		this.jjnewStateCnt = 0;
		this.curLexState = this.defaultLexState;
		this.input_stream = stream;
		ReInitRounds();
	}

	private void ReInitRounds() {
		int i;
		this.jjround = 0x80000001;
		for (i = 17; i-- > 0;) {
			this.jjrounds[i] = 0x80000000;
		}
	}

	/** Reinitialise parser. */
	public void ReInit(SimpleCharStream stream, int lexState) {
		ReInit(stream);
		SwitchTo(lexState);
	}

	/** Switch to specified lex state. */
	public void SwitchTo(int lexState) {
		if (lexState >= 3 || lexState < 0) {
			throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.",
					TokenMgrError.INVALID_LEXICAL_STATE);
		}
		else {
			this.curLexState = lexState;
		}
	}

	protected Token jjFillToken() {
		final Token t;
		final String curTokenImage;
		final int beginLine;
		final int endLine;
		final int beginColumn;
		final int endColumn;
		String im = jjstrLiteralImages[this.jjmatchedKind];
		curTokenImage = (im != null) ? im : this.input_stream.GetImage();
		beginLine = this.input_stream.getBeginLine();
		beginColumn = this.input_stream.getBeginColumn();
		endLine = this.input_stream.getEndLine();
		endColumn = this.input_stream.getEndColumn();
		t = Token.newToken(this.jjmatchedKind, curTokenImage);

		t.beginLine = beginLine;
		t.endLine = endLine;
		t.beginColumn = beginColumn;
		t.endColumn = endColumn;

		return t;
	}

	int curLexState = 0;

	int defaultLexState = 0;

	int jjnewStateCnt;

	int jjround;

	int jjmatchedPos;

	int jjmatchedKind;

	/** Get the next Token. */
	public Token getNextToken() {
		Token matchedToken;
		int curPos = 0;

		EOFLoop: for (;;) {
			try {
				this.curChar = this.input_stream.BeginToken();
			}
			catch (java.io.IOException ex) {
				this.jjmatchedKind = 0;
				matchedToken = jjFillToken();
				return matchedToken;
			}

			switch (this.curLexState) {
				case 0:
					this.jjmatchedKind = 0x7fffffff;
					this.jjmatchedPos = 0;
					curPos = jjMoveStringLiteralDfa0_0();
					break;
				case 1:
					this.jjmatchedKind = 0x7fffffff;
					this.jjmatchedPos = 0;
					curPos = jjMoveStringLiteralDfa0_1();
					break;
				case 2:
					this.jjmatchedKind = 0x7fffffff;
					this.jjmatchedPos = 0;
					curPos = jjMoveStringLiteralDfa0_2();
					break;
			}
			if (this.jjmatchedKind != 0x7fffffff) {
				if (this.jjmatchedPos + 1 < curPos) {
					this.input_stream.backup(curPos - this.jjmatchedPos - 1);
				}
				matchedToken = jjFillToken();
				if (jjnewLexState[this.jjmatchedKind] != -1) {
					this.curLexState = jjnewLexState[this.jjmatchedKind];
				}
				return matchedToken;
			}
			int error_line = this.input_stream.getEndLine();
			int error_column = this.input_stream.getEndColumn();
			String error_after = null;
			boolean EOFSeen = false;
			try {
				this.input_stream.readChar();
				this.input_stream.backup(1);
			}
			catch (java.io.IOException e1) {
				EOFSeen = true;
				error_after = (curPos <= 1) ? "" : this.input_stream.GetImage();
				if (this.curChar == '\n' || this.curChar == '\r') {
					error_line++;
					error_column = 0;
				}
				else {
					error_column++;
				}
			}
			if (!EOFSeen) {
				this.input_stream.backup(1);
				error_after = (curPos <= 1) ? "" : this.input_stream.GetImage();
			}
			throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar,
					TokenMgrError.LEXICAL_ERROR);
		}
	}

	private void jjCheckNAdd(int state) {
		if (this.jjrounds[state] != this.jjround) {
			this.jjstateSet[this.jjnewStateCnt++] = state;
			this.jjrounds[state] = this.jjround;
		}
	}

	private void jjAddStates(int start, int end) {
		do {
			this.jjstateSet[this.jjnewStateCnt++] = jjnextStates[start];
		}
		while (start++ != end);
	}

	private void jjCheckNAddTwoStates(int state1, int state2) {
		jjCheckNAdd(state1);
		jjCheckNAdd(state2);
	}

	private void jjCheckNAddStates(int start, int end) {
		do {
			jjCheckNAdd(jjnextStates[start]);
		}
		while (start++ != end);
	}

}
