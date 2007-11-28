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

package org.springframework.ldap.pool;


/**
 * An enum representing the two types of {@link DirContext}s that can be returned by a
 * {@link ContextSource}.
 * 
 * @author Eric Dalquist
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
