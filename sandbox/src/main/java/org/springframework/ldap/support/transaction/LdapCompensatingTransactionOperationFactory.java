package org.springframework.ldap.support.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;

public class LdapCompensatingTransactionOperationFactory implements
        CompensatingTransactionOperationFactory {
    private static Log log = LogFactory
            .getLog(LdapCompensatingTransactionOperationFactory.class);

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ctx
     *            The transactional DirContext.
     */
    public LdapCompensatingTransactionOperationFactory(DirContext ctx) {
        this.ldapOperations = new LdapTemplate(new SingleContextSource(ctx));
    }

    public CompensatingTransactionRecordingOperation createRecordingOperation(
            String operation) {
        if (StringUtils.equals(operation, LdapUtils.BIND_METHOD_NAME)) {
            log.debug("Bind operation recorded");
            return new BindRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.REBIND_METHOD_NAME)) {
            log.debug("Rebind operation recorded");
            return new RebindRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.RENAME_METHOD_NAME)) {
            log.debug("Rename operation recorded");
            return new RenameRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation,
                LdapUtils.MODIFY_ATTRIBUTES_METHOD_NAME)) {
            return new ModifyAttributesRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.UNBIND_METHOD_NAME)) {
            return new UnbindRecordingOperation(ldapOperations);
        }

        log
                .warn("No suitable CompensatingTransactionRecordingOperation found for method "
                        + operation + ". Operation will not be transacted.");
        return new NullRecordingOperation();
    }

    /**
     * A {@link ContextSource} implementation using returning
     * {@link NonClosingDirContextInvocationHandler} proxies on the same
     * DirContext instance for each call.
     * 
     * @author Mattias Arthursson
     */
    static class SingleContextSource implements ContextSource {
        private DirContext ctx;

        /**
         * Constructor.
         * 
         * @param ctx
         *            the target DirContext.
         */
        public SingleContextSource(DirContext ctx) {
            this.ctx = ctx;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.ldap.ContextSource#getReadOnlyContext()
         */
        public DirContext getReadOnlyContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.ldap.ContextSource#getReadWriteContext()
         */
        public DirContext getReadWriteContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        private DirContext getNonClosingDirContextProxy(DirContext context) {
            return (DirContext) Proxy.newProxyInstance(DirContextProxy.class
                    .getClassLoader(), new Class[] { DirContextProxy.class },
                    new NonClosingDirContextInvocationHandler(context));

        }
    }

    /**
     * A proxy for DirContext forwarding all operation to the target DirContext,
     * but making sure that no <code>close</code> operations will be
     * performed.
     * 
     * @author Mattias Arthursson
     */
    public static class NonClosingDirContextInvocationHandler implements
            InvocationHandler {

        private DirContext target;

        public NonClosingDirContextInvocationHandler(DirContext target) {
            this.target = target;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            String methodName = method.getName();
            if (methodName.equals("getTargetContext")) {
                return target;
            } else if (methodName.equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("hashCode")) {
                // Use hashCode of Connection proxy.
                return new Integer(proxy.hashCode());
            } else if (methodName.equals("close")) {
                // Never close the target context, as this class will only be
                // used for operations concerning the compensating transactions.
                return null;
            }

            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }
}
