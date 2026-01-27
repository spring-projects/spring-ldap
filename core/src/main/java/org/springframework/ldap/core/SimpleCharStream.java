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
 * An implementation of interface CharStream, where the stream is assumed to contain only
 * ASCII characters (without unicode processing).
 *
 * @deprecated As of 2.0 it is recommended to use {@link javax.naming.ldap.LdapName} along
 * with utility methods in {@link LdapUtils} instead.
 */
@Deprecated
@NullUnmarked
public class SimpleCharStream {

	/** Whether parser is static. */
	public static final boolean staticFlag = false;

	int bufsize;

	int available;

	int tokenBegin;

	/** Position in buffer. */
	public int bufpos = -1;

	protected int[] bufline;

	protected int[] bufcolumn;

	protected int column = 0;

	protected int line = 1;

	protected boolean prevCharIsCR = false;

	protected boolean prevCharIsLF = false;

	protected java.io.Reader inputStream;

	protected char[] buffer;

	protected int maxNextCharInd = 0;

	protected int inBuf = 0;

	protected int tabSize = 8;

	protected void setTabSize(int i) {
		this.tabSize = i;
	}

	protected int getTabSize(int i) {
		return this.tabSize;
	}

	protected void ExpandBuff(boolean wrapAround) {
		char[] newbuffer = new char[this.bufsize + 2048];
		int[] newbufline = new int[this.bufsize + 2048];
		int[] newbufcolumn = new int[this.bufsize + 2048];

		try {
			if (wrapAround) {
				System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
				System.arraycopy(this.buffer, 0, newbuffer, this.bufsize - this.tokenBegin, this.bufpos);
				this.buffer = newbuffer;

				System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
				System.arraycopy(this.bufline, 0, newbufline, this.bufsize - this.tokenBegin, this.bufpos);
				this.bufline = newbufline;

				System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
				System.arraycopy(this.bufcolumn, 0, newbufcolumn, this.bufsize - this.tokenBegin, this.bufpos);
				this.bufcolumn = newbufcolumn;

				this.bufpos += (this.bufsize - this.tokenBegin);
				this.maxNextCharInd = this.bufpos;
			}
			else {
				System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
				this.buffer = newbuffer;

				System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
				this.bufline = newbufline;

				System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
				this.bufcolumn = newbufcolumn;

				this.bufpos -= this.tokenBegin;
				this.maxNextCharInd = this.bufpos;
			}
		}
		catch (Throwable ex) {
			throw new Error(ex.getMessage());
		}

		this.bufsize += 2048;
		this.available = this.bufsize;
		this.tokenBegin = 0;
	}

	protected void FillBuff() throws java.io.IOException {
		if (this.maxNextCharInd == this.available) {
			if (this.available == this.bufsize) {
				if (this.tokenBegin > 2048) {
					this.bufpos = 0;
					this.maxNextCharInd = 0;
					this.available = this.tokenBegin;
				}
				else if (this.tokenBegin < 0) {
					this.bufpos = 0;
					this.maxNextCharInd = 0;
				}
				else {
					ExpandBuff(false);
				}
			}
			else if (this.available > this.tokenBegin) {
				this.available = this.bufsize;
			}
			else if ((this.tokenBegin - this.available) < 2048) {
				ExpandBuff(true);
			}
			else {
				this.available = this.tokenBegin;
			}
		}

		int i;
		try {
			i = this.inputStream.read(this.buffer, this.maxNextCharInd, this.available - this.maxNextCharInd);
			if (i == -1) {
				this.inputStream.close();
				throw new java.io.IOException();
			}
			else {
				this.maxNextCharInd += i;
			}
		}
		catch (java.io.IOException ex) {
			--this.bufpos;
			backup(0);
			if (this.tokenBegin == -1) {
				this.tokenBegin = this.bufpos;
			}
			throw ex;
		}
	}

