<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form:form commandName="group" method="post">
	<table>
		<tr>
			<td>Edit Group Details</td>
		</tr>
		<tr>
			<td colspan="2"><hr></td>
		</tr>
		<spring:hasBindErrors name="group">
		<tr>
			<td colspan="2">
				<div class="error">Please check form for invalid input</div>
			</td>
		</tr>
		</spring:hasBindErrors>
		<tr>
			<td><b>Name</b></td>
			<td><form:input path="name" disabled="true" /></td>
		</tr>
		<tr>
			<td colspan="2">
				<br>
				<b>Members:</b>
				<br>
				<select name="selectedMembers" size="4" multiple="true">
					<c:forEach var="item" items="${group.members}">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</td>
			<td>
               	<table>
               		<tr>
               			<td align="center">
				        	<input type="submit" class="button" name="_eventId_add"
								value="Add..." />
		                </td>
		            </tr>
		            <tr>
		                <td align="center">
				        	<input type="submit" class="button" name="_eventId_remove"
								value="Remove" />
		                </td>
	                </tr>
                </table>
			</td>
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
