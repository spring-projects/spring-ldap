package org.springframework.ldap.support;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.DirContextProcessor;

/**
 * Manages a sequence of DirContextProcessor instances. Applies
 * {@link #preProcess(DirContext)} and {@link #postProcess(DirContext)}
 * respectively in sequence on the managed objects.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class AggregateDirContextProcessor implements DirContextProcessor {

    private List dirContextProcessors = new LinkedList();

    /**
     * Add the supplied DirContextProcessor to the list of managed objects.
     * 
     * @param processor
     *            the DirContextpProcessor to add.
     */
    public void addDirContextProcessor(DirContextProcessor processor) {
        dirContextProcessors.add(processor);
    }

    /**
     * Get the list of managed DirContextProcessors.
     * 
     * @return the managed list of {@link DirContextProcessor} instances.
     */
    public List getDirContextProcessors() {
        return dirContextProcessors;
    }

    /**
     * Set the list of managed {@link DirContextProcessor} instances.
     * 
     * @param dirContextProcessors
     *            the list of {@link DirContextProcessor} instances to set.
     */
    public void setDirContextProcessors(List dirContextProcessors) {
        this.dirContextProcessors = dirContextProcessors;
    }

    /*
     * @see org.springframework.ldap.core.DirContextProcessor#preProcess(javax.naming.directory.DirContext)
     */
    public void preProcess(DirContext ctx) throws NamingException {
        for (Iterator iter = dirContextProcessors.iterator(); iter.hasNext();) {
            DirContextProcessor processor = (DirContextProcessor) iter.next();
            processor.preProcess(ctx);
        }
    }

    /*
     * @see org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming.directory.DirContext)
     */
    public void postProcess(DirContext ctx) throws NamingException {
        for (Iterator iter = dirContextProcessors.iterator(); iter.hasNext();) {
            DirContextProcessor processor = (DirContextProcessor) iter.next();
            processor.postProcess(ctx);
        }
    }
}
