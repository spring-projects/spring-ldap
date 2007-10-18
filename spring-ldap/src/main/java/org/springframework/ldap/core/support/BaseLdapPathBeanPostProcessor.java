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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.util.StringUtils;

/**
 * This <code>BeanPostProcessor</code> checks each bean if it implements
 * {@link BaseLdapPathAware}. If it does, the deafult context base LDAP path
 * will be determined, and that value will be injected to the
 * {@link BaseLdapPathAware#setBaseLdapPath(DistinguishedName)} method of the
 * processed bean.
 * <p>
 * If the <code>baseLdapPath</code> property of this
 * <code>BeanPostProcessor</code> is set, that value will be used. Otherwise,
 * in order to determine which base LDAP path to supply to the instance the
 * <code>ApplicationContext</code> is searched for any beans that are
 * implementations of {@link AbstractContextSource}. If one single occurrance
 * is found, that instance is queried for its base path, and that is what will
 * be injected. If more than one {@link AbstractContextSource} instance is
 * configured in the <code>ApplicationContext</code>, the name of the one to
 * use will need to be specified to the <code>contextSourceName</code>
 * property; otherwise the post processing will fail. If no
 * {@link AbstractContextSource} implementing bean is found in the context and
 * the <code>basePath</code> property is not set, post processing will also
 * fail.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class BaseLdapPathBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private DistinguishedName basePath;

	private String contextSourceName;

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof BaseLdapPathAware) {
			BaseLdapPathAware baseLdapPathAware = (BaseLdapPathAware) bean;

			if (basePath != null) {
				baseLdapPathAware.setBaseLdapPath(basePath);
			}
			else {
				AbstractContextSource abstractContextSource = getAbstractContextSourceFromApplicationContext();
				baseLdapPathAware.setBaseLdapPath(abstractContextSource.getBase());
			}
		}
		return bean;
	}

	AbstractContextSource getAbstractContextSourceFromApplicationContext() {
		if (StringUtils.hasLength(contextSourceName)) {
			return (AbstractContextSource) applicationContext.getBean(contextSourceName);
		}
		String[] definedContextSources = applicationContext.getBeanNamesForType(AbstractContextSource.class);
		if (definedContextSources.length < 1) {
			throw new NoSuchBeanDefinitionException("No AbstractContextSource implementation definition found");
		}
		else if (definedContextSources.length > 1) {
			throw new NoSuchBeanDefinitionException(
					"More than AbstractContextSource implementation definition found in current ApplicationContext");
		}
		return (AbstractContextSource) applicationContext.getBean(definedContextSources[0]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
	 * java.lang.String)
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
	 * from any defined {@link AbstractContextSource} instances available in the
	 * <code>ApplicationContext</code>.
	 * 
	 * @param basePath the base path.
	 */
	public void setBasePath(DistinguishedName basePath) {
		this.basePath = basePath;
	}

	/**
	 * Set the name of the <code>ContextSource</code> bean to use for getting
	 * the base path. This method is typically useful if several ContextSource
	 * instances have been configured.
	 * 
	 * @param contextSourceName the name of the <code>ContextSource</code>
	 * bean to use for determining the base path.
	 */
	public void setContextSourceName(String contextSourceName) {
		this.contextSourceName = contextSourceName;
	}

}
