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

import org.springframework.ldap.samples.person.dao.CompanyDao;
import org.springframework.util.Assert;

/**
 * Responsible for retrieving available companies for a given country, sorting
 * them, and adjusting the command object if necessary. Collaborates with
 * {@link ChainedOptionStrategy}, {@link BeanConverter}, {@link CompanyDao}.
 * 
 * @author Ulrik Sandberg
 */
public class CompanyOption extends AbstractChainedOption {

    private String countryProperty;
    
    private CompanyDao companyDao;

    /*
     * @see net.sf.chainedoptions.AbstractChainedOption#retrieveOptions(java.lang.Object,
     *      java.lang.Object)
     */
    public List retrieveOptions(Object command, Object context) {
        String country = (String) getProperty(command, countryProperty);
        List companyBeans = companyDao.findByCountry(country);
        List companies = getConverter().convert(companyBeans);
        return getStrategy(command).adjustAndSort(companies, context);
    }

    /*
     * @see net.sf.chainedoptions.AbstractChainedOption#initChainedOption()
     */
    protected void initChainedOption() {
        super.initChainedOption();
        Assert.notNull(companyDao, "Property 'companyDao' must be set");
        Assert.notNull(countryProperty, "Property 'countryProperty' must be set");
        Assert.notNull(getConverter(), "Property 'converter' must be set");
    }

    public void setCompanyDao(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    public void setCountryProperty(String countryProperty) {
        this.countryProperty = countryProperty;
    }
}
