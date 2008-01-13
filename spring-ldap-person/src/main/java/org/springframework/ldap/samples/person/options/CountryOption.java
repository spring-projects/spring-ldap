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
package org.springframework.ldap.samples.person.options;

import java.util.List;

import net.sf.chainedoptions.AbstractChainedOption;
import net.sf.chainedoptions.BeanConverter;
import net.sf.chainedoptions.ChainedOptionStrategy;

import org.springframework.ldap.samples.person.dao.CountryDao;
import org.springframework.util.Assert;

/**
 * Responsible for retrieving available countries, sorting them, and adjusting
 * the command object if necessary. Collaborates with
 * {@link ChainedOptionStrategy}, {@link BeanConverter}, {@link CountryDao}.
 * 
 * @author Ulrik Sandberg
 */
public class CountryOption extends AbstractChainedOption {

    private CountryDao countryDao;

    /*
     * @see net.sf.chainedoptions.AbstractChainedOption#retrieveOptions(java.lang.Object,
     *      java.lang.Object)
     */
    public List retrieveOptions(Object command, Object context) {
        List countryBeans = countryDao.findAll();
        List countries = getConverter().convert(countryBeans);
        return getStrategy(command).adjustAndSort(countries, context);
    }

    /*
     * @see net.sf.chainedoptions.AbstractChainedOption#initChainedOption()
     */
    protected void initChainedOption() {
        super.initChainedOption();
        Assert.notNull(countryDao, "Property 'countryDao' must be set");
        Assert.notNull(getConverter(), "Property 'converter' must be set");
    }

    public void setCountryDao(CountryDao countryDao) {
        this.countryDao = countryDao;
    }
}
