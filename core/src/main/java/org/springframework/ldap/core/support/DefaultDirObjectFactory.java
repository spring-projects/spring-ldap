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

package org.springframework.ldap.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirObjectFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * Default implementation of the DirObjectFactory interface. Creates a
 * {@link DirContextAdapter} from the supplied arguments.
 *
 * @author Mattias Hellborg Arthursson
 */
public class DefaultDirObjectFactory implements DirObjectFactory {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultDirObjectFactory.class);

	/**
	 * Key to use in the ContextSource implementation to store the value of the
	 * base path suffix, if any, in the Ldap Environment.
	 *
	 * @deprecated Use {@link BaseLdapNameAware} and
	 * {@link BaseLdapPathBeanPostProcessor} instead.
	 */
	public static final String JNDI_ENV_BASE_PATH_KEY = "org.springframework.ldap.base.path";

	private static final String LDAP_PROTOCOL_PREFIX = "ldap://";

	private static final String LDAPS_PROTOCOL_PREFIX = "ldaps://";

	@Override
	public final Object getObjectInstance(
			Object obj,
			Name name,
			Context nameCtx,
			Hashtable<?, ?> environment,
			Attributes attrs) throws Exception {

		try {
			String nameInNamespace;
			if (nameCtx != null) {
				nameInNamespace = nameCtx.getNameInNamespace();
			}
			else {
				nameInNamespace = "";
			}

			return constructAdapterFromName(attrs, name, nameInNamespace);
		}
		finally {
			// It seems that the object supplied to the obj parameter is a
			// DirContext instance with reference to the same Ldap connection as
			// the original context. Since it is not the same instance (that's
			// the nameCtx parameter) this one really needs to be closed in
			// order to correctly clean up and return the connection to the pool
			// when we're finished with the surrounding operation.
			if (obj instanceof Context) {

				Context ctx = (Context) obj;
				try {
					ctx.close();
				}
				catch (Exception e) {
					// Never mind this
				}

			}
		}
	}

	/**
	 * Construct a DirContextAdapter given the supplied paramters. The
	 * <code>name</code> is normally a JNDI <code>CompositeName</code>, which
	 * needs to be handled with particuclar care. Specifically the escaping of a
	 * <code>CompositeName</code> destroys proper escaping of Distinguished
	 * Names. Also, the name might contain referral information, in which case
	 * we need to separate the server information from the actual Distinguished
	 * Name so that we can create a representing DirContextAdapter.
	 *
	 * @param attrs the attributes
	 * @param name the Name, typically a <code>CompositeName</code>, possibly
	 * including referral information.
	 * @param nameInNamespace the Name in namespace.
	 * @return a {@link DirContextAdapter} representing the specified
	 * information.
	 */
	DirContextAdapter constructAdapterFromName(Attributes attrs, Name name, String nameInNamespace) {
		String nameString;
		String referralUrl = "";

		if (name instanceof CompositeName) {
			// Which it most certainly will be, and therein lies the
			// problem. CompositeName.toString() completely screws up the
			// formatting
			// in some cases, particularly when backslashes are involved.
			nameString = LdapUtils
					.convertCompositeNameToString((CompositeName) name);
		}
		else {
			LOG
					.warn("Expecting a CompositeName as input to getObjectInstance but received a '"
							+ name.getClass().toString()
							+ "' - using toString and proceeding with undefined results");
			nameString = name.toString();
		}

		if (nameString.startsWith(LDAP_PROTOCOL_PREFIX) || nameString.startsWith(LDAPS_PROTOCOL_PREFIX)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received name '" + nameString + "' contains protocol delimiter; indicating a referral."
						+ "Stripping protocol and address info to enable construction of a proper LdapName");
			}
			try {
				URI url = new URI(nameString);
				String pathString = url.getPath();
				referralUrl = nameString.substring(0, nameString.length() - pathString.length());

				if (StringUtils.hasLength(pathString) && pathString.startsWith("/")) {
					// We don't want any slash in the beginning of the
					// Distinguished Name.
					pathString = pathString.substring(1);
				}

				nameString = pathString;
			}
			catch (URISyntaxException e) {
				throw new IllegalArgumentException(
						"Supplied name starts with protocol prefix indicating a referral,"
								+ " but is not possible to parse to an URI",
						e);
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Resulting name after removal of referral information: '" + nameString + "'");
			}
		}

		DirContextAdapter dirContextAdapter = new DirContextAdapter(attrs, LdapUtils.newLdapName(nameString),
				LdapUtils.newLdapName(nameInNamespace), referralUrl);
		dirContextAdapter.setUpdateMode(true);
		return dirContextAdapter;
	}

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		return null;
	}

}
