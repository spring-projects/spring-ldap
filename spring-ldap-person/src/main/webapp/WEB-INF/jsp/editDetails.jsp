<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form:form commandName="person" method="post">
	<table>
		<tr>
			<td>Edit Person Details</td>
		</tr>
		<tr>
			<td colspan="2"><hr></td>
		</tr>
		<spring:hasBindErrors name="person">
		<tr>
			<td colspan="2">
				<div class="error">Please check form for invalid input</div>
			</td>
		</tr>
		</spring:hasBindErrors>
		<tr>
			<td><b>Name</b></td>
			<td><form:input path="fullName" disabled="true" /></td>
		</tr>
		<tr>
			<td><b>Company</b></td>
			<td><form:input path="company" /></td>
		</tr>
		<tr>
			<td><b>Country</B></td>
			<td><form:input path="country" /></td>
		</tr>
		<tr>
			<td><b>Phone</b></td>
			<td><form:input path="phone" /></td>
		</tr>
		<tr>
			<td><b>Description</b></td>
			<td><form:input path="description" /></td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_submit" value="Update">
				<input type="submit" class="button" name="_eventId_cancel" value="Cancel">
			</td>
		</tr>
	</table>
	</form:form>
</div>

<%@ include file="includeBottom.jsp" %>
