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

import javax.naming.NamingException;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.support.AggregateDirContextProcessor;

public class AggregateDirContextProcessorTest extends TestCase {

    private MockControl processor1Control;

    private DirContextProcessor processor1Mock;

    private MockControl processor2Control;

    private DirContextProcessor processor2Mock;

    private AggregateDirContextProcessor tested;

    protected void setUp() throws Exception {
        super.setUp();

        // Create processor1 mock
        processor1Control = MockControl
                .createControl(DirContextProcessor.class);
        processor1Mock = (DirContextProcessor) processor1Control.getMock();

        // Create processor2 mock
        processor2Control = MockControl
                .createControl(DirContextProcessor.class);
        processor2Mock = (DirContextProcessor) processor2Control.getMock();

        tested = new AggregateDirContextProcessor();
        tested.addDirContextProcessor(processor1Mock);
        tested.addDirContextProcessor(processor2Mock);

    }

    protected void tearDown() throws Exception {
        super.tearDown();

        processor1Control = null;
        processor1Mock = null;

        processor2Control = null;
        processor2Mock = null;
    }

    protected void replay() {
        processor1Control.replay();
        processor2Control.replay();

    }

    protected void verify() {
        processor1Control.verify();
        processor2Control.verify();
    }

    public void testPreProcess() throws NamingException {
        processor1Mock.preProcess(null);
        processor2Mock.preProcess(null);
        
        replay();
        
        tested.preProcess(null);
        
        verify();
    }

    public void testPostProcess() throws NamingException {
        processor1Mock.postProcess(null);
        processor2Mock.postProcess(null);
        
        replay();
        
        tested.postProcess(null);
        
        verify();
    }

}
