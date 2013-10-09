/*
 * Copyright 2005-2013 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

import javax.naming.ldap.LdapName;
import java.util.Collection;

/**
 * This <code>BeanPostProcessor</code> checks each bean if it implements
 * {@link BaseLdapNameAware} or {@link BaseLdapPathAware}.
 * If it does, the default context base LDAP path will be determined,
 * and that value will be injected to the {@link BaseLdapNameAware#setBaseLdapPath(javax.naming.ldap.LdapName)}
 * or  {@link BaseLdapPathAware#setBaseLdapPath(DistinguishedName)} method of the
 * processed bean.
 * <p>
 * If the <code>baseLdapPath</code> property of this
 * <code>BeanPostProcessor</code> is set, that value will be used. Otherwise, in
 * order to determine which base LDAP path to supply to the instance the
 * <code>ApplicationContext</code> is searched for any beans that are
 * implementations of {@link BaseLdapPathSource}. If one single occurrence is
 * found, that instance is queried for its base path, and that is what will be
 * injected. If more than one {@link BaseLdapPathSource} instance is configured
 * in the <code>ApplicationContext</code>, the name of the one to use will need
 * to be specified to the <code>baseLdapPathSourceName</code> property;
 * otherwise the post processing will fail. If no {@link BaseLdapPathSource}
 * implementing bean is found in the context and the <code>basePath</code>
 * property is not set, post processing will also fail.
 * 
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class BaseLdapPathBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, Ordered {

	private ApplicationContext applicationContext;

	private LdapName basePath;

	private String baseLdapPathSourceName;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof BaseLdapNameAware) {
            BaseLdapNameAware baseLdapNameAware = (BaseLdapNameAware) bean;

            if (basePath != null) {
                baseLdapNameAware.setBaseLdapPath(LdapUtils.newLdapName(basePath));
            }
            else {
                BaseLdapPathSource ldapPathSource = getBaseLdapPathSourceFromApplicationContext();
                baseLdapNameAware.setBaseLdapPath(LdapUtils.newLdapName(ldapPathSource.getBaseLdapName()));
            }
        } else if (bean instanceof BaseLdapPathAware) {
			BaseLdapPathAware baseLdapPathAware = (BaseLdapPathAware) bean;

			if (basePath != null) {
				baseLdapPathAware.setBaseLdapPath(new DistinguishedName(basePath));
			}
			else {
				BaseLdapPathSource ldapPathSource = getBaseLdapPathSourceFromApplicationContext();
				baseLdapPathAware.setBaseLdapPath(ldapPathSource.getBaseLdapPath().immutableDistinguishedName());
			}
		}
		return bean;
	}

	BaseLdapPathSource getBaseLdapPathSourceFromApplicationContext() {
		if (StringUtils.hasLength(baseLdapPathSourceName)) {
			return applicationContext.getBean(baseLdapPathSourceName, BaseLdapPathSource.class);
		}

        Collection<BaseLdapPathSource> beans = applicationContext.getBeansOfType(BaseLdapPathSource.class).values();
        if (beans.isEmpty()) {
            throw new NoSuchBeanDefinitionException("No BaseLdapPathSource implementation definition found");
        } else if (beans.size() == 1) {
            return beans.iterator().next();
        } else {
            BaseLdapPathSource found = null;

            // Try to find the correct one
            for (BaseLdapPathSource bean : beans) {
                if(bean instanceof AbstractContextSource) {
                    if(found != null) {
                        // More than one found - nothing much to do.
                        throw new NoSuchBeanDefinitionException(
                                "More than BaseLdapPathSource implementation definition found in current ApplicationContext; " +
                                        "unable to determine the one to use. Please specify 'baseLdapPathSourceName'");
                    }

                    found = bean;
                }
            }

            if(found == null) {
                throw new NoSuchBeanDefinitionException(
                        "More than BaseLdapPathSource implementation definition found in current ApplicationContext; " +
                                "unable to determine the one to use (one of them should be an AbstractContextSource instance). " +
                                "Please specify 'baseLdapPathSourceName'");
            }

            return found;
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#
	 * postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// Do nothing for this implementation
		return bean;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set the base path to be injected in all {@link BaseLdapPathAware} beans.
	 * If this property is not set, the default base path will be determined
	 * from any defined {@link BaseLdapPathSource} instances available in the
	 * <code>ApplicationContext</code>.
	 * 
	 * @param basePath the base path.
     * @deprecated {@link DistinguishedName} and associated classes and methods are deprecated as of 2.0.
	 */
	public void setBasePath(DistinguishedName basePath) {
		this.basePath = LdapUtils.newLdapName(basePath);
	}

    public void setBasePath(String basePath) {
        this.basePath = LdapUtils.newLdapName(basePath);
    }

	/**
	 * Set the name of the <code>ContextSource</code> bean to use for getting
	 * the base path. This method is typically useful if several ContextSource
	 * instances have been configured.
	 * 
	 * @param contextSourceName the name of the <code>ContextSource</code> bean
	 * to use for determining the base path.
	 */
	public void setBaseLdapPathSourceName(String contextSourceName) {
		this.baseLdapPathSourceName = contextSourceName;
	}

    /**
     * Set the order value of this object for sorting purposes.
     *
     * @param order the order of this instance. Defaults to <code>Ordered.LOWEST_PRECEDENCE</code>.
     * @see Ordered
     * @since 1.3.2
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
