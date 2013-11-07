<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />

<div class="container">
    <div class="main-body item-list">
        <h2>User List</h2>
        <c:forEach var="user" items="${users}" varStatus="stat">
            <div class="row row-${stat.index % 2}">
                <div class="col-md-11">${user.fullName} - ${user.department}&nbsp;</div>
                <div class="col-md-1"><a href="<c:url value='/users/${user.id}'/>">Edit</a></div>
            </div>
        </c:forEach>
        <div class="button-bottom">
            <div class="col-md-offset-10 col-md-1"><a class="btn btn-primary" href="<c:url value='/newuser' />" role="button">New User</a></div>
        </div>
    </div>
</div>

<c:import url="footer.jsp" />
