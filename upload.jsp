<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<!-- Static content -->
<link rel="stylesheet" href="/BPMNXMLGenerator/resources/css/style.css">
<script src="/BPMNXMLGenerator/resources/js/jquery-1.9.1.js"></script>
<link href="/BPMNXMLGenerator/resources/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
<script src="/BPMNXMLGenerator/resources/js/bootstrap.min.js"></script>
<script src="/BPMNXMLGenerator/resources/js/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="/BPMNXMLGenerator/resources/js/app.js"></script>
<title>Excel to BPMN converter</title>
</head>
<body>
	<div class="row">
	  <div class="col-md-12 modal-content" align="center">
  		Excel to BPMN converter
  		</div>
  	</div>
  <div class="container">
	<div class="row row-full">
	  <div class="col-md-8 " align="center">
				<form action="upload" method="post" onsubmit="return validate()" enctype="multipart/form-data">
					<div class="form-group files">
						<input id="file" name="file" type="file" class="form-control" multiple="multiple">
					</div>
					<div>
                      <input id="version" name="version" type="checkbox" value="checked"> Generate a new version
                    </div>
					<div>
						<input class="upload" type="submit" value="Upload">
					</div>
					<div>
						<a class="download" href="/BPMNXMLGenerator/resources/sample/Process_Table.xlsx">Download Template</a>
					</div>
					<div id="messages">
						<c:if test="${success == false}">
							<div style="color: red">${message}</div>
						</c:if>
						<c:if test="${success == true}">
							<!-- <div style="color: green">Successfully generated BPMN files</div> -->
							<%-- <div><a href="${downloadLink}" aria-expanded="false"><i class="fa fa-download"></i>&nbsp; Download</a> generated BPMN file.</div> --%>
							<div>${downloadLink}</div>
						</c:if>
						<%-- <c:if test="${success == false}"> --%>
							<c:if test="${not empty errors}">
							<br>
							<div style="color: red">Uploading error Excel files </div>
							<div>
								<table>
									<tr>
										<th>File</th>
										<th>Row</th>
										<th>Error Message</th>
									</tr>
									<c:forEach var="entryData" items="${errors}">
									<c:forEach var="entry" items="${entryData.value}">
										<tr>
											<td><c:out value="${entryData.key}" /></td>
											<td><c:out value="${entry.key}" /></td>
											<td><c:out value="${entry.value}" /></td>
										</tr>
										</c:forEach>
									</c:forEach>
								</table>
							</div>
							</c:if>
						<%-- </c:if> --%>
					</div>
				</form>
			</div>
	</div>
</div>


  <!-- <div class="form">
    <form action="upload" method="post" onsubmit="return validate()"  enctype="multipart/form-data">
      <table>
        <tr>
          <td>Select File</td>
          <td><input id="file" name="file" type="file" multiple="multiple"></td>
          <td>&nbsp;</td>
          <td><input type="submit" value="Submit"></td>
        </tr>
      </table>
    </form>
  </div> -->

</body>

<script type="text/javascript">
	$("#file").change(function() {
		document.getElementById("messages").style.display = 'none';
	});
</script>
</html>