	/** Start. */
	public char BeginToken() throws java.io.IOException {
		this.tokenBegin = -1;
		char c = readChar();
		this.tokenBegin = this.bufpos;

		return c;
	}

	protected void UpdateLineColumn(char c) {
		this.column++;

		if (this.prevCharIsLF) {
			this.prevCharIsLF = false;
			this.column = 1;
			this.line += this.column;
		}
		else if (this.prevCharIsCR) {
			this.prevCharIsCR = false;
			if (c == '\n') {
				this.prevCharIsLF = true;
			}
			else {
				this.column = 1;
				this.line += this.column;
			}
		}

		switch (c) {
			case '\r':
				this.prevCharIsCR = true;
				break;
			case '\n':
				this.prevCharIsLF = true;
				break;
			case '\t':
				this.column--;
				this.column += (this.tabSize - (this.column % this.tabSize));
				break;
			default:
				break;
		}

		this.bufline[this.bufpos] = this.line;
		this.bufcolumn[this.bufpos] = this.column;
	}

	/** Read a character. */
	public char readChar() throws java.io.IOException {
		if (this.inBuf > 0) {
			--this.inBuf;

			if (++this.bufpos == this.bufsize) {
				this.bufpos = 0;
			}

			return this.buffer[this.bufpos];
		}

		if (++this.bufpos >= this.maxNextCharInd) {
			FillBuff();
		}

		char c = this.buffer[this.bufpos];

		UpdateLineColumn(c);
		return c;
	}

	/**
	 * @deprecated
	 * @see #getEndColumn
	 */
	@Deprecated
	public int getColumn() {
		return this.bufcolumn[this.bufpos];
	}

	/**
	 * @deprecated
	 * @see #getEndLine
	 */
	@Deprecated
	public int getLine() {
		return this.bufline[this.bufpos];
	}

	/** Get token end column number. */
	public int getEndColumn() {
		return this.bufcolumn[this.bufpos];
	}

	/** Get token end line number. */
	public int getEndLine() {
		return this.bufline[this.bufpos];
	}

	/** Get token beginning column number. */
	public int getBeginColumn() {
		return this.bufcolumn[this.tokenBegin];
	}

	/** Get token beginning line number. */
	public int getBeginLine() {
		return this.bufline[this.tokenBegin];
	}

	/** Backup a number of characters. */
	public void backup(int amount) {

		this.inBuf += amount;
		this.bufpos -= amount;
		if (this.bufpos < 0) {
			this.bufpos += this.bufsize;
		}
	}

	/** Constructor. */
	public SimpleCharStream(java.io.Reader dstream, int startline, int startcolumn, int buffersize) {
		this.inputStream = dstream;
		this.line = startline;
		this.column = startcolumn - 1;

		this.available = buffersize;
		this.bufsize = buffersize;
		this.buffer = new char[buffersize];
		this.bufline = new int[buffersize];
		this.bufcolumn = new int[buffersize];
	}

