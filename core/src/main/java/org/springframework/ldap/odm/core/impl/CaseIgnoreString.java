package org.springframework.ldap.odm.core.impl;

// A case independent String wrapper.
/* package */ final class CaseIgnoreString implements Comparable<CaseIgnoreString> {
    private final String string;
    private final int hashCode;  
    
    public CaseIgnoreString(String string) {
        if (string == null)
            throw new NullPointerException();
        this.string = string;
        hashCode = string.toUpperCase().hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof CaseIgnoreString &&
            ((CaseIgnoreString)other).string.equalsIgnoreCase(string);
    }
    
    public int hashCode() { 
        return hashCode;
    }

    public int compareTo(CaseIgnoreString other) {
        CaseIgnoreString cis = (CaseIgnoreString)other;
        return String.CASE_INSENSITIVE_ORDER.compare(string, cis.string);
    }

    public String toString() {
        return string;
    }
}
