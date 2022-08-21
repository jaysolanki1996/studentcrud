package my.airo.roboadvisor.common.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import my.airo.roboadvisor.banking.enums.BankCountry;
import my.airo.roboadvisor.banking.model.Redemption;
import my.airo.roboadvisor.banking.model.Remittance;
import my.airo.roboadvisor.banking.service.RedemptionService;
import my.airo.roboadvisor.banking.service.RemittanceService;
import my.airo.roboadvisor.common.dto.ManagementDataReportDto;
import my.airo.roboadvisor.common.enums.AccountCurrencyType;
import my.airo.roboadvisor.common.enums.AccountStatus;
import my.airo.roboadvisor.common.enums.AgentOTPStatus;
import my.airo.roboadvisor.common.enums.BankDetailsStatus;
import my.airo.roboadvisor.common.enums.DeclarationStatus;
import my.airo.roboadvisor.common.enums.KycDocType;
import my.airo.roboadvisor.common.enums.KycStatus;
import my.airo.roboadvisor.common.enums.MonthlyIncome;
import my.airo.roboadvisor.common.enums.ResidenceCountry;
import my.airo.roboadvisor.common.enums.Role;
import my.airo.roboadvisor.common.enums.UserProgressStatus;
import my.airo.roboadvisor.common.model.DueDilligence;
import my.airo.roboadvisor.common.model.User;
import my.airo.roboadvisor.common.model.UserCashAccount;
import my.airo.roboadvisor.common.model.UserSubmission;
import my.airo.roboadvisor.common.service.AgentService;
import my.airo.roboadvisor.common.service.DueDilligenceService;
import my.airo.roboadvisor.common.service.UserCashAccountService;
import my.airo.roboadvisor.common.service.UserService;
import my.airo.roboadvisor.common.service.UserSubmissionService;
import my.airo.roboadvisor.infra.controller.CRUDController;
import my.airo.roboadvisor.infra.controller.aspect.SessionLookup;
import my.airo.roboadvisor.infra.dto.FormOptionDto;
import my.airo.roboadvisor.infra.dto.PagingDto;
import my.airo.roboadvisor.infra.enums.UserOperationContextResultType;
import my.airo.roboadvisor.infra.exception.NoRightException;
import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.model.AbstractModel;
import my.airo.roboadvisor.infra.utils.StringUtils;
import my.airo.roboadvisor.infra.utils.SystemUtils;
import my.airo.roboadvisor.infra.utils.ValidationUtils;
import my.airo.roboadvisor.portfolio.enums.PortfolioAssignmentCategory;
import my.airo.roboadvisor.portfolio.model.UserPortfolio;
import my.airo.roboadvisor.portfolio.service.UserPortfolioService;
import my.airo.roboadvisor.trade.model.UserTrade;
import my.airo.roboadvisor.trade.service.UserTradeService;

@Controller
@RequestMapping("/admin/user")
@SessionLookup(role = { Role.Accountant, Role.Admin, Role.CustomerSupport, Role.PortfolioManager })
public class UserController extends CRUDController<User, UserService> {

    private Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    public void setService(UserService service) {
        this.service = service;
    }

    @Autowired
    public UserController(UserService service) {
        super(User.class, service);
    }

    @Autowired
    protected UserPortfolioService userportfolioService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private RemittanceService remittanceService;

    @Autowired
    private RedemptionService redemptionService;

    @Autowired
    private UserSubmissionService userSubmissionService;

    @Autowired
    private UserCashAccountService userCashAccountService;

    @Autowired
    private UserTradeService userTradeService;
    
    @Autowired
    private DueDilligenceService dueDilligenceService;