	/** Constructor. */
	public SimpleCharStream(java.io.Reader dstream, int startline, int startcolumn) {
		this(dstream, startline, startcolumn, 4096);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.Reader dstream) {
		this(dstream, 1, 1, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader dstream, int startline, int startcolumn, int buffersize) {
		this.inputStream = dstream;
		this.line = startline;
		this.column = startcolumn - 1;

		if (this.buffer == null || buffersize != this.buffer.length) {
			this.available = buffersize;
			this.bufsize = buffersize;
			this.buffer = new char[buffersize];
			this.bufline = new int[buffersize];
			this.bufcolumn = new int[buffersize];
		}
		this.prevCharIsLF = false;
		this.prevCharIsCR = false;
		this.tokenBegin = 0;
		this.inBuf = 0;
		this.maxNextCharInd = 0;
		this.bufpos = -1;
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader dstream, int startline, int startcolumn) {
		ReInit(dstream, startline, startcolumn, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader dstream) {
		ReInit(dstream, 1, 1, 4096);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline, int startcolumn,
			int buffersize) throws java.io.UnsupportedEncodingException {
		this((encoding != null) ? new java.io.InputStreamReader(dstream, encoding)
				: new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream, int startline, int startcolumn, int buffersize) {
		this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline, int startcolumn)
			throws java.io.UnsupportedEncodingException {
		this(dstream, encoding, startline, startcolumn, 4096);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream, int startline, int startcolumn) {
		this(dstream, startline, startcolumn, 4096);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException {
		this(dstream, encoding, 1, 1, 4096);
	}

	/** Constructor. */
	public SimpleCharStream(java.io.InputStream dstream) {
		this(dstream, 1, 1, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream, String encoding, int startline, int startcolumn, int buffersize)
			throws java.io.UnsupportedEncodingException {
		ReInit((encoding != null) ? new java.io.InputStreamReader(dstream, encoding)
				: new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream, int startline, int startcolumn, int buffersize) {
		ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException {
		ReInit(dstream, encoding, 1, 1, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream) {
		ReInit(dstream, 1, 1, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream, String encoding, int startline, int startcolumn)
			throws java.io.UnsupportedEncodingException {
		ReInit(dstream, encoding, startline, startcolumn, 4096);
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream dstream, int startline, int startcolumn) {
		ReInit(dstream, startline, startcolumn, 4096);
	}

	/** Get token literal value. */
	public String GetImage() {
		if (this.bufpos >= this.tokenBegin) {
			return new String(this.buffer, this.tokenBegin, this.bufpos - this.tokenBegin + 1);
		}
		else {
			return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin)
					+ new String(this.buffer, 0, this.bufpos + 1);
		}
	}

	/** Get the suffix. */
	public char[] GetSuffix(int len) {
		char[] ret = new char[len];

		if ((this.bufpos + 1) >= len) {
			System.arraycopy(this.buffer, this.bufpos - len + 1, ret, 0, len);
		}
		else {
			System.arraycopy(this.buffer, this.bufsize - (len - this.bufpos - 1), ret, 0, len - this.bufpos - 1);
			System.arraycopy(this.buffer, 0, ret, len - this.bufpos - 1, this.bufpos + 1);
		}

		return ret;
	}

	/** Reset buffer when finished. */
	public void Done() {
		this.buffer = null;
		this.bufline = null;
		this.bufcolumn = null;
	}

	/**
	 * Method to adjust line and column numbers for the start of a token.
	 */
	public void adjustBeginLineColumn(int newLine, int newCol) {
		int start = this.tokenBegin;
		int len;

		if (this.bufpos >= this.tokenBegin) {
			len = this.bufpos - this.tokenBegin + this.inBuf + 1;
		}
		else {
			len = this.bufsize - this.tokenBegin + this.bufpos + 1 + this.inBuf;
		}

		int i = 0;
		int j = 0;
		int k = 0;
		int nextColDiff = 0;
		int columnDiff = 0;

		j = start % this.bufsize;
		k = ++start % this.bufsize;
		while (i < len && this.bufline[j] == this.bufline[k]) {
			this.bufline[j] = newLine;
			nextColDiff = columnDiff + this.bufcolumn[k] - this.bufcolumn[j];
			this.bufcolumn[j] = newCol + columnDiff;
			columnDiff = nextColDiff;
			i++;
		}

		if (i < len) {
			this.bufline[j] = newLine++;
			this.bufcolumn[j] = newCol + columnDiff;

			while (i++ < len) {
				j = start % this.bufsize;
				k = ++start % this.bufsize;
				if (this.bufline[j] != this.bufline[k]) {
					this.bufline[j] = newLine++;
				}
				else {
					this.bufline[j] = newLine;
				}
			}
		}

		this.line = this.bufline[j];
		this.column = this.bufcolumn[j];
	}

}
/* JavaCC - OriginalChecksum=2246b7a585f64c4ea5972a13e0680b9e (do not edit this line) */
