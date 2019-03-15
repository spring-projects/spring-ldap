/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.core;

import org.springframework.ldap.NamingException;

/**
 * The root of the Spring LDAP ODM exception hierarchy.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 *
 */
@SuppressWarnings("serial")
public class OdmException extends NamingException {
    public OdmException(String message) {
        super(message);
    }

    public OdmException(String message, Throwable e) {
        super(message, e);
    }
}
