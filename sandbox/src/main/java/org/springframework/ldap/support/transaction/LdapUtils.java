package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.springframework.ldap.support.DistinguishedName;
import org.springframework.util.Assert;

/**
 * Common helper methods for Ldap operations.
 * 
 * @author Mattias Arthursson
 */
public class LdapUtils {
    /**
     * Not to be instantiated.
     */
    private LdapUtils() {
    }

    /**
     * Get the first parameter in the argument list as a Name.
     * 
     * @param args
     *            arguments supplied to a ldap operation.
     * @return a Name representation of the first argument, or the Name itself
     *         if it is a name.
     */
    public static Name getFirstArgumentAsName(Object[] args) {
        Assert.notEmpty(args);

        if (args[0] instanceof String) {
            return new DistinguishedName((String) args[0]);
        } else if (args[0] instanceof Name) {
            return (Name) args[0];
        } else {
            throw new IllegalArgumentException(
                    "First argument needs to be a Name or a String representation thereof");
        }
    }

}