    @Override
    public Object[] preCreateUpdateGet(User model, HttpServletRequest request) {
        String isNonUser = request.getParameter("nu");
        if ((isNonUser != null) && isNonUser.equals("true")) {
            model.setNeedToRehashPassword(true);
        }

        List<FormOptionDto> residenceContryList = new ArrayList<>();
        for (ResidenceCountry country : ResidenceCountry.values()) {
            residenceContryList.add(new FormOptionDto(country.name(), country.getCountryName()));
        }

        List<FormOptionDto> monthlyIncomeList = new ArrayList<>();
        for (MonthlyIncome monthlyIncome : MonthlyIncome.values()) {
        	monthlyIncomeList.add(new FormOptionDto(monthlyIncome.name(), monthlyIncome.getLabel()));
        }
        
              
        List<FormOptionDto> accountCurrencyTypeList = new ArrayList<>();
        for (AccountCurrencyType accountCurrencyType : AccountCurrencyType.values()) {
        	accountCurrencyTypeList.add(new FormOptionDto(accountCurrencyType.name(), accountCurrencyType.getLabel()));
        }
        
        List<FormOptionDto> agentOtpStatusList = new ArrayList<>();
        for (AgentOTPStatus agentOTPStatus : AgentOTPStatus.values()) {
            agentOtpStatusList.add(new FormOptionDto(agentOTPStatus.name(), agentOTPStatus.getLabel()));
        }

        List<FormOptionDto> kycTypeList = new ArrayList<>();
        for (KycDocType kycDocType : KycDocType.values()) {
            kycTypeList.add(new FormOptionDto(kycDocType.name(), kycDocType.getLabel()));
        }

        List<FormOptionDto> kycStatusList = new ArrayList<>();
        for (KycStatus kycStatus : KycStatus.values()) {
            kycStatusList.add(new FormOptionDto(kycStatus.name(), kycStatus.getLabel()));
        }

        List<FormOptionDto> bankDetailStatusList = new ArrayList<>();
        for (BankDetailsStatus bankDetailStatus : BankDetailsStatus.values()) {
            bankDetailStatusList.add(new FormOptionDto(bankDetailStatus.name(), bankDetailStatus.getLabel()));
        }

        List<FormOptionDto> userProgressStatusList = new ArrayList<>();
        for (UserProgressStatus userProgressStatus : UserProgressStatus.values()) {
            userProgressStatusList.add(new FormOptionDto(userProgressStatus.name(), userProgressStatus.getLabel()));
        }

        List<FormOptionDto> roles = new ArrayList<>();
        for (Role role : Role.values()) {
            roles.add(new FormOptionDto(role.name(), role.getLabel()));
        }

        List<FormOptionDto> portfolioAssignmentCategoryList = new ArrayList<>();
        for (PortfolioAssignmentCategory portfolioAssignmentCategory : PortfolioAssignmentCategory.values()) {
            portfolioAssignmentCategoryList.add(new FormOptionDto(portfolioAssignmentCategory.name(), portfolioAssignmentCategory.getLabel()));
        }

        List<FormOptionDto> accountStatusOpts = new ArrayList<>();
        for (AccountStatus accountStatus : AccountStatus.values()) {
            accountStatusOpts.add(new FormOptionDto(accountStatus.name(), accountStatus.getLabel()));
        }
        
        List<FormOptionDto> bankContryList = new ArrayList<>();
        for (BankCountry country : BankCountry.values()) {
            bankContryList.add(new FormOptionDto(country.name(), country.getCountryName()));
        }

        PagingDto<UserPortfolio> userPortfolioPagingDto = new PagingDto<>();
        userPortfolioPagingDto = userportfolioService.retriveByUser(model.getId(), userPortfolioPagingDto);
        List<UserPortfolio> userPortfolioList = userPortfolioPagingDto.getResults();
        boolean showMorePortfolio = false;
        if (userPortfolioPagingDto.getResults().size() < userPortfolioPagingDto.getResultsSize()) {
            showMorePortfolio = true;
        }
        for (UserPortfolio userPortfolio : userPortfolioList) {
            BigDecimal fees = userportfolioService.calculateMonthlyFeesForExecutedPortfolio(userPortfolio.getId());
            userPortfolio.setManagementFees(fees);
            List<UserTrade> userTrades = userTradeService.getUserTradeByUserPortfolioId(userPortfolio.getId());
            userPortfolio.setUserTradeList(userTrades);
        }

        Collections.sort(userPortfolioList, Collections.reverseOrder(AbstractModel.COMPARE_BY_CREATION_DATE));
        
        PagingDto<Remittance> remittancePagingDto = new PagingDto<>();
        remittancePagingDto = remittanceService.getRemittancesByUserId(model.getId(),remittancePagingDto);
        List<Remittance> remittanceList = remittancePagingDto.getResults();
        Collections.sort(remittanceList, Collections.reverseOrder(AbstractModel.COMPARE_BY_CREATION_DATE));
        boolean showMoreRemittance = false;
        if (remittancePagingDto.getResults().size() < remittancePagingDto.getResultsSize()) {
            showMoreRemittance = true;
        }

        PagingDto<Redemption> redemptionPagingDto = new PagingDto<>();
        redemptionPagingDto = redemptionService.getListByUser(model.getId(), redemptionPagingDto);
        List<Redemption> redemptionList = redemptionPagingDto.getResults();
        Collections.sort(redemptionList, Collections.reverseOrder(AbstractModel.COMPARE_BY_CREATION_DATE));
        boolean showMoreRedemption = false;
        if (redemptionPagingDto.getResults().size() < redemptionPagingDto.getResultsSize()) {
            showMoreRedemption = true;
        }
        
        List<UserSubmission> userSubmissions = userSubmissionService.getUserSubmissionsByUserId(model.getId());

        UserCashAccount userCashAcccount = userCashAccountService.retrieveByUserId(model.getId());
        
        String dueDilligenceId = null;
        if(model.getId() != null) {
        	DueDilligence dueDilligence = dueDilligenceService.retrieveByUserId(model.getId());
        	if(dueDilligence != null) {
        		model.setApprovalStatus(dueDilligence.getApprovalStatus());
        		model.setSubmissionStatus(dueDilligence.getSubmissionStatus());
        		model.setDueDilligenceEmailSent(dueDilligence.getEmailSent());
        		model.setDueDilligenceLastSubmission(dueDilligence.getLastUserSubmission());
        		dueDilligenceId = dueDilligence.getId();
        	}
        }

        return new Object[] { "model", model, "remittanceList", remittanceList, "redemptionList", redemptionList, "residenceContryList", residenceContryList, "agentOTPStatusList", agentOtpStatusList, "kycTypeList", kycTypeList, "kycStatusList", kycStatusList,
                "bankDetailStatus", bankDetailStatusList, "portfolioAssignmentCategoryList", portfolioAssignmentCategoryList, "accountStatusOpts", accountStatusOpts, "userProgressStatusList", userProgressStatusList, "userSubmissions", userSubmissions,
                "userCashAcccount", userCashAcccount, "userPortfolioPagingDto", userPortfolioPagingDto, "roles", roles, "bankCountryList", bankContryList, "showMorePortfolio", showMorePortfolio, "showMoreRemittance", showMoreRemittance, "showMoreRedemption",
                showMoreRedemption,"monthlyIncomeList",monthlyIncomeList,"accountCurrencyTypeList",accountCurrencyTypeList, "dueDilligenceId", dueDilligenceId};
    }
    
