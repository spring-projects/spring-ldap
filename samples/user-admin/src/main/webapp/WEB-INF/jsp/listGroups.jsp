<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<script type="text/javascript">
    activeNav = ".groups-nav";
</script>

<div class="container">
    <div class="main-body item-list">
        <h2>User List</h2>
        <c:forEach var="group" items="${groups}" varStatus="stat">
            <div class="row row-${stat.index % 2}">
                <div class="col-md-11">${group}</div>
                <div class="col-md-1"><a href="<c:url value='/groups/${group}'/>">Edit</a></div>
            </div>
        </c:forEach>
        <div class="button-bottom">
            <div class="col-md-offset-10 col-md-1"><a class="btn btn-primary" href="<c:url value='/newGroup' />" role="button">New Group</a></div>
        </div>
    </div>
</div>

<c:import url="footer.jsp" />
