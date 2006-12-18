<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="ldaptemplate.htm" method="post">
	<table>
		<tr>
			<td>Group Details</td>
		</tr>
		<tr>
			<td colpan="2"><hr></td>
		</tr>
		<tr>
			<td><b>Name</b></td>
			<td>${group.name}</td>
		</tr>
		<tr>
			<td colspan="2">
				<br>
				<b>Members:</b>
				<br>
				<c:forEach var="item" items="${group.members}">
					${item}<br/>
				</c:forEach>
			</td>
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