<%@ include file="includeTop.jsp" %>

<div id="content">
	<form action="flowController.htm" method="post"/>
	<table>
		<tr>
			<td colspan="2" class="buttonBar">
				<!-- Tell webflow what flow execution we're participating in -->
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
				<!-- Tell webflow what event occurred -->
				<input type="submit" name="_eventId_submit" value="Submit">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>