<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@taglib prefix="misc" uri="/roboadvisor-misc"%>
<%@taglib prefix="javatime" uri="http://sargue.net/jsptags/time"%>

<c:set var="isFormsPage" value="true" />
<%@include file="../header.jspf" %>
<link href="${assetsBase}/plugins/jquery-file-upload/css/jquery.fileupload.css" rel="stylesheet" />
<link href="${assetsBase}/plugins/jquery-file-upload/css/jquery.fileupload-ui.css" rel="stylesheet" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.AgentOTPStatus).SentToAgent" var="AgentOTPStatusSentToAgent" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.AgentOTPStatus).Completed" var="AgentOTPStatusCompleted" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.KycStatus).Completed" var="KYCStatusCompleted" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).USD" var="USDCurrency" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).SGD" var="SGDCurrency" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).MYR" var="MYRCurrency" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).JPY" var="JPYCurrency" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).RMB" var="RMBCurrency" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Currency).HKD" var="HKDCurrency" />
<%-- <spring:eval expression="T(my.airo.roboadvisor.common.enums.Occupation).values()" var="Occupation" /> --%>
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Gender).values()" var="Gender" />
<spring:eval expression="T(my.airo.roboadvisor.common.enums.Role).Admin" var="admin" />
<spring:eval expression="{True,False}" var="booleans" />

