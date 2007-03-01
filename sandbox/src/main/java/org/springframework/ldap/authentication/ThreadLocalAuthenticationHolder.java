package org.springframework.ldap.authentication;

/**
 * Holder for ThreadLocal values of principal and credentials, used by
 * {@link ThreadLocalAuthenticationSource} to get its values.
 * 
 * @author Mattias Arthursson
 * 
 */
public class ThreadLocalAuthenticationHolder {
    private static ThreadLocal threadPrincipal = new ThreadLocal();

    private static ThreadLocal threadCredentials = new ThreadLocal();

    /**
     * Not to be instantiated.
     */
    private ThreadLocalAuthenticationHolder() {
    }

    /**
     * Set the ThreadLocal principal DN.
     * 
     * @param principal
     *            the principal to be returned in this thread by
     *            {@link ThreadLocalAuthenticationSource#getPrincipal()}.
     */
    public static void setPrincipal(String principal) {
        threadPrincipal.set(principal);
    }

    /**
     * Set the ThreadLocal credentials.
     * 
     * @param credentials
     *            the credentials to be returned in this thread by
     *            {@link ThreadLocalAuthenticationSource#getCredentials()}.
     */
    public static void setCredentials(String credentials) {
        threadCredentials.set(credentials);
    }

    /**
     * Get the currently bound principal.
     * 
     * @return the currently bound principal.
     */
    public static String getPrincipal() {
        return (String) threadPrincipal.get();
    }

    /**
     * Get the currently bound credentials.
     * 
     * @return the currently bound credentials.
     */
    public static String getCredentials() {
        return (String) threadCredentials.get();
    }
}
