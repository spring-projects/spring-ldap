<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="ldaptemplate.htm" method="post">
	<table>
		<tr>
			<td>
				Matching Persons
			</td>
		</tr>
		<tr>
			<td>
				<hr>
			</td>
		</tr>
		<tr>
			<td>
				<table border="1">
					<tr>
						<th>Name</th>
						<th>Company</th>
						<th>Country</th>
						<th>Phone</th>
					</tr>
					<c:forEach var="person" items="${results}">
						<tr>
							<td>
								<c:url var="url" value="ldaptemplate.htm">
									<c:param name="_flowExecutionKey" value="${flowExecutionKey}" />
									<c:param name="_eventId" value="select" />
									<c:param name="name" value="${person.fullName}" />
									<c:param name="company" value="${person.company}" />
									<c:param name="country" value="${person.country}" />
								</c:url>
								<a href="${url}">
									${person.fullName}
								</a>
							</td>
							<td>${person.company}</td>
							<td>${person.country}</td>
							<td>${person.phone}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
			<td class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_newSearch" value="New Search">
			</td>
		</tr>
	</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>