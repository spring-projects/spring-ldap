<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form:form commandName="searchCriteria" method="post">
	<table>
		<tr>
			<td>Group Search</td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<spring:hasBindErrors name="searchCriteria">
		<tr>
			<td colspan="2">
				<div class="error">Please provide valid search criteria</div>
			</td>
		</tr>
		</spring:hasBindErrors>
		<tr>
			<td>Name</td>
			<td>
				<form:input path="name" />
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_search" value="Search">
				<input type="submit" class="button" name="_eventId_cancel" value="Cancel">
			</td>
		</tr>		
	</table>
	</form:form>
</div>

<%@ include file="includeBottom.jsp" %>