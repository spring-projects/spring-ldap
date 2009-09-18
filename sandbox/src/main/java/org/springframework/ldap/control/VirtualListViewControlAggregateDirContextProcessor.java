/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.control;

import org.springframework.ldap.core.support.AggregateDirContextProcessor;

/**
 * AggregateDirContextProcessor implementation for managing a virtual list view
 * by aggregating DirContextProcessor implementations for a VirtualListViewControl
 * and its required companion SortControl.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 * @author Marius Scurtescu
 */
public class VirtualListViewControlAggregateDirContextProcessor extends AggregateDirContextProcessor
{
    private SortControlDirContextProcessor _sortControlDirContextProcessor;
    private VirtualListViewControlDirContextProcessor _virtualListViewControlDirContextProcessor;

    public VirtualListViewControlAggregateDirContextProcessor(String sortKey, int pageSize)
    {
        this(
            new SortControlDirContextProcessor(sortKey),
            new VirtualListViewControlDirContextProcessor(pageSize)
        );
    }

    public VirtualListViewControlAggregateDirContextProcessor(String sortKey, int pageSize, int targetOffset, int listSize, VirtualListViewResultsCookie cookie)
    {
        this(
            new SortControlDirContextProcessor(sortKey),
            new VirtualListViewControlDirContextProcessor(pageSize, targetOffset, listSize, cookie)
        );
    }

    public VirtualListViewControlAggregateDirContextProcessor(SortControlDirContextProcessor sortControlDirContextProcessor, VirtualListViewControlDirContextProcessor virtualListViewControlDirContextProcessor)
    {
        _sortControlDirContextProcessor = sortControlDirContextProcessor;
        _virtualListViewControlDirContextProcessor = virtualListViewControlDirContextProcessor;

        addDirContextProcessor(sortControlDirContextProcessor);
        addDirContextProcessor(virtualListViewControlDirContextProcessor);
    }

    public VirtualListViewResultsCookie getCookie() {
        return _virtualListViewControlDirContextProcessor.getCookie();
    }
}
