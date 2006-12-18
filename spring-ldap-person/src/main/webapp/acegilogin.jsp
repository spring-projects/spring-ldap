<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>

<html>
  <head>
    <title>Login</title>
  </head>

  <body>
    <h1>Login</h1>

	<P>Valid users:
	<P>
	<P>username <b>some.person</b> (USER), password <b>password</b><BR>
	username <b>some.person2</b> (ADMIN), password <b>password</b>
	<p>
	
    <%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
    <c:if test="${not empty param.login_error}">
      <font color="red">
        Your login attempt was not successful, try again.<BR>
        Reason: <%= ((AuthenticationException) session.getAttribute(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
      </font>
    </c:if>

    <form action="<c:url value='j_acegi_security_check'/>" method="POST">
      <table>
        <tr><td>User:</td>
        <td>
		<c:choose>
	       	<c:when test="${not empty param.login_error}">
        <input type='text' name='j_username' value='<%= session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY) %>'>
	       	</c:when>
	       	<c:otherwise>
        <input type='text' name='j_username'>
	       	</c:otherwise>
	    </c:choose>
        </td></tr>
        <tr><td>Password:</td><td><input type='password' name='j_password'></td></tr>

        <tr><td colspan='2'><input name="submit" type="submit" value="Login">&nbsp;
        <input name="reset" type="reset"></td></tr>
      </table>

    </form>

  </body>
</html>
