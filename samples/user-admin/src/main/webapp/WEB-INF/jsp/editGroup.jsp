<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<script type="text/javascript">
    activeNav = ".groups-nav";

    $(document).ready(function(){
        $('.addUserLink').click(function(event) {
            var target = $(event.target);
            $("#addUserId").val(target.attr('data-id'));
            $('#addUserForm').submit();
        });

        $('.removeUserLink').click(function(event) {
            var target = $(event.target);
            $("#removeUserId").val(target.attr('data-id'));
            $('#removeUserForm').submit();
        });
    });
</script>
<div class="container">
    <div class="main-body">
        <h2>Group</h2>
        <div class="row">
            <label class="col-md-3">Name:</label>
            <div class="col-md-7">${group.name}</div>
        </div>

        <div class="row">
            <label class="col-md-3">Description:</label>
            <div class="col-md-7">${group.description}</div>
        </div>

        <h3>Members</h3>
        <c:forEach var="user" items="${members}" varStatus="stat">
            <div class="row">
                <div class="col-md-3">${user.fullName} - ${user.department}&nbsp;</div>
                <div class="col-md-1"><a href="#" class="removeUserLink" data-id="${user.id}">Remove</a></div>
            </div>
        </c:forEach>

        <h3>Non-Members</h3>
        <c:forEach var="user" items="${nonMembers}" varStatus="stat">
            <div class="row">
                <div class="col-md-3">${user.fullName} - ${user.department}&nbsp;</div>
                <div class="col-md-1"><a href="#" class="addUserLink" data-id="${user.id}">Add</a></div>
            </div>
        </c:forEach>
    </div>
</div>
<form id="addUserForm" method="post" action="<c:url value='/groups/${group.name}/members'/>">
    <input id="addUserId" type="hidden" name="userId" />
</form>
<form id="removeUserForm" method="post" action="<c:url value='/groups/${group.name}/members'/>">
    <input type="hidden" name="_method" value="delete" />
    <input id="removeUserId" type="hidden" name="userId" />
</form>
<c:import url="footer.jsp" />
