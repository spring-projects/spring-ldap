<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:import url="header.jsp" />
<script type="text/javascript">
    var departments = ${departments};

    $(document).ready(function() {
        var departmentSelect = $('#department');
        var unitSelect = $('#unit');

        var populateUnits = function(selectedDepartment, selectedUnit) {
            selectedUnit = selectedUnit || departments[selectedDepartment][0];
            unitSelect.empty();

            _.each(departments[selectedDepartment], function (unit) {
                unitSelect.append($('<option value="' + unit + '">' + unit + '</option>'));
            });

            if (selectedUnit) {
                unitSelect.val(selectedUnit);
            }
        }

        var populateDepartments = function(selectedDepartment, selectedUnit) {
            selectedDepartment = selectedDepartment || _.keys(departments)[0];
            departmentSelect.empty();

            _.each(_.keys(departments), function(department) {
                departmentSelect.append($('<option value="' + department + '">' + department + '</option>'));
            });

            if(selectedDepartment) {
                departmentSelect.val(selectedDepartment);
            }
            populateUnits(selectedDepartment, selectedUnit);
        };

        populateDepartments('${user.department}', '${user.unit}');
        departmentSelect.change(function() {
            populateUnits(departmentSelect.val());
        });
    });
</script>

<div class="container">
<div class="main-body">
    <form method="POST" role="form" class="form-horizontal">
        <c:if test="${isNew}">
            <input type="hidden" name="employeeNumber" value="${user.employeeNumber}" />
        </c:if>
        <div class="form-group">
            <label for="employeeNumber" class="control-label col-md-2">Employee Number</label>
            <div class="col-md-2">
                <input type="text" disabled="disabled" class="form-control" id="employeeNumber" value="${user.employeeNumber}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="fullName" class="control-label col-md-2">Full Name</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="fullName" id="fullName" value="${user.fullName}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="firstName" class="control-label col-md-2">First Name</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="firstName" id="firstName" value="${user.firstName}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="lastName" class="control-label col-md-2">Last Name</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="lastName" id="lastName" value="${user.lastName}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="title" class="control-label col-md-2">Title</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="title" id="title" value="${user.title}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="email" class="control-label col-md-2">Email</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="email" id="email" value="${user.email}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="phone" class="control-label col-md-2">Phone</label>
            <div class="col-md-4">
                <input type="text" class="form-control" name="phone" id="phone" value="${user.phone}"/>
            </div>
        </div>
        <div class="form-group">
            <label for="department" class="control-label col-md-2">Department</label>
            <div class="col-md-4">
                <select name="department" id="department">
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="unit" class="control-label col-md-2">Unit</label>
            <div class="col-md-4">
                <select name="unit" id="unit">
                </select>
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
