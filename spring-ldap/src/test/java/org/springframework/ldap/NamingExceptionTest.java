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
