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
package org.springframework.ldap.core.support;

import javax.naming.directory.SearchResult;

import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;


import junit.framework.TestCase;

public class CountNameClassPairResultCallbackHandlerTest extends TestCase {

    
    private CountNameClassPairCallbackHandler tested;

    protected void setUp() throws Exception {
        super.setUp();
        
        tested = new CountNameClassPairCallbackHandler();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        tested = null;
    }

    public void testHandleSearchResult() throws Exception {
        SearchResult dummy = new SearchResult(null, null, null);
        tested.handleNameClassPair(dummy);
        tested.handleNameClassPair(dummy);
        tested.handleNameClassPair(dummy);
        
        assertEquals(3, tested.getNoOfRows());
    }
    
}
