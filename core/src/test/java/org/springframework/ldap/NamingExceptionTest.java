/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.directory.InitialDirContext;

import junit.framework.TestCase;

/**
 * Unit tests for the NamingException class.
 * 
 * @author Ulrik Sandberg
 */
public class NamingExceptionTest extends TestCase {
    private ByteArrayOutputStream byteArrayOutputStream;

    public void testNamingExceptionWithNonSerializableResolvedObj()
            throws Exception {
        javax.naming.NameAlreadyBoundException wrappedException = new javax.naming.NameAlreadyBoundException(
                "some error");
        wrappedException.setResolvedObj(new InitialDirContext());
        NamingException exception = new NameAlreadyBoundException(
                wrappedException);
        writeToStream(exception);
        NamingException deSerializedException = readFromStream();
        assertNotNull(
                "Original exception resolvedObj after serialization should not be null",
                exception.getResolvedObj());
        assertNull("De-serialized exception resolvedObj should be null",
                deSerializedException.getResolvedObj());
    }

    private NamingException readFromStream() throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
        NamingException deSerializedException;
        try {
            deSerializedException = (NamingException) in.readObject();
        } finally {
            in.close();
        }
        return deSerializedException;
    }

    private void writeToStream(NamingException exception) throws IOException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
        try {
            out.writeObject(exception);
            out.flush();
        } finally {
            out.close();
        }
    }
}