    @Override
	public Object createUpdateGet(@ModelAttribute User model, HttpServletRequest request) {
		if(model.getIsAdmin()) {
			Set<Role> loggedInUserRoles = userSessionService.get() == null || userSessionService.get().getUser() == null ? null
					: userSessionService.get().getUser().getRole();
			if (loggedInUserRoles == null || !loggedInUserRoles.contains(Role.Admin)) {
				return retrieveCommonModelAndView("norights","message.noRightException", request, new NoRightException());
			}
		}
		return super.createUpdateGet(model, request);
	}
    
    @Override
    public Object createUpdatePost(User model, Errors springErrors, HttpServletRequest request) {
    	if(model.getIsAdmin()) {
    		Set<Role> loggedInUserRoles = userSessionService.get() == null || userSessionService.get().getUser() == null ? null
    				: userSessionService.get().getUser().getRole();
        	if(loggedInUserRoles == null || !loggedInUserRoles.contains(Role.Admin)) {
        		return retrieveCommonModelAndView("norights","message.noRightException", request, new NoRightException());
        	}
    	}
    	
    	Object obj = super.createUpdatePost(model, springErrors, request);
        
        if ((userSessionService.get() == null) && model.getCreatingNewObject() && model.getNeedToSendMail() && (springErrors.hasErrors() == false)) {
            userOperationContextService.set(UserOperationContextResultType.Success, super.getContextMessage("message.signup.success"));
            obj = "redirect:../";
        }
        return obj;
    }

    public @ResponseBody Object sendNewMessageNotification(@RequestParam(value = "userId") String userId) {
        service.sendNewMessageNotification(service.retrieve(userId, true));
        return SystemUtils.getInstance().buildMap(new HashMap<String, String>(), "success", true);
    }

