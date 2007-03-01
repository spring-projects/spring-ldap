package org.springframework.ldap.authentication;

import org.springframework.ldap.core.AuthenticationSource;

/**
 * {@link AuthenticationSource} to be used if the principal and credentials
 * should be returned on a ThreadLocal basis. With this
 * <code>AuthenticationSource</code>, the {@link #getPrincipal()} and
 * {@link #getCredentials()} methods return the values set using
 * {@link ThreadLocalAuthenticationHolder#setPrincipal(String)} and
 * {@link ThreadLocalAuthenticationHolder#setCredentials(String)} respectively,
 * thus enabling use of a singleton ContextSource but still enabling the
 * authentication information to be thread specific.
 * <p>
 * This <code>AuthenticationSource</code> can be used e.g. to implement a
 * simple authentication mechanism using <code>ContextSource</code>, like so:
 * <p>
 * <b>Configuration</b>:
 * <pre>
 *   &lt;bean id="abstractContextSource" abstract="true" 
 *            class="org.springframework.ldap.core.support.LdapContextSource" &gt;
 *      ... Common configuration here ...
 *   &lt;/bean&gt;
 *   
 *   &lt;bean id="contextSource"
 *            parent="abstractContextSource" &gt;
 *      &lt;property name="userName" value="..." /&gt;
 *      &lt;property name="password" value="..." /&gt;
 *   &lt;/bean&gt;
 *   
 *   &lt;bean id="authenticationContextSource"
 *            parent="abstractContextSource" &gt;
 *      &lt;property name="authenticationSource"&gt;
 *         &lt;bean class="org.springframework.ldap.authentication.ThreadLocalAuthenticationSource" /&gt;
 *      &lt;/property&gt;
 *   &lt;/bean&gt;
 * </pre>
 * <p>
 * <b>Authentication code</b>:
 * <pre>
 *    public class MyDao {
 *      private ContextSource authenticationContextSource;
 *      
 *      public void setAuthenticationContextSource(ContextSource contextSource){
 *         this.authenticationContextSource = contextSource;
 *      }
 *      
 *      ...
 *      
 *      public boolean authenticate(String userDN, String credentials){
 *        ThreadLocalAuthenticationHolder.setPrincipal(userDN);
 *        ThreadLocalAuthenticationHolder.setCredentials(credentials);
 *        
 *        try{
 *          authenticationContextSource.getReadWriteContext();
 *          return true;
 *        }catch(Exception e){
 *          return false;
 *        }
 *      }
 *    }
 *    </pre>
 *    
 *    @author Mattias Arthursson
 * 
 */
public class ThreadLocalAuthenticationSource implements AuthenticationSource {
    public String getCredentials() {
        return ThreadLocalAuthenticationHolder.getCredentials();
    }

    public String getPrincipal() {
        return ThreadLocalAuthenticationHolder.getPrincipal();
    }
}
