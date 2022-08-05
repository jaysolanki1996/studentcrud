<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="isFormsPage" value="true" />

<%-- <spring:eval expression="T(my.airo.common.enums.BankCountry).values()" var="BankCountryList" /> --%>
&nbsp;
<!-- BEGIN form -->

<%-- <form action="${formPostUrl}" method="post"> --%>
 
<!--   <input type="text" name="name"></input><br> -->
<!--   <input type="text" name="designation"></input><br> -->
<!--   <input type="text" name="city"></input><br> -->
<!--   <input type="text" name="name"></input> <br> -->
<!--   <input type="submit"></input>  -->
<%-- </form> --%>

<form:form id="mainForm" class="form-horizontal" name="mainForm"
	action="${formPostUrl}" modelAttribute="model" method="post"
	role="form" data-parsley-validate="true">

	<h1 class="page-header">${not empty model.id ? 'Update' : 'Create'}
		Student</h1>

	<!-- BEGIN row -->
    
   <label for="name">Name *</label>
	<div >
		<form:input id="name" path="name" cssClass="form-control"  data-parsley-required="true" />
	</div>
	<br>
	
	<label for="Designation">Designation *</label>
	<div >
		<form:input id="designation" path="designation" cssClass="form-control"  data-parsley-required="true" />
	</div>
	<br>
	
	<label for="city">City *</label>
	<div >
		<form:input id="city" path="city" cssClass="form-control"  data-parsley-required="true" />
	</div>
	<br>
	
	<label for="gender">Gender: *</label>
	<div >
		Male <form:radiobutton path="gender" value="Male"/>  
        Female <form:radiobutton path="gender" value="Female"/>
	</div>
	<br>
	
	   
        
	
	<br>
	<button id="save" type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span>&nbsp;&nbsp;Save</button>
	
	<button id="cancel" type="submit" class="btn btn-primary"><a href="list">Cancel</a></button>

</form:form>