&nbsp;
<!-- begin row -->
<div class="row">
<!-- begin col-12 -->
             <h1 class="page-header">
	              ${not empty model.id ? 'Update' : 'Create' } User
	        </h1>
    <div class="col-md-12">
        <%@include file="../otp-verification.jsp"%>
			</div>
			 <form:form id="entityForm"  class="form-horizontal" name="mainForm" action="${appContextName}/admin/user/${formPostUrl}"
			modelAttribute="${modelName}" method="post" role="form" data-parsley-validate="true" enctype="multipart/form-data">
			<c:set var="alertMessages">
				<form:errors path="*" cssClass="_spring_errors" />
			</c:set>
			<%@include file="../alert.jspf"%>
			<input type="hidden" id="kycFileName1" name="kyc1FileName" value="${model.kyc1FileName}"/>
			<input type="hidden" id="kycFileName2" name="kyc2FileName" value="${model.kyc2FileName}"/>
			<input type="hidden" id="kycFileName3" name="kyc3FileName" value="${model.kyc3FileName}"/>
			<input type="hidden" id="bankSignatureFileName1" name="declarationsSignatureFileName" value="${model.declarationsSignatureFileName}"/>
			<input type="hidden" id="userKYCStatus" value="${model.kycStatus}"/>
			<form:hidden id="deleteFile" path="deleteFile" name="deleteFile" />
			<div class="panel panel-inverse">
	            <div class="panel-heading">
	                <h3 class="panel-title">
	                   User Information
	                </h3>
	            </div>
	            <input type="hidden" name="_csrf" value="${csrfToken}"/>
					<div class="panel-body panel-form">
						<form:hidden id="code" path="otpCode"/>
						<form:hidden id="verificationToken" path="verificationToken" />
						<form:hidden id="modelName" value="${modelName}" path=""/>
						<fieldset>
								<div class="form-horizontal form-bordered">
									<div class="form-group">
										<label for="legalName" class="col-md-4 control-label">Legal Name</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="legalName" path="legalName" cssClass="form-control" data-parsley-maxlength="50"/>
										</div>
									</div>
									<div class="form-group">
										<label for="icNumber" class="col-md-4 control-label">NRIC/Passport No</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="icNumber" path="icNumber" cssClass="form-control" data-parsley-maxlength="50"/>
										</div>
									</div>
									
									<div class="form-group <spring:bind path="firstName"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="firstName" class="col-md-4 control-label">First Name *</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="firstName" path="firstName" cssClass="form-control" data-parsley-required="true" data-parsley-maxlength="50"/>
										</div>
									</div>
									 <div class="form-group">
										<label for="lastName" class="col-md-4 control-label">Last Name </label>
										<div class="col-md-7 col-sm-7">
											<form:input id="lastName" path="lastName" cssClass="form-control" data-parsley-maxlength="50"/>
										</div>
									</div>
									
									<div class="form-group <spring:bind path="email"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="email" class="col-md-4 control-label">Email *</label>
										<div class="col-md-7 col-sm-7">
											<div class="input-group">
												<span class="input-group-addon">@</span>
												<form:input id="email" path="email" cssClass="form-control" data-parsley-required="true" data-parsley-maxlength="100"/>
											</div>
										</div>
									</div>
									<div class="form-group <spring:bind path="mobileNumber"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="mobileNumber" class="col-md-4 control-label">Phone Number *</label>
										<div class="col-md-7 col-sm-7">
											<div class="input-group">
												<span class="input-group-addon">
													<span class="glyphicon glyphicon-phone-alt"></span>
												</span>
												<form:input type="text" id="mobileNumber" path="mobileNumber" cssClass="form-control" data-parsley-maxlength="20"/>
											</div>
										</div>
									</div>
									
									<div class="form-group <spring:bind path="password"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="password" class="col-md-4 control-label">Password 
										<c:if test="${empty model.id || model.needToRehashPassword == true}"> *</c:if> </label>
										<div class="col-md-7 col-sm-7">
											<input type="password" id="password" autocomplete="new-password" name="password" class="form-control" data-parsley-minlength="8" <c:if test="${model.id == null || model.needToRehashPassword == true}">data-parsley-required="true"</c:if>/>
											<div id="passwordStrength" class="is0 m-t-5"></div>                                   
										</div>
									</div>  
									<div class="form-group">
										<label for="repassword" class="col-md-4 control-label">Retype Password 
										<c:if test="${empty model.id  || model.needToRehashPassword == true}"> *</c:if></label>
										<div class="col-md-7 col-sm-7">
											<input type="password" id="repassword" autocomplete="new-password" name="repassword" class="form-control" data-parsley-minlength="8" data-parsley-equalto="#password" <c:if test="${model.id == null || model.needToRehashPassword == true}">data-parsley-required="true"</c:if>/>
											<div id="rePasswordStrength" class="is0 m-t-5"></div>
										</div>
									</div>
									<div class="form-group">
										<label for="isEmailVerified" class="col-md-4 control-label">Email Verified</label>
										<div class="col-md-1 pull-left">
											<form:checkbox class="m-t-10" path="isEmailVerified" id="isEmailVerified"/>
										</div>
									</div>
									<div class="form-group">
										<label for="mobileVerified" class="col-md-4 control-label">Phone Number Verified</label>
										<div class="col-md-1 pull-left">
											<form:checkbox class="m-t-10" path="mobileVerified" id="mobileVerified"/>
										</div>
									</div>
									<misc:sessionLookupTag roles="Admin" sessionRoles="${userSession.user.role}">
										<div class="form-group">
				                            <label class="col-md-4 control-label">Is Admin</label>
				                            <div class="col-md-7 col-sm-7">
					                            <%-- <c:choose>
					                           		<c:when test="${model.id == null}">
					                                    <div class="col-md-12">
					                                        <label class="radio-inline"><input type="radio" id="adminYes" name="isAdmin" value="true" /> Yes </label> 
					                                        <label class="radio-inline"><input type="radio" id="adminNo" name="isAdmin" value="false" checked/> No </label> 
					                                    </div>
					                                </c:when>
					                                <c:otherwise>
					                                    <div class="col-md-7 col-sm-7">
					                                        <c:choose>
					                                            <c:when test="${model.isAdmin == true}">
					                                                <label class="radio-inline"><input type="radio" id="adminYes" name="isAdmin" value="true" checked/> Yes </label> 
					                                                <label class="radio-inline"><input type="radio" id="adminNo" name="isAdmin" value="false"/> No </label>
					                                            </c:when>
					                                            <c:otherwise>
					                                                <label class="radio-inline"><input type="radio" id="adminYes" name="isAdmin" value="true" /> Yes </label> 
					                                                <label class="radio-inline"><input type="radio" id="adminNo" name="isAdmin" value="false" checked/> No </label>
					                                            </c:otherwise>
					                                        </c:choose>
					                                    </div>
					                                </c:otherwise>
				                             	</c:choose> --%>
				                             	Yes &nbsp;<form:radiobutton path="isAdmin" value="true" class="radio-inline"/>&nbsp;
				                             	No<form:radiobutton path="isAdmin" value="false" class="radio-inline"/>
			                             	</div>
				                        </div>
			                        </misc:sessionLookupTag>
			                        <div class="form-group">
                                            <label for="role" class="col-md-4 control-label">Role</label>
                                            <div class="col-md-7 pull-left">
                                                <c:forEach items="${roles}" var="item">
                                                    <span class="col-md-4">
                                                        <form:checkbox  class="m-t-10" path="role" value="${item.value}" label="${item.label}" name="role" disabled="${userSession.user.role.contains(admin) ? 'false' : 'true'}"/>
                                                    </span>
                                                </c:forEach>
                                            </div>
                                    </div>
			                        <div class="form-group">
										<label for="accountKey" class="col-md-4 control-label">Broker Account Key</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="accountKey" path="brokerSaxoApiKey" cssClass="form-control"/>
										</div>
									</div>
									<div class="form-group">
										<label for="brokerSaxoApiSecret" class="col-md-4 control-label">Broker Secret Token</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="brokerSaxoApiSecret" path="brokerSaxoApiSecret" cssClass="form-control"/>
										</div>
									</div>
									<div class="form-group">
										<label for="residenceCountry" class="col-md-4 control-label">Residence Country </label>
										<div class="col-md-7 col-sm-7">
									    	<form:select id="residenceCountry" path="residenceCountry" class="form-control selectpicker " data-size="7" data-live-search="true" data-style="btn-white" data-none-selected-text="- Residence Country -" name="residenceCountry">
									        	<form:option value="">- Residence Country -</form:option>
								            	<form:options path="residenceCountry" items="${residenceContryList}" itemLabel="label" itemValue="value"/>
								            </form:select>
								        </div>
								 	</div>
								 	<div class="form-group">
										<label for="occupation" class="col-md-4 control-label">Occupation</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="occupation" path="occupation" cssClass="form-control"/>
								        </div>
								 	</div>
								 	
								 	<div class="form-group">
										<label for="gender" class="col-md-4 control-label">Gender</label>
										<div class="col-md-7 col-sm-7">
									    	<form:select id="gender" path="gender" class="form-control selectpicker " data-size="7" data-live-search="true" data-style="btn-white" data-none-selected-text="- Gender -" name="gender">
									        	<form:option value="">- Gender -</form:option>
								            	<c:forEach items="${Gender}" var="gender">
												<form:option path="gender" value="${gender}">${gender.label}</form:option>
												</c:forEach>
								            </form:select>
								        </div>
								 	</div>
								 	
								 	<div class="form-group <spring:bind path="address"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="address" class="col-md-4 control-label">Address Line 1</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="address" path="address" cssClass="form-control" data-parsley-maxlength="100"/>
										</div>
									</div>
									<div class="form-group <spring:bind path="addressLine2"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="addressLine2" class="col-md-4 control-label">Address Line 2</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="addressLine2" path="addressLine2" cssClass="form-control" data-parsley-maxlength="100"/>
										</div>
									</div>
									<div class="form-group <spring:bind path="city"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="city" class="col-md-4 control-label">City</label>
										<div class="col-md-7 col-sm-7">
											<form:input id="city" path="city" cssClass="form-control" data-parsley-maxlength="32"/>
										</div>
									</div>
									<div class="form-group <spring:bind path="postalCode"><c:if test="${status.error}">has-error</c:if></spring:bind>">
										<label for="postalCode" class="col-md-4 control-label">Postal Code</label>
										<div class="col-md-7 col-sm-7">
											<form:input type="text" id="postalCode" path="postalCode" cssClass="form-control" data-parsley-type="number" data-parsley-maxlength="20"/>
										</div>
									</div>
								 	<div class="form-group">
								 		<label for="monthlyIncome" class="col-md-4 control-label">Monthly Income</label>
								 		<div class="col-md-7 col-sm-7">
									 		 <form:select id="monthlyIncome" path="monthlyIncome" class="form-control selectpicker" data-size="5" data-live-search="true" data-style="btn-white" name="monthlyIncome">
									 			 <form:options path="monthlyIncome" items="${monthlyIncomeList}" itemLabel="label" itemValue="value"/>
	                                         </form:select>
								    	</div>
									</div>
									<div class="form-group">
										<label for="hearAboutUs" class="col-md-4 control-label">How did you hear about us?</label>
										<div class="col-md-7 col-sm-7">
											<form:input type="text" id="hearAboutUs" path="hearAboutUs" cssClass="form-control"  data-parsley-maxlength="50"/>
										</div>
									</div>
									<div class="form-group">
										<label for="dob" class="col-md-4 control-label">Date Of Birth</label>
										<div class="col-md-4">
											<div id="dateOfBirth" class="input-group date" data-provide="datepicker" data-date-format="yyyy-mm-dd">
												<span class="btn input-group-addon"><i class="fa fa-calendar"></i></span>
								                <form:input id="dob" path="dateOfBirth" cssClass="form-control" placeholder="2018-13-02"/>
											</div>
										</div>
									</div>
									<div class="form-group">
                                        <label for="accountStatus" class="col-md-4 control-label">Account Status *</label>
                                        <div class="col-md-7 col-sm-7">
                                        	<form:select id="accountStatus" path="accountStatus" class="form-control selectpicker" data-size="auto" data-live-search="true" data-parsley-required="true" data-style="btn-white" name="accountStatus">
                                            	<form:options path="accountStatus" items="${accountStatusOpts}" itemLabel="label" itemValue="value"/>
                                            </form:select>
                                        </div>
                                     </div>
                                     <%-- <div class="form-group">
	                                 	<label class="col-md-4 control-label">User Issue Sent</label>
	                                    <div class="col-md-8">
		                                    <c:forEach  items="${booleans}" var="bool" varStatus="status">
		                                    	<label class="radio-inline userIssueSent"><form:radiobutton path="userIssueSent" value="${bool}"/>${bool eq true ? 'Yes':'No'}</label>
		                                    </c:forEach>
	                                    </div>
                            		</div> --%>
                            		<div class="form-group">
                                        <label for="disabledReasonsSystem" class="col-md-4 control-label">System Reasons</label>
                                        <div class="col-md-7 col-sm-7">
                                           <form:textarea id="disabledReasonsSystem" placeholder="Reasons" path="disabledReasonsSystem" disabled="true" cssClass="form-control"/>
                                        </div>
                                     </div>
								 	<div class="form-group">
										<label for="disabledReasonsManual" class="col-md-4 control-label">Manual Reasons</label>
										<div class="col-md-7 col-sm-7">
											<form:textarea id="disabledReasonsManual" placeholder="Reasons" path="disabledReasonsManual" cssClass="form-control"/>
										</div>
								 	</div>
								</div>
						</fieldset>
					</div>
			</div>
			<input type="hidden" class="userId"  value="${model.id}"/>
				
				<c:if test="${not empty model.id }">
					<div class="panel panel-inverse" id="kycDocument">
			            <div class="panel-heading">
			                <h3 class="panel-title">
			                   KYC Documents
			                </h3>
			            </div>
						<div class="panel-body panel-form kycDocument">
								<fieldset>
										<div class="form-horizontal form-bordered">
											<div class="form-group">
												<label class="col-md-4 control-label"></label>
												<div class="col-md-7 col-sm-7">
													<span class="label label-info" style="vertical-align: baseline;">Supported File Type " .jpg / .jpeg / .png / .pdf "</span>
												</div>
											</div>
											<div class="form-group">
												<label for="kycDoc1" class="col-md-4 control-label">Upload Identity Card (Front) </label>
												<div class="col-md-7 col-sm-7">
													<span class="btn btn-white fileinput-button showAddFile hide"> <i class="fa fa-plus"></i> <span>Add file...</span>
					                           			<input id="kycDoc1" class="userFile kyc1FileName" type="file" name="multipartFiles" accept="image/jpeg,image/png"/>
						                       		</span>
						                       		<a href="javascript:;" id="filename" style="display: none;"class="btn btn-success showAddFile hide">Invalid File Type</a>
						                       		<div style="clear:both;"></div>
							                         <c:choose>
							                            <c:when test="${not empty model.id && not empty model.kyc1FileName}">
								                              <a href="${appContextName}/admin/user/downloadfile?fileType=kyc1FileName&id=${model.id}&view=" class="hideDownloadDelete hide" data-lightbox="gallery-group-1" target="_blank">
									                            <c:choose>
									                            	<c:when test="${fn:containsIgnoreCase(model.kyc1FileName,'.pdf')}">
									                            		<img src="${resourcesBase}/images/robo_pdf.jpg" class="hideDownloadDelete col-md-4">
									                            	</c:when>
									                            	<c:otherwise>
									                            		<img src="${appContextName}/admin/user/downloadfile?fileType=kyc1FileName&id=${model.id}" style="width: 50%" onerror="this.src = '${resourcesBase}/images/user-0.jpg';"/>
									                            	</c:otherwise>
									                            </c:choose>
									                         </a>
							                             </c:when>
							                            <c:otherwise>
							                               <img src="${resourcesBase}/images/user-0.jpg" class="hideDownloadDelete col-md-4">
							                            </c:otherwise>
							                         </c:choose>
				                       			</div>
											</div>
											<div class="form-group">
												<label class="col-md-4 control-label"></label>
												<div class="col-md-8">
													<c:if test="${not empty model.id }"><a href="${appContextName}/admin/user/downloadfile?fileType=kyc1FileName&id=${model.id}"
					                            class="btn btn-default dropdown-toggle hideDownloadDelete hide" aria-expanded="false" target="_blank"><i class="fa fa-download"></i>&nbsp; Download</a>
						                         	<button type="button"  class="btn btn-primary deleteFile hideDownloadDelete hide" value="${model.kyc1FileName}" ><i class="fa fa-trash"></i>&nbsp; Delete</button>
						                         	</c:if>
												</div>
					                         </div>
											<div class="form-group">
												<label for="kycDoc2" class="col-md-4 control-label">Upload Identity Card (Back) </label>
												<div class="col-md-8">
													<span class="btn btn-white fileinput-button showAddFile hide"> <i class="fa fa-plus"></i> <span>Add file...</span>
					                           			<input id="kycDoc2" class="userFile kyc2FileName" type="file" name="multipartFiles" accept="image/jpeg,image/png" />
						                       		</span>
						                       		<a href="javascript:;" id="filename" style="display: none;"class="btn btn-success showAddFile hide">Invalid File Type</a>
						                       		<div style="clear:both;"></div>
							                         <c:choose>
							                            <c:when test="${not empty model.id && not empty model.kyc2FileName}">
								                              <a href="${appContextName}/admin/user/downloadfile?fileType=kyc2FileName&id=${model.id}&view=" class="hideDownloadDelete hide" data-lightbox="gallery-group-1" target="_blank">
									                        	<c:choose>
									                            	<c:when test="${fn:containsIgnoreCase(model.kyc2FileName,'.pdf')}">
									                            		<img src="${resourcesBase}/images/robo_pdf.jpg" class="hideDownloadDelete col-md-4">
									                            	</c:when>
									                            	<c:otherwise>
									                            		<img src="${appContextName}/admin/user/downloadfile?fileType=kyc2FileName&id=${model.id}" style="width: 50%"  onerror="this.src = '${resourcesBase}/images/user-0.jpg';"/>
									                            	</c:otherwise>
									                            </c:choose>
									                         </a>
							                             </c:when>
							                            <c:otherwise>
							                               <img src="${resourcesBase}/images/user-0.jpg" class="hideDownloadDelete col-md-4">
							                            </c:otherwise>
							                         </c:choose>
				                       			</div>
											</div>
											<div class="form-group">
												<label class="col-md-4 control-label"></label>
												<div class="col-md-8">
													<c:if test="${not empty model.id }"><a href="${appContextName}/admin/user/downloadfile?fileType=kyc2FileName&id=${model.id}"
						                            class="btn btn-default dropdown-toggle hideDownloadDelete hide" aria-expanded="false" target="_blank"><i class="fa fa-download"></i>&nbsp; Download</a>
													<button type="button" class="btn btn-primary deleteFile hideDownloadDelete hide" value="${model.kyc2FileName}"><i class="fa fa-trash"></i>&nbsp; Delete</button>
												   </c:if>
												</div>
				                       		</div>
				                       		
											<!-- Add KYC Doc TyPE -->
											<div class="form-group">
												<label for="kycDoc3" class="col-md-4 control-label">Upload Proof-of-Residence</label>
												<div class="col-md-8">
													<span class="btn btn-white fileinput-button showAddFile hide"> <i class="fa fa-plus"></i> <span>Add file...</span>
					                           			<input id="kycDoc3" class="userFile kyc3FileName" type="file" name="multipartFiles" accept="image/jpeg,image/png" />
						                       		</span>
						                       		<a href="javascript:;" id="filename" style="display: none;"class="btn btn-success showAddFile hide">Invalid File Type</a>
						                       		<div style="clear:both;"></div>
							                         <c:choose>
							                             <c:when test="${not empty model.id && not empty model.kyc3FileName}">
								                             <a href="${appContextName}/admin/user/downloadfile?fileType=kyc3FileName&id=${model.id}&view=" class="hideDownloadDelete hide" data-lightbox="gallery-group-1" target="_blank">
									                            <c:choose>
									                            	<c:when test="${fn:containsIgnoreCase(model.kyc3FileName,'.pdf')}">
										                            	<img  src="${resourcesBase}/images/robo_pdf.jpg" class="hideDownloadDelete col-md-4">
									                            	</c:when>
									                            	<c:otherwise>
									                            		<img src="${appContextName}/admin/user/downloadfile?fileType=kyc3FileName&id=${model.id}" style="width: 50%"  onerror="this.src = '${resourcesBase}/images/user-0.jpg';"/>
									                            	</c:otherwise>
									                            </c:choose>
									                         </a>
							                             </c:when>
							                             <c:otherwise>
							                               <img src="${resourcesBase}/images/user-0.jpg" class="hideDownloadDelete col-md-4">
							                             </c:otherwise>
							                         </c:choose>
				                       			</div>
											</div>
											
											<div class="form-group">
												<label class="col-md-4 control-label"></label>
												<div class="col-md-8">
						                        	<c:if test="${not empty model.id }">
						                        	<a href="${appContextName}/admin/user/downloadfile?fileType=kyc3FileName&id=${model.id}"
						                            	class="btn btn-default dropdown-toggle hideDownloadDelete hide" aria-expanded="false" target="_blank"><i class="fa fa-download"></i>&nbsp; Download</a>
						                         	<button type="button" class="btn btn-primary deleteFile hideDownloadDelete hide" value="${model.kyc3FileName}"><i class="fa fa-trash"></i>&nbsp; Delete</button>
												    </c:if>
												</div>
					                         </div>
					                         
					                         <div class="form-group">
												<label for="dob" class="col-md-4 control-label">Date Of Residence-Proof</label>
												<div class="col-md-4">
													<div id="residenceDocDate" class="input-group date" data-provide="datepicker" data-date-format="yyyy-mm-dd">
														<span class="btn input-group-addon"><i class="fa fa-calendar"></i></span>
										                <form:input id="rdd" path="residenceDocDate" cssClass="form-control" placeholder="2018-13-02" autocomplete="off"/>
													</div>
												</div>
											</div>
											
											<div class="form-group">
	                                            <label for="kycDocType" class="col-md-4 control-label">KYC Type</label>
	                                            <div class="col-md-7 col-sm-7">
	                                                <form:select id="kycDocType" path="kycDocType" class="form-control selectpicker" data-size="auto" data-live-search="true" data-style="btn-white" name="kycDocType">
	                                                    <form:options path="kycDocType" items="${kycTypeList}" itemLabel="label" itemValue="value"/>
	                                                </form:select>
	                                        </div>
											
											<div class="form-group">
	                                            <label for="kycStatus" class="col-md-4 control-label">KYC Status</label>
	                                            <div class="col-md-7 col-sm-7">
	                                                <form:select id="kycStatus" path="kycStatus" class="form-control selectpicker" data-size="auto" data-live-search="true" data-style="btn-white" name="kycStatus">
	                                                    <form:options path="kycStatus" items="${kycStatusList}" itemLabel="label" itemValue="value"/>
	                                                </form:select>
	                                        </div>
										</div>
										 <div class="form-group">
	                                        <label for="kycRemarks" class="col-md-4 control-label">KYC Remarks</label>
	                                        <div class="col-md-7 col-sm-7">
	                                           <form:textarea id="kycRemarks" placeholder="Remarks" path="kycRemarks" cssClass="form-control"/>
	                                        </div>
	                                     </div>
	                                     <form:hidden path="isKycIssueNoteSent" class="isKycIssueNoteSentClass"/>
	                                     <div class="form-group <spring:bind path="kycIssueNote"><c:if test="${status.error}">has-error</c:if></spring:bind>">
	                                        <label for="kycIssueNote" class="col-md-4 control-label">KYC Issue Note</label>
	                                        <div class="col-md-7 col-sm-7">
	                                           <form:textarea rows="12" placeholder="Issue Note" id="kycIssueNote" path="kycIssueNote" cssClass="form-control kycUserIssueNoteClassZ"/>
	                                        </div>
	                                     </div>
	                                     
	                                     <div class="form-group <spring:bind path="submissionStatus"><c:if test="${status.error}">has-error</c:if></spring:bind>">
	                                        <label for="submissionStatus" class="col-md-4 control-label">Due Dilligence Submission Status</label>
	                                        <div class="col-md-7 col-sm-7">
	                                           <form:input type="text" rows="12" id="submissionStatus" path="submissionStatus" cssClass="form-control" disabled="true"/>
	                                        </div>
	                                     </div>
	                                      <div class="form-group <spring:bind path="approvalStatus"><c:if test="${status.error}">has-error</c:if></spring:bind>">
	                                        <label for="approvalStatus" class="col-md-4 control-label">Due Dilligence Approval Status</label>
	                                        <div class="col-md-7 col-sm-7">
	                                           <form:input type="text" rows="12" id="approvalStatus" path="approvalStatus" cssClass="form-control" disabled="true"/>
	                                        </div>
	                                     </div>
	                                     <div class="form-group">
											<label for="dueDilligenceEmailSent" class="col-md-4 control-label">Due Dilligence Email Sent</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="dueDilligenceEmailSent" path="dueDilligenceEmailSent" cssClass="form-control" disabled="true"/>
											</div>
										</div>
										
										<div class="form-group">
											<label for="dueDilligenceLastSubmission" class="col-md-4 control-label">Last User Submission</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="dueDilligenceLastSubmission" path="dueDilligenceLastSubmission" cssClass="form-control" disabled="true"/>
											</div>
										</div>	                                     
	                                     <div class="form-group">
	                                        <label for="eddLink" class="col-md-4 control-label">EDD Link</label>
	                                        <div class="col-md-7 col-sm-7">
	                                        	<c:if test="${dueDilligenceId != null}">
	                                        		<a href="${appContextName}/admin/dueDilligence/update?id=${dueDilligenceId}" target="_blank">Enhanced Due Dilligence Page Link</a>
	                                        	</c:if>
	                                        </div>
	                                     </div>
	                                     
	                                     <div class="form-group">
	                                        <label for="link" class="col-md-4 control-label"></label>
	                                        <div class="col-md-7 col-sm-7">
	                                            <a href="${appContextName}/admin/dueDilligence/sendEdd?userId=${model.id}" class="btn btn-primary" cssClass="form-control" <c:if test="${model.dueDilligenceEmailSent != null}">disabled='disabled'</c:if>>Trigger Enhance Due Dilligence process</a><br><br>
	                                            <p><b>Clicking this will send an EDM to the client to request client to fill-up the enhanced due-dilligence form</b></p>
	                                        </div>
	                                     </div>
									</div>
									
								</fieldset>
						</div>
					</div>
				</c:if>
				<div class="panel panel-inverse" id="bankDetail">
		            <div class="panel-heading">
		                <h3 class="panel-title">
		                    Bank Details
		                </h3>
		            </div>
					<div class="panel-body panel-form" id="bankDetail">
							<fieldset>
										<div class="form-horizontal form-bordered">
											<div class="form-group">
											<label for="bankCountry" class="col-md-4 control-label">Bank Country</label>
											<div class="col-md-7 col-sm-7">
										    	<form:select id="bankCountry" path="bankCountry" class="form-control selectpicker "  data-live-search="true" data-style="btn-white"  name="bankCountry">
									            	<form:options path="bankCountry" items="${bankCountryList}" itemLabel="label" itemValue="value"/>
									            </form:select>
									        </div>
									        </div>
									 	</div>
									 	
										<div class="form-group">
											<label for="bankDetailsBankName" class="col-md-4 control-label">Bank Name</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsBankName" path="bankDetailsBankName"
														cssClass="form-control"/>
											</div>
										</div>
										<div class="form-group">
											<label for="bankDetailsBankAddress" class="col-md-4 control-label">Bank Address</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsBankAddress" path="bankDetailsBankAddress"
														cssClass="form-control"/>
											</div>
										</div>
									<%-- 	<div class="form-group">
											<label for="bankDetailsAba" class="col-md-4 control-label">ABA</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsAba" path="bankDetailsAba"
														cssClass="form-control"/>
											</div>
										</div>
										<div class="form-group">
											<label for="bankDetailsChips" class="col-md-4 control-label">Chips</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsChips" path="bankDetailsChips"
														cssClass="form-control"/>
											</div>
										</div> --%>
										<div class="form-group">
											<label for="bankDetailsSwiftNumber" class="col-md-4 control-label">Swift Number</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsSwiftNumber" path="bankDetailsSwiftNumber"
														cssClass="form-control"/>
											</div>
										</div>
										<div class="form-group">
											<label for="bankDetailsAccountName" class="col-md-4 control-label">Account Name</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsAccountName" path="bankDetailsAccountName"
														cssClass="form-control" />
											</div>
										</div>
										<div class="form-group">
											<label for="bankDetailsAccountNumber" class="col-md-4 control-label">Account Number</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsAccountNumber" path="bankDetailsAccountNumber"
														cssClass="form-control" />
											</div>
										</div>
										<div class="form-group">
											<label for="accountCurrencyType" class="col-md-4 control-label">Account Currency Type</label>
											<div class="col-md-7 col-sm-7">
									 		 <form:select id="accountCurrencyType" path="accountCurrencyType" class="form-control selectpicker" data-size="6" data-live-search="true" data-style="btn-white" name="accountCurrencyType">
									 			 <form:options path="accountCurrencyType" items="${accountCurrencyTypeList}" itemLabel="label" itemValue="value"/>
	                                         </form:select>
										</div>
										<%-- <div class="form-group">
											<label for="bankDetailsReference" class="col-md-4 control-label">Reference</label>
											<div class="col-md-7 col-sm-7">
												<form:input id="bankDetailsReference" path="bankDetailsReference"
														cssClass="form-control"/>
											</div>
										</div> --%>
										<div class="form-group">
											<label for="highNetWorthIndividual" class="col-md-4 control-label">High Net Worth</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="highNetWorthIndividual" id="highNetWorthIndividual"/>
											</div>
										</div>
										<div class="form-group">
											<label for="declarationsPep" class="col-md-4 control-label">I'm a PEP</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="declarationsPep" id="declarationsPep"/>
											</div>
										</div>
									<%-- 	<div class="form-group">
											<label for="declarationsCrc" class="col-md-4 control-label">Not CRC</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="declarationsCrc" id="declarationsCrc"/>
											</div>
										</div>
										<div class="form-group">
                                            <label for="declarationsTaxCrime" class="col-md-4 control-label">Not Involved in Tax Crime</label>
                                            <div class="col-md-1 pull-left">
                                                <form:checkbox class="m-t-10" path="declarationsTaxCrime" id="declarationsTaxCrime"/>
                                            </div>
                                        </div> --%>
										<div class="form-group">
											<label for="declarationsUsCitizen" class="col-md-4 control-label">US Citizen</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="declarationsUsCitizen" id="declarationsUsCitizen"/>
											</div>
										</div>
										<div class="form-group">
											<label for="homeLoan" class="col-md-4 control-label">Home Loan</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="homeLoan" id="homeLoan"/>
											</div>
										</div>
										<div class="form-group">
											<label for="carLoan" class="col-md-4 control-label">Car Loan</label>
											<div class="col-md-1 pull-left">
												<form:checkbox class="m-t-10" path="carLoan" id="carLoan"/>
											</div>
										</div>
										<div class="form-group">
											<label for="sourceOfIncome" class="col-md-4 control-label">Source Of Income</label>
											<div class="col-md-7 col-sm-7">
									 		  <form:input id="sourceOfIncome" path="sourceOfIncome"	cssClass="form-control"/>
											</div>
										</div>
										<%-- <div class="form-group">
											<label for="signature" class="col-md-4 control-label">Upload Signature</label>
											<div class="col-md-7 col-sm-7">
													<span class="btn btn-white fileinput-button showAddFile hide"> <i class="fa fa-plus"></i> <span>Add file...</span>
					                           			<input id="signature" class="userFile signatureFileName" type="file" name="multipartFiles" accept="image/jpeg,image/png" />
						                       		</span>
						                       		<a href="javascript:;" id="filename" style="display: none;"class="btn btn-success showAddFile hide">Invalid File Type</a>
						                       		<div style="clear:both;"></div>
													<div class="m-t-5 showAddFile hide">
														<span class="label label-info">Supported File
															Type " .jpg / .jpeg / .png / .pdf "</span> </div>
													<a href="${appContextName}/admin/user/downloadfile?fileType=declarationsSignatureFile&id=${model.id}&view=" class="hideDownloadDelete hide" data-lightbox="gallery-group-1" >
						                            <c:choose>
						                            	<c:when test="${fn:containsIgnoreCase(model.declarationsSignatureFileName,'.pdf')}">
							                            	<img src="${resourcesBase}/images/robo_pdf.jpg" class="hideDownloadDelete col-md-4">
						                            	</c:when>
						                            	<c:otherwise>
						                            		<img src="${appContextName}/admin/user/downloadfile?fileType=declarationsSignatureFile&id=${model.id}" style="width: 50%"/>
					    	                        	</c:otherwise>
					                            	</c:choose>
						                         	</a>
			                       			
										</div>
										</div>
										<div class="form-group">
											<label class="col-md-4 control-label"></label>
											<div class="col-md-8">
					                        	<a href="${appContextName}/admin/user/downloadfile?fileType=declarationsSignatureFile&id=${model.id}"
				                        	    class="btn btn-default dropdown-toggle hideDownloadDelete hide" aria-expanded="false" target="_blank"><i class="fa fa-download"></i>&nbsp; Download</a>
												<button type="button" class="btn btn-primary deleteFile hideDownloadDelete hide" value="${model.declarationsSignatureFileName}"><i class="fa fa-trash"></i>&nbsp; Delete</button>
											</div>
			                         	</div> --%>
										<div class="form-group">
                                            <label for="bankDetailsStatus" class="col-md-4 control-label">Bank Details Status</label>
                                            <div class="col-md-7 col-sm-7">
                                                <form:select id="bankDetailsStatus" path="bankDetailsStatus" class="form-control selectpicker" data-size="auto" data-live-search="true" data-style="btn-white" name="bankDetailsStatus">
                                                    <form:options path="bankDetailsStatus" items="${bankDetailStatus}" itemLabel="label" itemValue="value"/>
                                                </form:select>
                                        </div>
									</div>
                                     <div class="form-group">
                                        <label for="bankDetailsRemarks" class="col-md-4 control-label">Bank Details Remarks</label>
                                        <div class="col-md-7 col-sm-7">
                                           <form:textarea id="bankDetailsRemarks" placeholder="Remarks" path="bankDetailsRemarks" cssClass="form-control"/>
                                        </div>
                                     </div>
                                     
                                     <div class="form-group">
                                        <label for="bankIssueNote" class="col-md-4 control-label">Bank Issue Note</label>
                                        <div class="col-md-7 col-sm-7">
                                           <%-- <textarea rows="12" placeholder="User Issue Note" id="bankDetailUserIssueNoteId" path="kycIssueNote" class="form-control bankDetailUserIssueNoteClass">${model.userIssueNote}</textarea> --%>
                                           <form:textarea rows="12" placeholder="Issue Note" id="bankDetailUserIssueNoteId" path="bankIssueNote" cssClass="form-control bankDetailUserIssueNoteClass"/>
                                        </div>
                                     </div>
								</div>
							</fieldset>
					</div>
				</div>
				<div class="panel panel-inverse" id="termsAndConditions">
                    <div class="panel-heading">
                        <h3 class="panel-title">
                            Terms &amp; Conditions
                        </h3>
                    </div>
                    <div class="panel-body panel-form" id="termsAndConditionsPanel">
                            <fieldset>
                                    <div class="form-horizontal form-bordered">
                                        <div class="form-group">
                                            <label for="agreementUserAgreement" class="col-md-4 control-label">Agree with terms?</label>
                                            <div class="col-md-1 pull-left">
                                                <form:checkbox class="m-t-10" path="agreementUserAgreement" id="agreementUserAgreement"/>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="agreementUserAgreementAcknowledged" class="col-md-4 control-label">Acknowledge Recommendation</label>
                                            <div class="col-md-1 pull-left">
                                                <form:checkbox class="m-t-10" path="agreementUserAgreementAcknowledged" id="agreementUserAgreementAcknowledged"/>
                                            </div>
                                        </div>
                                    </div>
                            </fieldset>
                        </div>
                </div>
			<div class="panel panel-inverse" id="remittances">
				<div class="panel-heading">
					<h3 class="panel-title">Remittance</h3>
				</div>
				<div class="panel-body">
					<c:choose>
						<c:when test="${remittanceList.size() > 0}">
							<table class="table table-hover table-bordered">
								<thead>
									<tr>
										<th>Remittance Reference</th>
										<th>Net Remittance Amount</th>
										<th>Date</th>
										<th>Funding Status</th>
										<th>Action</th>
									</tr>
								</thead>
								<tbody class="_body">
									<c:forEach items="${remittanceList}" var="remittance"
										varStatus="status">
										<tr>
											<td class="col-md-2">${remittance.referenceNo }</td>
											<td class="col-md-2">${empty remittance.investorRemittanceReceivedAmount ? remittance.investorRemittanceRemittedAmount : remittance.investorRemittanceReceivedAmount} ${remittance.currency}</td>
											<fmt:parseDate value="${remittance.createdOn}" var="parsedDate"  pattern="yyyy-MM-dd'T'HH:mm"/>
                            				<fmt:formatDate value="${parsedDate}" var="remittanceDate" type="date" pattern="${commonProperties.DATE_FORMAT_FOR_FORM} " />
											<td class="col-md-2">${remittanceDate}</td>
											<td class="col-md-2">${remittance.investorRemittanceStatus.label }</td>
											<td class="action col-md-2"><a
												href="${appContextName}/admin/remittance/update?id=${remittance.id}"
												class="btn btn-primary" target="_blank"> <i
													class="fa fa-pencil-square-o"></i>&nbsp;Edit
											</a></td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</c:when>
						<c:otherwise>
							<span>No records found</span>
						</c:otherwise>
					</c:choose>
					<c:if test="${showMoreRemittance}"><a
												href="${appContextName}/admin/remittance/list?user.id=${model.id}"
												class="btn btn-primary pull-right" target="_blank">See More...
											</a></c:if>
				</div>
			</div>
			<div class="panel panel-inverse" id="redemptions">
				<div class="panel-heading">
					<h3 class="panel-title">Redemption</h3>
				</div>
				<div class="panel-body">
					<c:choose>
						<c:when test="${redemptionList.size() > 0}">
							<table class="table table-hover table-bordered">
								<thead>
									<tr>
										<th>Portfolio Name</th>
										<th>Net Redemption Amount</th>
										<th>Date</th>
										<th>Status</th>
										<th>Action</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${redemptionList}" var="redemption"
										varStatus="status">
										<tr>
											<td class="col-md-3"><a
												href="${appContextName}/admin/portfolio/update?id=${redemption.userPortfolio.portfolio.id}"
												target="_blank">${redemption.userPortfolio.portfolio.name }</a></td>
											<td class="col-md-3">${empty redemption.netRedemptionAmount ? redemption.amountRequestedFromBroker : redemption.netRedemptionAmount} ${redemption.currency}</td>
											<td class="col-md-2">${redemption.redemptionDate }</td>
											<td class="col-md-2">${redemption.redemptionStatus.label }</td>
											<td class="col-md-2"><a
												href="${appContextName}/admin/redemption/update?id=${redemption.id}"
												class="btn btn-primary" target="_blank"> <i
													class="fa fa-pencil-square-o"></i>&nbsp;Edit
											</a></td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</c:when>
						<c:otherwise>
							<span>No records found</span>
						</c:otherwise>
					</c:choose>
					<c:if test="${showMoreRedemption}"><a
												href="${appContextName}/admin/redemption/list?user.id=${model.id}"
												class="btn btn-primary pull-right" target="_blank">See More...
											</a></c:if>
				</div>
			</div>
			<c:forEach items="${userPortfolioPagingDto.results}" var="userPortfolio"
				varStatus="list">
				<%@include file="../UserPortfolio/user_portfolio_details.jspf"%>
			</c:forEach>
			<div>
						<c:if test="${showMorePortfolio}"><a
												href="${appContextName}/admin/userPortfolio/list?user.email=${model.email}"
												class="btn btn-primary pull-right" target="_blank">See More...
											</a><br><br><br></c:if>
											</div>
			<div class="panel panel-inverse" id="userSubmissions">
				<div class="panel-heading">
					<h3 class="panel-title">Submissions</h3>
				</div>
				<div class="panel-body">
					<c:choose>
						<c:when test="${userSubmissions.size() > 0}">
							<table class="table table-hover table-bordered">
								<thead>
									<tr>
										<th>SN</th>
										<th>Created On</th>
										<th>Status</th>
										<th>Type</th>
										<th>Sent On</th>
										<th>Issue Note</th>
										<th>Remarks</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${userSubmissions}" var="submission"
										varStatus="status">
										<tr>
											<td>${status.count}</td>
											<td class="col-md-2"><javatime:format value="${submission.createdOn}" pattern="M/dd/yy hh:mm a"></javatime:format></td>
											<td class="col-md-1">${submission.status }</td>
											<td class="col-md-1">${submission.type.label }</td>
											<td class="col-md-1"><javatime:format value="${submission.sentOn}" pattern="M/dd/yy hh:mm a"></javatime:format></td></td>
											<td class="col-md-3">${submission.userIssueNote }</td>
											<td class="col-md-4">${submission.remark}</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</c:when>
						<c:otherwise>
							<span>No records found</span>		
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="panel panel-inverse" id="userSubmissions">
				<div class="panel-heading">
					<h3 class="panel-title">User Cash Account</h3>
				</div>
				<div class="panel-body">
					<table class="table table-hover table-bordered">
						<thead>
							<tr>
								<th class="text-center">USD</th>
								<th class="text-center">SGD</th>
								<th class="text-center">MYR</th>
								<th class="text-center">JPY</th>
								<th class="text-center">RMB</th>
								<th class="text-center">HKD</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col-md-2 text-right">${USDCurrency.label} ${userCashAcccount.cashUsd}</td>
								<td class="col-md-2 text-right">${SGDCurrency.label} ${userCashAcccount.cashSgd}</td>
								<td class="col-md-2 text-right">${MYRCurrency.label} ${userCashAcccount.cashMyr}</td>
								<td class="col-md-2 text-right">${JPYCurrency.label} ${userCashAcccount.cashJpy}</td>
								<td class="col-md-2 text-right">${RMBCurrency.label} ${userCashAcccount.cashRmb}</td>
								<td class="col-md-2 text-right">${HKDCurrency.label} ${userCashAcccount.cashHkd}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			<div class="panel">
				<div class="panel-body panel-form">
					<fieldset disabled>
							<div class="form-horizontal form-bordered">
								<div class="form-group">
									<label for="bankDetailsDeclarationsStatusCompletedEmailSent" class="col-md-4 control-label">Bank Details Declarations Status 
									 Completed Email Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="bankDetailsDeclarationsStatusCompletedEmailSent" path="bankDetailsDeclarationsStatusCompletedEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="kycCompletedEmailSent" class="col-md-4 control-label">KYC Completed Email
									 Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="kycCompletedEmailSent" path="kycCompletedEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="bankIssueEmailSent" class="col-md-4 control-label">Bank Issue Email
									 Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="bankIssueEmailSent" path="bankIssueEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="kycIssueEmailSent" class="col-md-4 control-label">KYC Issue Email
									 Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="kycIssueEmailSent" path="kycIssueEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="bankDetailsDeclarationsEmailSent" class="col-md-4 control-label">Bank Details 
									 Declarations Email Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="bankDetailsDeclarationsEmailSent" path="bankDetailsDeclarationsEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="kycUploadEmailSent" class="col-md-4 control-label">KYC Upload Email
									 Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="kycUploadEmailSent" path="kycUploadEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="signupEmailSent" class="col-md-4 control-label">Signup Email
									 Sent</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="signupEmailSent" path="signupEmailSent"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="createdOn" class="col-md-4 control-label">Created
										on</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="createdOn" path="createdOn"
											cssClass="form-control" />
									</div>
								</div>
								<div class="form-group">
									<label for="updatedOn" class="col-md-4 control-label">Updated
										on</label>
									<div class="col-md-7 col-sm-7">
										<form:input id="updatedOn" path="updatedOn"
											cssClass="form-control" />
									</div>
								</div>
							</div>
					</fieldset>
					</div>
				</div>
					<fieldset>
						<div class="row">
						<div class="row m-t-20">
							<div class="col-md-12">
								<div class="form-group">
									<label for="type" class="col-md-4 control-label"></label>
									<div class="col-md-7 col-sm-7">
										<table>
									<tr>
