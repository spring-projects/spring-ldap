/*
 * Copyright 2005-2015 the original author or authors.
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

package org.springframework.ldap.config;

/**
 * @author Mattias Hellborg Arthursson
 * @author Anindya Chatterjee
 */
public abstract class Elements {
    public static final String CONTEXT_SOURCE = "context-source";
    public static final String POOLING = "pooling";
    public static final String POOLING2 = "pooling2";
    public static final String LDAP_TEMPLATE = "ldap-template";
    public static final String TRANSACTION_MANAGER = "transaction-manager";
    public static final String REPOSITORIES = "repositories";
    public static final String DEFAULT_RENAMING_STRATEGY = "default-renaming-strategy";
    public static final String DIFFERENT_SUBTREE_RENAMING_STRATEGY = "different-subtree-renaming-strategy";
}
