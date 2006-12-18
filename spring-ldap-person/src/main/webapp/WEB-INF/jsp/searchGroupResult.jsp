<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="ldaptemplate.htm" method="post">
	<table>
		<tr>
			<td>
				Matching Groups
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
					</tr>
					<c:forEach var="group" items="${groups}">
						<tr>
							<td>
								<c:url var="url" value="ldaptemplate.htm">
									<c:param name="_flowExecutionKey" value="${flowExecutionKey}" />
									<c:param name="_eventId" value="select" />
									<c:param name="name" value="${group.name}" />
								</c:url>
								<a href="${url}">
									${group.name}
								</a>
							</td>
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