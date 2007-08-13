/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;
import org.springframework.ldap.NameNotFoundException;

import javax.naming.Name;
import java.beans.PropertyEditorSupport;

/**
 * ReferencedEntryEditor is responsible for converting references in an object
 * directory map from distinguished name strings to the target type and vice versa.
 */
public class ReferencedEntryEditor extends PropertyEditorSupport
{
    private SavePolicy savePolicy;
    private LoadPolicy loadPolicy;
    private DistinguishedName base;
    private LdapTemplate ldapTemplate;
    private ObjectDirectoryMapper objectDirectoryMapper;

    public ReferencedEntryEditor(SavePolicy savePolicy,
                                 LoadPolicy loadPolicy,
                                 DistinguishedName baseDn,
                                 LdapTemplate ldapTemplate,
                                 ObjectDirectoryMapper objectDirectoryMapper)
    {
        this.savePolicy = savePolicy;
        this.loadPolicy = loadPolicy;
        this.base = baseDn;
        this.ldapTemplate = ldapTemplate;
        this.objectDirectoryMapper = objectDirectoryMapper;
    }

    /**
     * Builds a distinguished name from the instance value
     */
    public String getAsText()
    {
        try
        {
            DistinguishedName refDn = (DistinguishedName) objectDirectoryMapper.buildDn(getValue());
            doCreateAndUpdatePolicies(refDn);
            DistinguishedName fullDn = (DistinguishedName) base.clone();
            fullDn.append(refDn);
            return fullDn.toString();
        }
        catch (MappingException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    //TODO: This looks inefficient
    private void doCreateAndUpdatePolicies(Name referenceDn)
    {
        if (savePolicy == SavePolicy.CREATE || savePolicy == SavePolicy.CREATE_OR_UPDATE)
        {
            try
            {
                ldapTemplate.lookup(referenceDn, objectDirectoryMapper);
            }
            catch (NameNotFoundException e)
            {
                DirContextAdapter contextAdapter = new DirContextAdapter();
                objectDirectoryMapper.mapToContext(getValue(), contextAdapter);
                ldapTemplate.bind(referenceDn, contextAdapter, null);
            }
        }
        if (savePolicy == SavePolicy.CREATE_OR_UPDATE)
        {
            DirContextAdapter contextAdapter = (DirContextAdapter) ldapTemplate.lookup(referenceDn);
            objectDirectoryMapper.mapToContext(getValue(), contextAdapter);
            ldapTemplate.modifyAttributes(referenceDn, contextAdapter.getModificationItems());
        }
    }

    /**
     * Sets the value of the editor by performing a lookup and mapping the result
     * to the target type using an object directory mapper.
     *
     * @param text The distinguished name of an ldap entry.
     * @throws IllegalArgumentException
     */
    public void setAsText(String text) throws IllegalArgumentException
    {
        DistinguishedName dn = new DistinguishedName(text);
        if (dn.startsWith(base))
        {
            for (int i = 0; i < base.size(); i++)
            {
                dn.removeFirst();
            }
        }
        try
        {
            setValue(ldapTemplate.lookup(dn, objectDirectoryMapper));
        }
        catch (NameNotFoundException e)
        {
            if (loadPolicy == LoadPolicy.SUPPRESS_REFERENTIAL_INTEGRITY_EXCEPTIONS)
            {
                setValue(null);
            }
            else
            {
                throw new ReferentialIntegrityException("The referenced entry: "
                        + text + " does not exist", e);
            }
        }
    }
}