    @Override
    @RequestMapping("/list")
    public Object list(@ModelAttribute PagingDto<User> pagingDto, String ids, HttpServletRequest request) {
        String[] queryAccountStatus = request.getParameterValues("accountStatus");
        String[] querykycStatus = request.getParameterValues("kycStatus");
        String[] querybankDetailsStatus = request.getParameterValues("bankDetailsStatus");
        String queryEmailAddress = request.getParameter("email");
        Boolean isAdmin = Boolean.FALSE;
        
        if(StringUtils.getInstance().hasValue(request.getParameter("isAdmin")) && userSessionService.get().getUser().getRole() != null && userSessionService.get().getUser().getRole().contains(Role.Admin) ) {
        	isAdmin = ("on".equals(request.getParameter("isAdmin")) ? true : false);
        }

        pagingDto = service.retrieveForListPage(queryAccountStatus, querykycStatus, querybankDetailsStatus, pagingDto, isAdmin, queryEmailAddress);

        List<FormOptionDto> accountStatusOpts = new ArrayList<>();
        for (AccountStatus accountStatus : AccountStatus.values()) {
            accountStatusOpts.add(new FormOptionDto(accountStatus.name(), accountStatus.getLabel()));
        }
        List<FormOptionDto> kycStatusOpts = new ArrayList<>();
        for (KycStatus kycStatus : KycStatus.values()) {
            kycStatusOpts.add(new FormOptionDto(kycStatus.name(), kycStatus.getLabel()));
        }
        List<FormOptionDto> bankDetailsStatusOpts = new ArrayList<>();
        for (BankDetailsStatus bankDetailsStatus : BankDetailsStatus.values()) {
            bankDetailsStatusOpts.add(new FormOptionDto(bankDetailsStatus.name(), bankDetailsStatus.getLabel()));
        }
        List<FormOptionDto> declarationStatusOpts = new ArrayList<>();
        for (DeclarationStatus declarationStatus : DeclarationStatus.values()) {
            declarationStatusOpts.add(new FormOptionDto(declarationStatus.getLabel(), declarationStatus.getLabel()));
        }
        return modelAndView(getFullJspPath("list"), "list", pagingDto, "accountStatusOpts", accountStatusOpts, "kycStatusOpts", kycStatusOpts, "bankDetailsStatusOpts", bankDetailsStatusOpts, "declarationStatusOpts",
                declarationStatusOpts, "isAdmin", isAdmin);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/verifyOtp" }, method = RequestMethod.POST)
    public Object veryfyOtp(@RequestParam("userId") String userId, @RequestParam("agentOtp") String agentOtp) {
        User model = service.getUser(userId);
        Boolean otpVerifiedStatus = service.otpVerification(model, agentOtp);
        return otpVerifiedStatus;
    }

    @ResponseBody
    @RequestMapping("/downloadfile")
    public void downloadFile1(@RequestParam(value = "id") String id, @RequestParam(value = "fileType") String fileType, HttpServletResponse response) {

        if (!ValidationUtils.getInstance().isEmptyString(id) && !ValidationUtils.getInstance().isEmptyString(fileType)) {
            ServletOutputStream out = null;
            try {
                User user = service.retrieve(id, false);
                String fileName = "";
                if (user != null) {
                    if ("kyc1FileName".equalsIgnoreCase(fileType)) {
                        fileName = user.getKyc1FileName();
                    } else if ("kyc2FileName".equalsIgnoreCase(fileType)) {
                        fileName = user.getKyc2FileName();
                    } else if ("kyc3FileName".equalsIgnoreCase(fileType)) {
                        fileName = user.getKyc3FileName();
                    } else if ("declarationsSignatureFile".equalsIgnoreCase(fileType)) {
                        fileName = user.getDeclarationsSignatureFileName();
                    }
                    if (!ValidationUtils.getInstance().isEmptyString(fileName)) {
                        out = response.getOutputStream();
                        response.setHeader("content-disposition", "attachment; filename = \"" + fileName + "\"");
                        InputStream io = service.getAttachmentInputStream(user.getFilePath(), fileName);
                        IOUtils.copy(io, out);
                    }
                }

            } catch (IOException e) {
            	throw new SystemException(e);
            } finally {
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
				} catch (IOException e) {
					logger.error("Error while download Image !", e);
				}
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = { "/deletefile" }, method = RequestMethod.GET)
    public Object deleteFile(@RequestParam String fileName, @RequestParam String userId) {
        User user = service.retrieve(userId);

        if (fileName.equalsIgnoreCase(user.getKyc1FileName())) {
            user.setKyc1FileName(null);
        } else if (fileName.equalsIgnoreCase(user.getKyc2FileName())) {
            user.setKyc2FileName(null);
        } else if (fileName.equalsIgnoreCase(user.getKyc3FileName())) {
            user.setKyc3FileName(null);
        } else if (fileName.equalsIgnoreCase(user.getDeclarationsSignatureFileName())) {
            user.setDeclarationsSignatureFileName(null);
        }
        attachmentHelper.deleteFile(user.getFilePath(), fileName);

        user = service.saveWithoutPrePost(user);
        return user;
    }

    // @ResponseBody
    // @RequestMapping(value = { "/getBankNames" }, method = RequestMethod.GET)
    // public Object deleteFile(@RequestParam String country) {
    // String[] banks = new String[] {};
    // boolean success = false;
    // if(!ValidationUtils.getInstance().isEmptyString(country) && !"null".equalsIgnoreCase(country)) {
    // BankCountry bankCountry = BankCountry.getByName(country);
    // if(bankCountry != null && bankCountry.getBankList() != null && bankCountry.getCountryName().length() > 0) {
    // banks = bankCountry.getCountryName().split(",");
    // success = true;
    // }
    // }
    //
    // Map<String,Object> returnMap = SystemUtils.getInstance().buildMap(new LinkedHashMap<String,Object>(), "success",success,"banks",banks);
    // return returnMap;
    // }

    /*@Override
    public Object delete(User model, HttpServletRequest request) {
        service.delete(model);
        return redirect(model, request);
    }*/

    @RequestMapping(value = "/generateReport", method = RequestMethod.POST)
    public void downloadReport(@ModelAttribute ManagementDataReportDto.Report model, HttpServletRequest request, HttpServletResponse response) throws WriteException, IOException {
        service.exportAllUsersAsExcel(((UserService) service).new Report() {

            @Override
            protected void writeRow(WritableSheet sheet, User item, int row) throws WriteException {
                int count = 0;
                sheet.addCell(new Label(count++, row, item.getId()));
                sheet.addCell(new Label(count++, row, item.getCreatedBy()));
                sheet.addCell(new Label(count++, row, item.getUpdatedBy() != null ? item.getUpdatedBy() : " "));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getCreatedOn())));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getUpdatedOn() != null ? item.getUpdatedOn() : " ")));

