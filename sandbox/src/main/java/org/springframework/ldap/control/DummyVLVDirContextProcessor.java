package org.springframework.ldap.control;

import org.springframework.ldap.core.support.AggregateDirContextProcessor;

public class DummyVLVDirContextProcessor extends AggregateDirContextProcessor {

    private VirtualListViewControlDirContextProcessor vlvProcessor;

    private SortControlDirContextProcessor sortControlProcessor;

    public DummyVLVDirContextProcessor(String sortKey, int pageSize) {
    }
}
