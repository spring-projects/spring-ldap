<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>

	<c:url var="personUrl" value="/ldaptemplate.htm">
		<c:param name="_flowExecutionKey" value="${flowExecutionKey}" />
		<c:param name="_eventId" value="findPerson" />
	</c:url>
	<a href="<c:out value="${personUrl}" />">Find and manage persons</a>
	<br>
	<c:url var="groupUrl" value="/ldaptemplate.htm">
		<c:param name="_flowExecutionKey" value="${flowExecutionKey}" />
		<c:param name="_eventId" value="findGroup" />
	</c:url>
	<a href="<c:out value="${groupUrl}" />">Find and manage groups</a>
</div>

<%@ include file="includeBottom.jsp" %>