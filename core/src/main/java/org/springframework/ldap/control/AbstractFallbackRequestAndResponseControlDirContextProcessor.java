package org.springframework.ldap.control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Convenient base class useful when implementing a standard DirContextProcessor
 * which has a request control and a response control. It handles the loading of
 * the control classes, using fallback implementations specified by the subclass
 * if necessary. It handles the request control constructor invocation; it only
 * needs the constructor arguments to be provided. It also handles most of the
 * work in the post processing of the response control, only delegating to a
 * template method for the actual value retrieval. In short, it makes it easy to
 * implement a custom DirContextProcessor.</p>
 * 
 * <pre>
 * public class SortControlDirContextProcessor extends AbstractFallbackRequestAndResponseControlDirContextProcessor {
 * 	String sortKey;
 * 
 * 	private boolean sorted = false;
 * 
 * 	private int resultCode = -1;
 * 
 * 	public SortControlDirContextProcessor(String sortKey) {
 * 		this.sortKey = sortKey;
 * 
 * 		defaultRequestControl = &quot;javax.naming.ldap.SortControl&quot;;
 * 		defaultResponseControl = &quot;com.sun.jndi.ldap.ctl.SortControl&quot;;
 * 		fallbackRequestControl = &quot;javax.naming.ldap.SortResponseControl&quot;;
 * 		fallbackResponseControl = &quot;com.sun.jndi.ldap.ctl.SortResponseControl&quot;;
 * 
 * 		loadControlClasses();
 * 	}
 * 
 * 	public boolean isSorted() {
 * 		return sorted;
 * 	}
 * 
 * 	public int getResultCode() {
 * 		return resultCode;
 * 	}
 * 
 * 	public Control createRequestControl() {
 * 		return super.createRequestControl(new Class[] { String[].class, boolean.class }, new Object[] {
 *				new String[] { sortKey }, Boolean.valueOf(critical) });
 * 	}
 * 
 * 	protected void handleResponse(Object control) {
 * 		Boolean result = (Boolean) invokeMethod(&quot;isSorted&quot;, responseControlClass, control);
 * 		this.sorted = result.booleanValue();
 * 		Integer code = (Integer) invokeMethod(&quot;getResultCode&quot;, responseControlClass, control);
 * 		resultCode = code.intValue();
 * 	}
 * }
 * </pre>
 * 
 * @author Ulrik Sandberg
 */
public abstract class AbstractFallbackRequestAndResponseControlDirContextProcessor extends
		AbstractRequestControlDirContextProcessor {

	private static final boolean CRITICAL_CONTROL = true;

	protected Class responseControlClass;

	protected Class requestControlClass;

	protected boolean critical = CRITICAL_CONTROL;

	protected String defaultRequestControl;

	protected String defaultResponseControl;

	protected String fallbackRequestControl;

	protected String fallbackResponseControl;

	protected void loadControlClasses() {
		Assert.notNull(defaultRequestControl, "defaultRequestControl must not be null");
		Assert.notNull(defaultResponseControl, "defaultResponseControl must not be null");
		Assert.notNull(fallbackRequestControl, "fallbackRequestControl must not be null");
		Assert.notNull(fallbackResponseControl, "fallbackReponseControl must not be null");
		try {
			requestControlClass = Class.forName(defaultRequestControl);
			responseControlClass = Class.forName(defaultResponseControl);
		}
		catch (ClassNotFoundException e) {
			log.debug("Default control classes not found - falling back to LdapBP classes", e);

			try {
				requestControlClass = Class.forName(fallbackRequestControl);
				responseControlClass = Class.forName(fallbackResponseControl);
			}
			catch (ClassNotFoundException e1) {
				throw new UncategorizedLdapException(
						"Neither default nor fallback classes are available - unable to proceed", e);
			}
		}
	}

	/**
	 * Set the class of the expected ResponseControl for the sorted result
	 * response.
	 * 
	 * @param responseControlClass Class of the expected response control.
	 */
	public void setResponseControlClass(Class responseControlClass) {
		this.responseControlClass = responseControlClass;
	}

	public void setRequestControlClass(Class requestControlClass) {
		this.requestControlClass = requestControlClass;
	}

	/**
	 * Utility method for invoking a method on a Control.
	 * @param method name of method to invoke
	 * @param clazz Class of the object that the method should be invoked on
	 * @param control Instance that the method should be invoked on
	 * @return the invocation result, if any
	 */
	protected Object invokeMethod(String method, Class clazz, Object control) {
		Method actualMethod = ReflectionUtils.findMethod(clazz, method);
		return ReflectionUtils.invokeMethod(actualMethod, control);
	}

	/**
	 * Creates a request control using the constructor parameters given in
	 * <code>params</code>.
	 * @param paramTypes Types of the constructor parameters
	 * @param params Actual constructor parameters
	 * @return Control to be used by the DirContextProcessor
	 */
	public Control createRequestControl(Class[] paramTypes, Object[] params) {
		Constructor constructor = ClassUtils.getConstructorIfAvailable(requestControlClass, paramTypes);
		if (constructor == null) {
			throw new IllegalArgumentException("Failed to find an appropriate RequestControl constructor");
		}

		Control result = null;
		try {
			result = (Control) constructor.newInstance(params);
		}
		catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}

		return result;
	}

	/*
	 * @see
	 * org.springframework.ldap.core.DirContextProcessor#postProcess(javax.naming
	 * .directory.DirContext)
	 */
	public void postProcess(DirContext ctx) throws NamingException {

		LdapContext ldapContext = (LdapContext) ctx;
		Control[] responseControls = ldapContext.getResponseControls();
		if (responseControls == null) {
			responseControls = new Control[0];
		}

		// Go through response controls and get info, regardless of class
		for (int i = 0; i < responseControls.length; i++) {
			Control responseControl = responseControls[i];

			// check for match, try fallback otherwise
			if (responseControl.getClass().isAssignableFrom(responseControlClass)) {
				Object control = responseControl;
				handleResponse(control);
				return;
			}
		}

		log.fatal("No matching response control found for paged results - looking for '" + responseControlClass);
	}

	protected abstract void handleResponse(Object control);
}
