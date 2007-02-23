package org.springframework.ldap.pool;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextSource;

/**
 * An enum representing the two types of {@link DirContext}s that can be returned by a
 * {@link ContextSource}.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public final class DirContextType {
    private String name;

    private DirContextType(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
    
    /**
     * The type of {@link DirContext} returned by {@link ContextSource#getReadOnlyContext()}
     */
    public static final DirContextType READ_ONLY = new DirContextType("READ_ONLY");
    
    /**
     * The type of {@link DirContext} returned by {@link ContextSource#getReadWriteContext()}
     */
    public static final DirContextType READ_WRITE = new DirContextType("READ_WRITE");
}
