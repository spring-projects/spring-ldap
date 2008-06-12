<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<body>
<a href="addPerson.do">Add new test person</a>
<a href="removePerson.do">Remove test person</a>

<c:forEach var="row" items="${rows}">
			${row}
		</c:forEach>
</body>
</html>