                sheet.addCell(new Label(count++, row, item.getFirstName()));
                sheet.addCell(new Label(count++, row, item.getLastName()));
                sheet.addCell(new Label(count++, row, item.getEmail()));
                sheet.addCell(new Label(count++, row, item.getSocialId() != null ? item.getSocialId() : " "));
                sheet.addCell(new Label(count++, row, item.getMobileNumber() != null ? item.getMobileNumber() : " "));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getMobileVerified() != null ? item.getMobileVerified() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getMobileVerifiedTimestamp() != null ? item.getMobileVerifiedTimestamp() : " ")));
                sheet.addCell(new Label(count++, row, item.getResidenceCountry() != null ? item.getResidenceCountry().getCountryName() : " "));

                sheet.addCell(new Label(count++, row, String.valueOf(item.getMonthlyIncome() != null ? item.getMonthlyIncome() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getDateOfBirth() != null ? item.getDateOfBirth() : " ")));

                sheet.addCell(new Label(count++, row, item.getLastLoggedInIpAddress() != null ? item.getLastLoggedInIpAddress() : " "));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getIsAdmin() != null ? item.getIsAdmin() : " ")));
                sheet.addCell(new Label(count++, row, item.getBrokerSaxoApiKey() != null ? item.getBrokerSaxoApiKey() : " "));
                sheet.addCell(new Label(count++, row, item.getBrokerSaxoApiSecret() != null ? item.getBrokerSaxoApiSecret() : " "));

                sheet.addCell(new Label(count++, row, item.getAccountStatus() != null ? item.getAccountStatus().getLabel() : " "));
                sheet.addCell(new Label(count++, row, item.getKycStatus() != null ? item.getKycStatus().getLabel() : " "));
                sheet.addCell(new Label(count++, row, item.getKycRemarks() != null ? item.getKycRemarks() : " "));
                sheet.addCell(new Label(count++, row, item.getKyc1FileName() != null ? item.getKyc1FileName() : ""));
                sheet.addCell(new Label(count++, row, item.getKyc2FileName() != null ? item.getKyc2FileName() : ""));
                sheet.addCell(new Label(count++, row, item.getKyc3FileName() != null ? item.getKyc3FileName() : ""));

                sheet.addCell(new Label(count++, row, item.getBankDetailsStatus() != null ? item.getBankDetailsStatus().getLabel() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsRemarks() != null ? item.getBankDetailsRemarks() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsBankName() != null ? item.getBankDetailsBankName() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsBankAddress() != null ? item.getBankDetailsBankAddress() : " "));
