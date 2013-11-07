<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<script type="text/javascript">
    activeNav = ".groups-nav";
</script>

<div class="container">
<div class="main-body">
    <form method="POST" action="<c:url value='/groups'/>" role="form" class="form-horizontal">
        <div class="form-group">
            <label for="name" class="control-label col-md-2">Group Name</label>
            <div class="col-md-4">
                <input type="text" class="form-control" id="name" name="name"/>
            </div>
        </div>
        <div class="form-group">
            <label for="description" class="control-label col-md-2">Description</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="description" id="description"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-md-offset-5 col-md-2">
                <button type="submit" class="btn btn-default">Submit</button>
            </div>
        </div>
    </form>
</div>
</div>

<c:import url="footer.jsp" />
