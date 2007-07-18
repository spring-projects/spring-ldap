<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="ldaptemplate.htm" method="post">
	<table>
		<tr>
			<td>Person Details</td>
		</tr>
		<tr>
			<td colspan="2"><hr></td>
		</tr>
		<tr>
			<td><b>Name</b></td>
			<td>${person.fullName}</td>
		</tr>
		<tr>
			<td><b>Country</B></td>
			<td>${person.country}</td>
		</tr>
		<tr>
			<td><b>Company</b></td>
			<td>${person.company}</td>
		</tr>
		<tr>
			<td><b>Phone</b></td>
			<td>${person.phone}</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_edit" value="Edit">
				<input type="submit" class="button" name="_eventId_back" value="Back">
			</td>
		</tr>
	</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>