<%-- 										<c:if test="${needOTPVerification == true}"> --%>
<%-- 											<td><button id="${modelName}-sendOTPBtn" type="button" class="btn btn-primary" onclick="validateAndSendOTP('${appContextName}/admin/user/validate','${modelName}','','${modelName}')"><span class="glyphicon glyphicon-ok"></span>&nbsp;&nbsp;Send OTP</button></td> --%>
<%-- 											<td><button id="${modelName}-re-enterOtp" type="button" class="btn btn-primary"  onclick="reenterOTP()"><span class="glyphicon glyphicon-ok"></span>&nbsp;&nbsp;Re-Enter OTP</button></td> --%>
<%-- 										</c:if> --%>
										<td><button id="${modelName}-formSubmitBtn" type="submit" class="btn btn-primary" ><span class="glyphicon glyphicon-ok"></span>&nbsp;&nbsp;Save</button></td>
										
										<%-- <c:if test="${not empty param['id']}">
											<td><a class="btn btn-inverse deleteButton" data-id="${model.id}">
												<i class="fa fa-trash"></i>&nbsp;Delete
											</a></td>
										</c:if> --%>
										<td><a id="form-cancel-btn"  
											class="btn btn-primary "> <i class="fa -square-o fa-times"></i>&nbsp;Cancel
										</a></td>
