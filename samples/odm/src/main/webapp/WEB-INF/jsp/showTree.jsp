<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<body>
<h2>Operations</h2>
<h3>Clicking a link below performs the described operation which will be reflected in the LDAP tree below</h3>
<a href="addPerson.do">Add new test person 'John Doe'</a> (only works once)<br>
<a href="updatePhoneNumber.do">Add a '0' to the phone number of test person</a> (only works if the person has been created)<br>
<a href="removePerson.do">Remove test person</a><br>
<p>
<h2>Tree contents</h2>
<h3>Click a person row to see the attribute values (country and company rows do not have additional info)</h3>
<c:forEach var="row" items="${rows}">
			${row}
</c:forEach>
</p>
</body>
</html>
