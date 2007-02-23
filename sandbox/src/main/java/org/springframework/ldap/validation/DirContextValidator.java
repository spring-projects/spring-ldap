package org.springframework.ldap.validation;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.pool.DirContextType;

/**
 * A validator for {@link DirContext}s.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public interface DirContextValidator {
    /**
     * Validates the {@link DirContext}. A valid {@link DirContext} should be able
     * to answer queries and if applicable write to the directory.
     * 
     * @param contextType The type of the {@link DirContext}, refers to if {@link ContextSource#getReadOnlyContext()} or {@link ContextSource#getReadWriteContext()} was called to create the {@link DirContext}
     * @param dirContext The {@link DirContext} to validate.
     * @return <code>true</code> if the {@link DirContext} operated correctly during validation.
     */
    public boolean validateDirContext(DirContextType contextType, DirContext dirContext);
}
