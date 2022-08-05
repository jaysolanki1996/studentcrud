<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


Student Data
<table >
	<thead>
		<tr>
			<th >#</th>
			<th>Name</th>
			<th>Designation</th>
			<th>City</th>
			<th>Gender</th>
			<th>Actions</th>
		</tr>
	</thead>
	<tbody>
		<c:if test="${not empty list}">
			<c:forEach var="model" items="${list}" varStatus="status">
				<tr>
					<td> ${serialCount+status.index +1}.</td>
					<td>${model.name}</td>
					<td>${model.designation}</td>
					<td>${model.city}</td>
					<td>${model.gender}</td>
					
					<td><a
						href="update?id=${model.id}"
						> &nbsp;Edit
					</a></td>
				</tr>
			</c:forEach>
		</c:if>
	</tbody>
	
	
	<a href="create">Add new Student</a>
	<c:if test="${empty list}">
		<tr class="empty-row">
			<td colspan="3">No records found.</td>
		</tr>
	</c:if>
</table>





