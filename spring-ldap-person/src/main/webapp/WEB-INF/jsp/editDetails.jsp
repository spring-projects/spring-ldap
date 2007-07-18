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
			<td><b>Country</B></td>
			<td><form:select path="country" onchange="this.form._eventId.value='formChange';this.form.submit()"><form:options items="${countries}" itemLabel="label" itemValue="value"/></form:select></td>
		</tr>
		<tr>
			<td><b>Company</b></td>
			<td><form:select path="company"><form:options items="${companies}" itemLabel="label" itemValue="value"/></form:select></td>
		</tr>
		<tr>
			<td><b>Phone</b></td>
			<td><form:input path="phone" /></td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="hidden" name="_eventId" value="">
				<input type="button" class="button" name="update" value="Update" onclick="this.form._eventId.value='submit';this.form.submit()">
				<input type="button" class="button" name="cancel" value="Cancel" onclick="this.form._eventId.value='cancel';this.form.submit()">
			</td>
		</tr>
	</table>
	</form:form>
</div>

<%@ include file="includeBottom.jsp" %>