//                sheet.addCell(new Label(count++, row, item.getBankDetailsAba() != null ? item.getBankDetailsAba() : " "));
//                sheet.addCell(new Label(count++, row, item.getBankDetailsChips() != null ? item.getBankDetailsChips() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsSwiftNumber() != null ? item.getBankDetailsSwiftNumber() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsAccountName() != null ? item.getBankDetailsAccountName() : " "));
                sheet.addCell(new Label(count++, row, item.getBankDetailsAccountNumber() != null ? item.getBankDetailsAccountNumber() : " "));
                sheet.addCell(new Label(count++, row, item.getAccountCurrencyType() != null ? item.getAccountCurrencyType().getLabel() : " "));
//                sheet.addCell(new Label(count++, row, item.getBankDetailsReference() != null ? item.getBankDetailsReference() : " "));

                sheet.addCell(new Label(count++, row, String.valueOf(item.getHighNetWorthIndividual() != null ? item.getHighNetWorthIndividual() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getDeclarationsPep() != null ? item.getDeclarationsPep() : " ")));
//                sheet.addCell(new Label(count++, row, String.valueOf(item.getDeclarationsCrc() != null ? item.getDeclarationsCrc() : " ")));
//                sheet.addCell(new Label(count++, row, String.valueOf(item.getDeclarationsTaxCrime() != null ? item.getDeclarationsTaxCrime() : " ")));
                sheet.addCell(new Label(count++, row, item.getSourceOfIncome() != null ? item.getSourceOfIncome() : " "));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getDeclarationsUsCitizen() != null ? item.getDeclarationsUsCitizen() : " ")));
                sheet.addCell(new Label(count++, row, item.getDeclarationsSignatureFileName() != null ? item.getDeclarationsSignatureFileName() : " "));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getAgreementUserAgreement() != null ? item.getAgreementUserAgreement() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getAgreementUserAgreementAcknowledged() != null ? item.getAgreementUserAgreementAcknowledged() : " ")));

                sheet.addCell(new Label(count++, row, item.getKycIssueNote() != null ? item.getKycIssueNote() : " "));
                sheet.addCell(new Label(count++, row, item.getBankIssueNote() != null ? item.getBankIssueNote() : " "));

                sheet.addCell(new Label(count++, row, String.valueOf(item.getSignupEmailSent() != null ? item.getSignupEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getKycUploadEmailSent() != null ? item.getKycUploadEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getBankDetailsDeclarationsEmailSent() != null ? item.getBankDetailsDeclarationsEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getKycIssueEmailSent() != null ? item.getKycIssueEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getBankIssueEmailSent() != null ? item.getBankIssueEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getKycCompletedEmailSent() != null ? item.getKycCompletedEmailSent() : " ")));
                sheet.addCell(new Label(count++, row, String.valueOf(item.getBankDetailsDeclarationsStatusCompletedEmailSent() != null ? item.getBankDetailsDeclarationsStatusCompletedEmailSent() : " ")));
            }

            @Override
            protected void writeMoreHeadings(WritableSheet sheet, int row) throws WriteException {
                return;
            }

            @Override
            protected void writeHeadings(WritableSheet sheet, int row) throws WriteException {
                int count = 0;
                sheet.addCell(new Label(count++, row, "ID"));
                sheet.addCell(new Label(count++, row, "Created By"));
                sheet.addCell(new Label(count++, row, "Updated By"));
                sheet.addCell(new Label(count++, row, "Created On"));
                sheet.addCell(new Label(count++, row, "Updated On"));

                sheet.addCell(new Label(count++, row, "First Name"));
                sheet.addCell(new Label(count++, row, "Last Name"));
                sheet.addCell(new Label(count++, row, "Email"));
                sheet.addCell(new Label(count++, row, "Social Id"));
                sheet.addCell(new Label(count++, row, "Mobile Number"));
                sheet.addCell(new Label(count++, row, "Mobile Verified"));
                sheet.addCell(new Label(count++, row, "Mobile Verified Timestamp"));
                sheet.addCell(new Label(count++, row, "Residence Country"));

                sheet.addCell(new Label(count++, row, "Annual Income"));
                sheet.addCell(new Label(count++, row, "Date Of Birth"));

                sheet.addCell(new Label(count++, row, "Agent ID"));
                sheet.addCell(new Label(count++, row, "Agent Otp"));
                sheet.addCell(new Label(count++, row, "Agent OTPStatus"));

                sheet.addCell(new Label(count++, row, "Last Logged In IpAddress"));
                sheet.addCell(new Label(count++, row, "Is Admin"));
                sheet.addCell(new Label(count++, row, "Account Summary"));
                sheet.addCell(new Label(count++, row, "Broker Saxo API Key"));
                sheet.addCell(new Label(count++, row, "Broker API Secret"));
                sheet.addCell(new Label(count++, row, "Progress Status"));
                sheet.addCell(new Label(count++, row, "Progress Status Jump Screen"));

                sheet.addCell(new Label(count++, row, "Account Status"));
                sheet.addCell(new Label(count++, row, "KYC Status"));
                sheet.addCell(new Label(count++, row, "KYC Remarks"));
                sheet.addCell(new Label(count++, row, "Kyc1 File Name"));
                sheet.addCell(new Label(count++, row, "Kyc2 File Name"));
                sheet.addCell(new Label(count++, row, "Kyc3 File Name"));

                sheet.addCell(new Label(count++, row, "Bank Details Status"));
                sheet.addCell(new Label(count++, row, "Bank Details Remarks"));
                sheet.addCell(new Label(count++, row, "Bank Details Bank Name"));
                sheet.addCell(new Label(count++, row, "Banbk Details Bank Address"));
                sheet.addCell(new Label(count++, row, "Bank Details Aba"));
                sheet.addCell(new Label(count++, row, "Bank Details Chips"));
                sheet.addCell(new Label(count++, row, "Bank Details Swift Number"));
                sheet.addCell(new Label(count++, row, "Bank Details Account Name"));
                sheet.addCell(new Label(count++, row, "Bank Detaisl Account Number"));
                sheet.addCell(new Label(count++, row, "Bank Details Reference"));

                sheet.addCell(new Label(count++, row, "Declarations Ai"));
                sheet.addCell(new Label(count++, row, "Declarations Pep"));
                sheet.addCell(new Label(count++, row, "Declarations Crc"));
                sheet.addCell(new Label(count++, row, "Declarations Tax Crime"));
                sheet.addCell(new Label(count++, row, "Declarations Source Of Income"));
                sheet.addCell(new Label(count++, row, "Declarations Us Citizen"));
                sheet.addCell(new Label(count++, row, "Declarations Signature File Name"));
                sheet.addCell(new Label(count++, row, "Agreement User Agreement"));
                sheet.addCell(new Label(count++, row, "Agreement User Agreement Acknowledged"));
                sheet.addCell(new Label(count++, row, "Portfolio Category"));

                sheet.addCell(new Label(count++, row, "KYC Issue Note"));
                sheet.addCell(new Label(count++, row, "Bank Issue Note"));

                sheet.addCell(new Label(count++, row, "Sign Up Email Sent"));
                sheet.addCell(new Label(count++, row, "Kyc  Upload Email Sent"));
                sheet.addCell(new Label(count++, row, "Bank Details Declarations Email Sent"));
                sheet.addCell(new Label(count++, row, "KYC Issue Email Sent"));
                sheet.addCell(new Label(count++, row, "Bank Issue Email Sent"));
                sheet.addCell(new Label(count++, row, "KYC Completed Email Sent"));
                sheet.addCell(new Label(count++, row, "Bank Details Declarations Status Completed Email Sent"));
            }

            @Override
            protected String getSheetName() {
                return getModelName();
            }

            @Override
            protected WritableWorkbook getWorkbook(HttpServletResponse response) throws IOException {
                return super.getWorkbook(response);
            }

        }, model, request, response);
    }
}