<%-- 										<c:if test="${needOTPVerification == true}"> --%>
<!-- 												<td> -->
<%-- 												<button id="${modelName}-formSubmitBtn" --%>
<%-- 														type="button" class="btn btn-primary entityFormSubmitBtn" onclick="validateAndSubmit('${appContextName}/admin/user/validate')" > --%>
<!-- 														<span class="glyphicon glyphicon-ok"></span>&nbsp;&nbsp;Save -->
<!-- 												</button> -->
<!-- 												</td> -->
<%-- 											</c:if> --%>
									</tr>	
									</table>
									</div>
								</div>
							</div>
							</div>
						</div>
					</fieldset>
		</form:form>
	</div>
</div>
<!-- end row -->
<%-- <form action="delete" name="deleteForm" method="post">
	<input type="hidden"  name="${_csrf.parameterName}"   value="${_csrf.token}"/>
    <input type="hidden" name="id" />
</form> --%>
<link rel="stylesheet" type="text/css" href="${resourcesBase}/vendors/password-indicator/css/password-indicator.css" />
<script src="${resourcesBase}/vendors/password-indicator/js/password-indicator.js"></script>
<style>
.btn-primary{
margin-right:4px;
}

</style>
<script type="text/javascript">
    $(document).ready(function() {
    	$('.selectpicker').selectpicker();
    	$('#dob').datepicker({
    		format: 'yyyy-mm-dd',
            todayHighlight: true,
            calendarWeeks: true,
        });
    	
    	var passwordValue =  '${model.password}';
    	var roleValue = '${model.role}';

    	console.log("------------Itereate All CheckBox------------");
    	$("input[name='role']").each( function () {
    	    console.log("Role Name : " + $(this).val());
            var isContain = roleValue.includes($(this).val());
    		if(isContain) {
    			console.log("Yes Contain");
    			$(this).prop('checked', true);
    		}else {
    			console.log("No Contain");
    			$(this).prop('checked', false);
    		}
            
    	});
    	
    	console.log("Roles : " + roleValue);
//     	Roles : [Accountant, Trustee]
//     	var n = str.includes("test"); 
    	
    	$('#password').passwordStrength({targetDiv: '#passwordStrength'});
        $('#repassword').passwordStrength({targetDiv: '#rePasswordStrength'});
        $('input[name=password]').on('change', function(){
       	 if($(this).val().length > 0){
       		$("#repassword").attr('data-parsley-required', 'true');
       	 }else{
       		$("#repassword").val('');
       		$("#repassword").parsley().destroy();
       		$("#repassword").attr('data-parsley-required', 'false');
       	 }
        });
        showHide('${model.kyc1FileName}','.kyc1FileName');
        showHide('${model.kyc2FileName}','.kyc2FileName');
        showHide('${model.kyc3FileName}','.kyc3FileName');
        showHide('${model.declarationsSignatureFileName}','.signatureFileName');
        
        $('#sendOtpIdBtn').val('${modelName}');
        if(${sendOtp == true}) {
        	$("#password").val(passwordValue);
        	$("#repassword").val(passwordValue);

//         	sendOTP('${modelName}','','${modelName}');
        	showOTPModel('${modelName}','${modelName}','${model.verificationToken}');
		}
        
    });
    function showHide(fileName,className) {
    	if(fileName){
    		$(className).closest('.form-group').next().find('.hideDownloadDelete').removeClass('hide');
    		$(className).closest('.form-group').find('.hideDownloadDelete').removeClass('hide');
    	} else {
    		$(className).closest('.form-group').find('.showAddFile').removeClass('hide');
    	}
    }
    $('input[type=file]').change(function(){
		var filename = $(this).val().split('\\').pop();
		var filenameContainer = (typeof $(this).data("filename-container") === "undefined") ? $(this).parent().parent().parent().find("#filename") : $("#"+$(this).data("filename-container"));
		
		if(($('input[type=file]').attr('class').includes('userFile'))) {
	        var fileExtension = ['jpeg','jpg','png','pdf'];
		    if ($.inArray($(this).val().split('.').pop().toLowerCase(), fileExtension) == -1) {
		    	alert('Invalid File Type')
		    } else {	
			        $(filenameContainer).html(filename);
		            $(filenameContainer).attr('class','btn btn-success');
		            
		            if(($(this).attr('class').includes('kyc1FileName'))){
		    			$('#kycFileName1').val(filename);
		    		}
		    		if(($(this).attr('class').includes('kyc2FileName'))){
		    			$('#kycFileName2').val(filename);
		    		}
		    		if(($(this).attr('class').includes('kyc3FileName'))){
		    			$('#kycFileName3').val(filename);
		    		}
		    		if(($(this).attr('class').includes('signatureFileName'))){
		    			$('#bankSignatureFileName1').val(filename);
		    		}
			    }
		} else {
		        $(filenameContainer).html(filename);
				}
		 $(filenameContainer).removeAttr("style");
	});
     
     var valueArr = [];
     $(".deleteFile").on("click",function(){
        	if (confirm('Confirm Delete?')){
         	$(this).closest('.form-group').find('.hideDownloadDelete').addClass('hide');
             $(this).closest('.form-group').prev().find('.hideDownloadDelete').addClass('hide');
             $(this).closest('.form-group').prev().find('.showAddFile').removeClass('hide');
             valueArr.push($(this).val());
             document.getElementById('deleteFile').value = valueArr;

        	}
 
       }); 
            
     
     $("#kycStatus")
     .on("change", function() {
     	if($("#kycStatus").val() == 'SubmissionIssues'){
     		$("#kycIssueNote").attr("data-parsley-required","true");
     		
     	}else{
     		$("#kycIssueNote").attr("data-parsley-required","false");
     	}
     });

     $("#bankDetailsStatus")
     .on("change", function() {
     	if($("#bankDetailsStatus").val() == 'SubmissionIssues'){
     		$("#bankDetailUserIssueNoteId").attr("data-parsley-required","true");
     	}else{
     		$("#bankDetailUserIssueNoteId").attr("data-parsley-required","false");
     	}
     });
     
    
</script>

<%@include file="../footer.jspf" %>
