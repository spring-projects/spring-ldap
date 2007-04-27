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

package org.springframework.ldap.core;

/**
 * Helper interface to be used by Dao implementations for assembling to and from
 * context. Useful if we have assembler classes responsible for mapping to and
 * from a specific entry.
 * 
 * @author Mattias Arthursson
 */
public interface ContextAssembler extends ContextMapper {
    /**
     * Map the supplied object to the specified context.
     * 
     * @param obj
     *            the object to read data from.
     * @param ctx
     *            the context to map to.
     */
    public void mapToContext(Object obj, Object ctx);
}
