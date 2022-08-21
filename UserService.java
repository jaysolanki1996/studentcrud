package my.airo.roboadvisor.common.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jxl.write.WriteException;
import my.airo.roboadvisor.banking.enums.BankCountry;
import my.airo.roboadvisor.banking.enums.InvestorRemittanceStatus;
import my.airo.roboadvisor.banking.model.Remittance;
import my.airo.roboadvisor.banking.service.RemittanceService;
import my.airo.roboadvisor.common.dao.UserCashAccountDao;
import my.airo.roboadvisor.common.dao.UserDao;
import my.airo.roboadvisor.common.dto.ManagementDataReportDto;
import my.airo.roboadvisor.common.dto.UserStatusDto;
import my.airo.roboadvisor.common.enums.AccountStatus;
import my.airo.roboadvisor.common.enums.BankDetailsStatus;
import my.airo.roboadvisor.common.enums.Currency;
import my.airo.roboadvisor.common.enums.KycDocType;
import my.airo.roboadvisor.common.enums.KycStatus;
import my.airo.roboadvisor.common.enums.Role;
import my.airo.roboadvisor.common.enums.TransactionStatus;
import my.airo.roboadvisor.common.enums.TransactionType;
import my.airo.roboadvisor.common.model.OTPTransaction;
import my.airo.roboadvisor.common.model.User;
import my.airo.roboadvisor.common.model.UserCashAccount;
import my.airo.roboadvisor.infra.dto.ErrorDto;
import my.airo.roboadvisor.infra.dto.ErrorsDto;
import my.airo.roboadvisor.infra.dto.FilterDto;
import my.airo.roboadvisor.infra.dto.FilterDto.Operator;
import my.airo.roboadvisor.infra.dto.PagingDto;
import my.airo.roboadvisor.infra.dto.UserSessionDto;
import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.exception.ValidateException;
import my.airo.roboadvisor.infra.helper.PropertiesHelper;
import my.airo.roboadvisor.infra.helper.notification.NotificationType;
import my.airo.roboadvisor.infra.helper.notification.UserNotificationDispatcher;
import my.airo.roboadvisor.infra.helper.sms.SMSSender;
import my.airo.roboadvisor.infra.service.AbstractService;
import my.airo.roboadvisor.infra.service.AsynchronousTask;
import my.airo.roboadvisor.infra.service.UserSessionService;
import my.airo.roboadvisor.infra.utils.FileUtils;
import my.airo.roboadvisor.infra.utils.HashUtils;
import my.airo.roboadvisor.infra.utils.StringUtils;
import my.airo.roboadvisor.infra.utils.ValidationUtils;
import my.airo.roboadvisor.portfolio.service.UserPortfolioCashflowService;
import my.airo.roboadvisor.trade.enums.BuySell;

@Service
public class UserService extends AbstractService<User> {

    private Logger logger = Logger.getLogger(UserService.class);

    public UserService() {
        super(User.class);
    }

    @Autowired
    public void setDao(UserDao dao) {
        super.dao = dao;
    }

    @Autowired
    private SMSSender smsSender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private UserNotificationDispatcher userNotificationDispatcher;

    @Autowired
    private UserSubmissionService userSubmissionService;

    @Autowired
    private UserCashAccountDao userCashAccountDao;

    @Autowired
    private UserPortfolioCashflowService userPortfolioCashflowService;

    @Autowired
    private RemittanceService remittanceService;
    
    @Autowired
    private OTPTransactionService otpTransactionService;
    
    @Autowired
    private UserSessionService userSessionService;
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public User createUser(User user) {
		
		boolean isNeedToSendOtp = user.getNeedOTPVerification();
		user.setNeedToSendOTP(isNeedToSendOtp);
		user = save(user);

		if (isNeedToSendOtp) {
			OTPTransaction transaction = otpTransactionService.retrievePendingTransaction(user.getMobileNumber(),
					TransactionStatus.Pending, TransactionType.MOBILE_VERIFICATION);
			transaction.setUser(user);
			transaction.setEntityId(user.getId());
			transaction.setEntityName(user.getClass().getSimpleName());
			transaction.setStatus(TransactionStatus.Completed);
			otpTransactionService.saveWithoutValidation(transaction);
		}

		return user;
	}

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
   	public User submitBank(User user,String verificationToken) {
    	
    	user=save(user);

		OTPTransaction transaction = otpTransactionService.retrieveByVerificationToken(verificationToken);
		transaction.setStatus(TransactionStatus.Completed);
		transaction.setUser(user);
		transaction.setEntityId(user.getId());
		otpTransactionService.saveWithoutValidation(transaction);
		
		return user;
		
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public User submitKyc(User user,String verificationToken,ErrorsDto errors) {
    	
		try {
			if (!errors.hasErrors()) {
				validateVerificationToken(verificationToken, TransactionType.KYC, TransactionStatus.Pending,
						User.class.getSimpleName(), user.getMobileNumber(), errors);
				if (!errors.hasErrors()) {
					user.setKycStatus(KycStatus.Submitted);
					save(user);
				}
			}
		} catch (ValidateException e) {
			errors = e.getErrors();
			for (ErrorDto errorDto : errors.getErrors()) {
				if (errorDto.getErrorField().equals("kyc1FileName")) {
					user.setKyc1FileName(null);
				} else if (errorDto.getErrorField().equals("kyc2FileName")) {
					user.setKyc2FileName(null);
				} else if (errorDto.getErrorField().equals("kyc3FileName")) {
					user.setKyc3FileName(null);
				}
			}
		} finally {
			if (errors.hasErrors()) {
				user.setKycStatus(KycStatus.SubmissionIssues);
				saveWithoutPrePost(user);
				throw new ValidateException(errors);
			}
		}
          
  		OTPTransaction transaction = otpTransactionService.retrieveByVerificationToken(verificationToken);
  		transaction.setStatus(TransactionStatus.Completed);
  		transaction.setUser(user);
  		transaction.setEntityId(user.getId());
  		otpTransactionService.saveWithoutValidation(transaction);
  		
  		return user;
    }
    
    public Map<String,Object> validateUploadAndConvertBase64FileToMultipart(String kycDocType,String kycContentType,String userId,ErrorsDto errors,
    		String kycDocField,String kycDocContentTypeField) {
    	
    	Map<String,Object> kycFileDetailMap=new HashMap<>();
    	
    	 if (!ValidationUtils.getInstance().isEmptyString(kycContentType) && !ValidationUtils.getInstance().isEmptyString(kycDocType)) {
             String kycFileName = userId + "_"+kycDocField.toLowerCase();
             kycFileName = FileUtils.getInstance().setFileExtensionUsingContentType(kycFileName, kycContentType);
             if (!ValidationUtils.getInstance().isEmptyString(FilenameUtils.getExtension(kycFileName))) {
                 MultipartFile multipartFile = FileUtils.getInstance().convertBase64ToMultipartFile(kycDocType, kycFileName, kycContentType);
                 if (multipartFile != null) {
                	 kycFileDetailMap.put("multipartFile", multipartFile);
                	 kycFileDetailMap.put("kycFileName", kycFileName);
                 } else {
                     errors.add(new ErrorDto(kycDocField, "error.file.notfound"));
                     logger.info(kycFileName + " file not found. userId = " + userId);
                 }
             } else {
                 errors.add(new ErrorDto(kycDocContentTypeField, "error.invalid", kycDocContentTypeField));
                 logger.info(kycFileName + " is invalid. kycContentTyp e= "+kycContentType+" , userId = " + userId);
             }
         }
    	 
    	 return kycFileDetailMap;
    }
    
    public User getSystemUser() {
        return this.retrieveByEmailAddress(propertiesHelper.systemUserEmailId);
    }

    public List<String> retrieveBase64Image(String... fileNames) {
        List<String> result = new ArrayList<>(fileNames.length);
        List<String> errors = new ArrayList<>(fileNames.length);

        // Retrieve Images from S3 Bucket.
        for (String fileName : fileNames) {
            if (!ValidationUtils.getInstance().isEmptyString(fileName)) {
                try {
                    InputStream io = attachmentHelper.retrieveFileInputStream(new User().getFilePath(), fileName);
                    byte[] byteArray = IOUtils.toByteArray(io);
                    String encoded = Base64.getEncoder().encodeToString(byteArray);
                    result.add(encoded);
                } catch (SystemException | IOException e) {
                    errors.add(e.getMessage());
//                    logger.error(e.getMessage(), e);
                    logger.warn("Error Occurs while retriving image : " +fileName);
                }
            }
        }

        if (result.size() == 0 && errors.size() > 0) {
            result.add("File Not Found.");
        }
        return result;
    }

    @Override
    public ErrorsDto validateForSave(User model, ErrorsDto errors) throws ValidateException {
        errors = super.validateForSave(model, errors);

        if (ValidationUtils.getInstance().isEmptyString(model.getFirstName())) {
            errors.add(new ErrorDto("Name", "error.required", "Name"));
        }

        if (ValidationUtils.getInstance().isEmptyString(model.getEmail())) {
            errors.add(new ErrorDto("email", "error.required", "Email address"));
        } else if (!ValidationUtils.getInstance().isValidEmailAddress(model.getEmail())) {
            errors.add(new ErrorDto("email", "error.invalid", "Email address"));
        } else if(model.getEmail().length() > PropertiesHelper.EMAIL_MAX_LENGTH) {
        	errors.add(new ErrorDto("email", "error.maxlength", "Email address",PropertiesHelper.EMAIL_MAX_LENGTH));
        }

        // uniqueness check for Email Address
        User user = retrieveByEmailAddress(model.getEmail());
        if (model.getCreatingNewObject()) {
            if (user != null) {
                errors.add(new ErrorDto("email", "error.alreadyused", "Email address"));
            }
        } else {
            if (user != null && !user.getId().equals(model.getId())) {
                errors.add(new ErrorDto("email", "error.alreadyused", "Email address"));
            }
        }

        // Add validation for password
        if (model.getCreatingNewObject() && ValidationUtils.getInstance().isEmptyString(model.getSocialId())) {
            if (ValidationUtils.getInstance().isEmptyString(model.getPassword())) {
                errors.add(new ErrorDto("password", "error.required", "Password"));
            } else {
                if (model.getPassword().length() < 8) {
                    errors.add(new ErrorDto("password", "error.minlength", "Password", 8));
                }
            }
        }

        if (!model.getCreatingNewObject()) {
            if (org.apache.commons.lang.StringUtils.isNotBlank(model.getPassword())) {
                if (model.getPassword().length() != 44) {
                    model.setNeedToRehashPassword(true);
                    if (model.getPassword().length() < 8) {
                        errors.add(new ErrorDto("password", "error.minlength", "Password", 8));
                    } else if (ValidationUtils.getInstance().isEmptyString(model.getRepassword())) {
                        errors.add(new ErrorDto("repassword", "error.required", "Password (again)"));
                    }
                }
            }
        }
        
        if (!ValidationUtils.getInstance().isEmptyString(model.getPassword())) {
            boolean isValidPassword = ValidationUtils.getInstance().validatePassword(model.getPassword());
            if (!isValidPassword) {
                errors.add(new ErrorDto("password", "message.passwordValidation", "Password"));
            }
        }
        
        validateUploadFile(errors, model);
        
        if (KycStatus.Submitted.equals(model.getKycStatus())) {
        	
        	if (ValidationUtils.getInstance().isEmptyString(model.getIcNumber())) {
				errors.add(new ErrorDto("icNumber", "error.required", "IcNumber"));
			}

			if(model.getResidenceCountry() == null) {
				errors.add(new ErrorDto("residenceCountry", "error.required", "Residence Country"));
			}
			
			String specialCharacters = "[" + "-/@#!*$%^&.'_+={}()" + "]+";
			if (ValidationUtils.getInstance().isEmptyString(model.getIcNumber())) {
				errors.add(new ErrorDto("icNumber", "error.required", "IcNumber"));
			} else if (model.getIcNumber().matches(specialCharacters)) {
				errors.add(new ErrorDto("icNumber", "error.invalid", "IcNumber"));
			}
        }
        
        if (KycStatus.Completed.equals(model.getKycStatus())) {
             
            checkDeletedFile(model);
            if (ValidationUtils.getInstance().isEmptyString(model.getKyc1FileName())) {
                errors.add(new ErrorDto("kyc1FileName", "error.required", "Identity Card (Front) "));
            }
            if (model.getKycDocType() == null) {
                errors.add(new ErrorDto("kycDocType", "error.required", "kycDocType"));
            } else if (KycDocType.IdentityCard.equals(model.getKycDocType()) && ValidationUtils.getInstance().isEmptyString(model.getKyc2FileName())) {
                errors.add(new ErrorDto("kyc2FileName", "error.required", "Identity Card (Back) "));
            }

            if (ValidationUtils.getInstance().isEmptyString(model.getKyc3FileName())) {
                errors.add(new ErrorDto("kyc3FileName", "error.required", "Proof-of-Residence"));
            }
            
            if(model.getResidenceDocDate() == null) {
            	errors.add(new ErrorDto("residenceDocDate", "error.required", "Date Of Residence-Proof"));
            }
        }
        if (model.getResidenceDocDate() != null) {
            if (model.getResidenceDocDate().isAfter(LocalDate.now().plusDays(1)) || model.getResidenceDocDate().isBefore(LocalDate.now().minusMonths(3))) {
                errors.add(new ErrorDto("residenceDocDate", "error.invalid", "Date Of Residence-Proof"));
            }
        }
        // TODO need to discuss with client
//        if (KycStatus.Completed.equals(model.getKycStatus()) && !ValidationUtils.getInstance().isEmptyString(model.getDeclarationsSignatureFileName())) {
//            File signatureFile = new File(propertiesHelper.appUploads + File.separator + model.getDeclarationsSignatureFileName());
//            checkUploadedFile(errors, signatureFile, model.getDeclarationsSignatureFileName(), "signatureFileName");         
//        }
        if (KycStatus.SubmissionIssues.equals(model.getKycStatus()) && ValidationUtils.getInstance().isEmptyString(model.getKycIssueNote())) {
            errors.add(new ErrorDto("kycIssueNote", "error.required", "KYC Issue note"));
        }

        if (BankDetailsStatus.SubmissionIssues.equals(model.getBankDetailsStatus()) && ValidationUtils.getInstance().isEmptyString(model.getBankIssueNote())) {
            errors.add(new ErrorDto("bankIssueNote", "error.required", "Bank Issue note"));
        }

        if (model.getMonthlyIncome() == null) {
			errors.add(new ErrorDto("monthlyIncome", "error.required", "Monthly Income"));
		}
        
        // Validate MobileNumber.
        if (ValidationUtils.getInstance().isEmptyString(model.getMobileNumber())) {
            errors.add(new ErrorDto("mobileNumber", "error.required", "Mobile Number"));
        } else if (!model.getMobileNumber().matches(PropertiesHelper.MOBILE_NUMBER_REGEX)) {
            errors.add(new ErrorDto("mobileNumber", "error.invalid", "Mobile Number"));
        } else if (model.getMobileNumber().length() < PropertiesHelper.MOBILE_MIN_LENGTH) {
            errors.add(new ErrorDto("mobileNumber", "error.minlength", "Mobile Number", PropertiesHelper.MOBILE_MIN_LENGTH));
        } else if (model.getMobileNumber().length() > PropertiesHelper.MOBILE_MAX_LENGTH) {
            errors.add(new ErrorDto("mobileNumber", "error.maxlength", "Mobile Number", PropertiesHelper.MOBILE_MAX_LENGTH));
        } else {
            // Uniqueness check for Phone
            User phoneUser = retrieveByPhone(model.getMobileNumber());
            if (phoneUser != null && !phoneUser.getId().equals(model.getId())) {
                errors.add(new ErrorDto("mobileNumber", "error.duplicate.value", "User", "mobile number"));
            } else if (model.getCreatingNewObject() && /*propertiesHelper.appIsSendNotification &&*/ model.getNeedToSendOTP()) {
                OTPTransaction transaction = otpTransactionService.retrievePendingTransaction(model.getMobileNumber(), TransactionStatus.Pending, TransactionType.MOBILE_VERIFICATION);
                if (transaction == null) {
                    errors.add(new ErrorDto("mobileNumber", "message.otp.verification.required", new Object[] {}));
                } else if (ValidationUtils.getInstance().isEmptyString(transaction.getEntityName()) || !User.class.getSimpleName().equalsIgnoreCase(transaction.getEntityName())) {
                    errors.add(new ErrorDto("mobileNumber", "message.otp.verification.required", new Object[] {}));
                }
            }
        }
		
		if(BankDetailsStatus.Completed.equals(model.getBankDetailsStatus())) {
			if (model.getBankCountry() == null) {
				errors.add(new ErrorDto("bankCountry", "error.required", "Bank Country"));
			}
			
			if(ValidationUtils.getInstance().isEmptyString(model.getBankName())) {
				errors.add(new ErrorDto("bankName", "error.required", "BankName"));
			}
			
			if(ValidationUtils.getInstance().isEmptyString(model.getBankAccountNumber())) {
				errors.add(new ErrorDto("bankAccountNumber", "error.required", "Bank Account Number"));
			}
			
			if(ValidationUtils.getInstance().isEmptyString(model.getBankAccountName())) {
				errors.add(new ErrorDto("bankAccountName", "error.required", "Bank Account Name"));
			}
			
			if (!BankCountry.MALAYSIA.equals(model.getBankCountry())) {
				if (ValidationUtils.getInstance().isEmptyString(model.getBankAddress())) {
					errors.add(new ErrorDto("bankAddress", "message.non.malaysian.bank.address.is.required"));
				}

				if (ValidationUtils.getInstance().isEmptyString(model.getBankSwiftNumber())) {
					errors.add(new ErrorDto("bankSwiftNumber", "message.non.malaysian.bank.swift.is.required"));
				}
			}
		}
		
		if(BankDetailsStatus.Submitted.equals(model.getBankDetailsStatus())) {
			if(model.getAccountCurrencyType()==null) {
				errors.add(new ErrorDto("accountCurrencyType", "error.required", "Account Currency Type"));
			}
			
			if(ValidationUtils.getInstance().isEmptyString(model.getSourceOfIncome())) {
				errors.add(new ErrorDto("sourceOfIncome", "error.required", "Source Of Income"));
			}
		}
//		if (!ValidationUtils.getInstance().isEmptyString(model.getBankName())) {
//			List<RemittanceBank> banks = remittanceBankService.getRemittanceBanks(model.getBankName());
//			if (banks.isEmpty() || (model.getBankCountry() != null	&& remittanceBankService.getRemittanceBank(model.getBankCountry(), model.getBankName()) == null)) {
//				errors.add(new ErrorDto("bankName", "error.invalid", "BankName"));
//			}
//		}

		UserSessionDto usersession = userSessionService.get();
        if(usersession != null) {
        	User sessionUser = usersession.getUser();
        	if(sessionUser != null && sessionUser.getRole() != null && !sessionUser.getRole().contains(Role.Admin)) {
        		if(!model.getCreatingNewObject()) {
        			User dbUser = getUserAndEvictIt(model.getId());
        			if(dbUser.getRole() != null && !dbUser.getRole().equals(model.getRole()) || !dbUser.getIsAdmin().equals(model.getIsAdmin())) {
        				errors.add(new ErrorDto("message.permission.error", new Object[] {}));
        			}
        		}else if((model.getRole() != null && model.getRole().size() > 0) || Boolean.TRUE.equals(model.getIsAdmin())){
        			errors.add(new ErrorDto("message.permission.error", new Object[] {}));
        		}
        	}
        }
		
        return errors;

    }

	public void checkDeletedFile(User model) {
		
		if (!ValidationUtils.getInstance().isEmptyString(model.getDeleteFile())) {

			String[] deleteFileNameArray = model.getDeleteFile().split(",");

			for (String deleteFileName : deleteFileNameArray) {

				if (deleteFileName.equalsIgnoreCase(model.getKyc1FileName())) {
					model.setKyc1FileName(null);
				} else if (deleteFileName.equalsIgnoreCase(model.getKyc2FileName())) {
					model.setKyc2FileName(null);
				} else if (deleteFileName.equalsIgnoreCase(model.getKyc3FileName())) {
					model.setKyc3FileName(null);
				} else if (deleteFileName.equalsIgnoreCase(model.getDeclarationsSignatureFileName())) {
					model.setDeclarationsSignatureFileName(null);
				}

				attachmentHelper.deleteFile(model.getFilePath(), deleteFileName);
			}
		}
	}

    @Override
    public User preSave(User model) {

        model = super.preSave(model);
        checkDeletedFile(model);

        if (model.getCreatingNewObject() && model.getAdmin() && CollectionUtils.isEmpty(model.getRole())) {

            Set<Role> roles = new HashSet<>();
            roles.add(Role.Admin);
            model.setRole(roles);
        }

        if (model.getCreatingNewObject()) {
            model.setEmail(model.getEmail().toLowerCase());
        }

        if (model.getHashSalt() == null) {
            //model.setHashSalt(StringUtils.getInstance().generateRandomToken(32, true));
            model.setHashSalt(StringUtils.getInstance().generateRandomUUID());
            String hashed = HashUtils.getInstance().getHash(model.getPassword() != null ? model.getPassword() : "", model.getHashSalt());
            model.setPassword(hashed);
        }

        if (model.getPassword() == null) {
            String oldPassword = ((UserDao) dao).retrievePassword(model.getId());
            model.setPassword(oldPassword);
        } else {
            if (!model.getCreatingNewObject() && model.getNeedToRehashPassword()) {
                String hashed = HashUtils.getInstance().getHash(model.getPassword(), model.getHashSalt());
                model.setPassword(hashed);
            }
        }

        if (!model.getCreatingNewObject()) {
            userSubmissionService.logUserChange(model);
            
            User dbUser = getUserAndEvictIt(model.getId());
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("email", dbUser.getEmail());
            if(!ValidationUtils.getInstance().isEmptyString(model.getEmail()) && !dbUser.getEmail().equals(model.getEmail())) {
            	dataMap.put("image", propertiesHelper.appContextResourcePath + "/images/email/change-email.png");
            	userNotificationDispatcher.sendResetVerificationNotification(model, dataMap, NotificationType.EmailChangeRequest);
            }else if(!ValidationUtils.getInstance().isEmptyString(model.getMobileNumber()) && (ValidationUtils.getInstance().isEmptyString(dbUser.getMobileNumber()) || !dbUser.getMobileNumber().equals(model.getMobileNumber()))) {
            	dataMap.put("image", propertiesHelper.appContextResourcePath + "/images/email/change-phone.png");
            	dataMap.put("oldPhNumber", dbUser.getMobileNumber());
            	model.setMobileVerified(false);
            	userNotificationDispatcher.sendResetVerificationNotification(model, dataMap,  NotificationType.PhoneChangeRequest);
            	userNotificationDispatcher.sendPhoneVerificationNotification(model, dataMap,  NotificationType.PhoneVerification);
            }
        }
        
        if (model.getAuthenticationAttempts() != null && model.getAuthenticationAttempts().compareTo(Integer.valueOf(0)) > 0 && AccountStatus.Approved.equals(model.getAccountStatus()) && userSessionService.get() != null
                && userSessionService.get().getUser() != null && userSessionService.get().getUser().getIsAdmin()) {
            model.setAuthenticationAttempts(0);
        }
         
        userNotificationDispatcher.notifyChange(model);
        
        return model;
    }
    
    public void exportAllUsersAsExcel(Report report, ManagementDataReportDto.Report model, HttpServletRequest request, HttpServletResponse response) throws WriteException, IOException {
        boolean isAdminRoleLoggedIn = userSessionService.get() != null && userSessionService.get().getUser() != null && userSessionService.get().getUser().getIsAdmin() && userSessionService.get().getUser().getRole() != null
                && userSessionService.get().getUser().getRole().contains(Role.Admin);
        List<FilterDto> filterDto = new ArrayList<FilterDto>(1);
        if (!isAdminRoleLoggedIn) {
            filterDto.add(new FilterDto("isAdmin", Operator.EQ, new Object[] { Boolean.FALSE }));
        }
        exportAsExcel(filterDto, report, model, request, response);
    }
    
	public String validateUploadFile(ErrorsDto errors, String userId, String kycFileName, String errorField,
			String filePath, MultipartFile multipartFile, String kycDocType) {

		if (validateFile(errors, multipartFile.getSize(), kycFileName, errorField)) {
			kycFileName = null;
		} else {
			String extension = attachmentHelper.getFormatNameFromContentType(multipartFile.getContentType());
			String fileName = userId + "_" + kycDocType + "."+ extension;
			boolean uploaded = uploadAttachment(fileName, filePath, multipartFile);
			if (uploaded) {
				kycFileName = fileName;
			}
		}

		return kycFileName;
	}
	
    public void validateUploadFile(ErrorsDto errors, User model) {
    	String kycFileName=null;
    	List<String> deletedFileList = null;
		
		if (!ValidationUtils.getInstance().isEmptyString(model.getDeleteFile())) {
			String[] deletedFileArray = model.getDeleteFile().split(",");
			deletedFileList = new ArrayList<String>(Arrays.asList(deletedFileArray));
		} else {
			deletedFileList = new ArrayList<>();
		}
    			 
        for (MultipartFile multipartFile : model.getMultipartFiles()) {
            if (multipartFile.getOriginalFilename().equals(model.getKyc1FileName())) {
            	
            	kycFileName=validateUploadFile(errors, model.getId(), model.getKyc1FileName(), "kyc1FileName", model.getFilePath(), multipartFile, "kycfront");
            	model.setKyc1FileName(kycFileName);
            	deletedFileList.remove(kycFileName);
            }
            if (multipartFile.getOriginalFilename().equals(model.getKyc2FileName())) {
            	
            	kycFileName=validateUploadFile(errors, model.getId(), model.getKyc2FileName(), "kyc2FileName", model.getFilePath(), multipartFile, "kycback");
            	model.setKyc2FileName(kycFileName);
            	deletedFileList.remove(kycFileName);
            }
            if (multipartFile.getOriginalFilename().equals(model.getKyc3FileName())) {
                if(model.getResidenceDocDate() != null && model.getResidenceDocDate().isAfter(LocalDate.now().plusDays(1))) {
                    errors.add(new ErrorDto("residenceDocDate", "error.invalid", "Date Of Residence-Proof"));
                }
                
                kycFileName=validateUploadFile(errors, model.getId(), model.getKyc3FileName(), "kyc3FileName", model.getFilePath(), multipartFile, "residence");
            	model.setKyc3FileName(kycFileName);
            	deletedFileList.remove(kycFileName);
            }
            if (multipartFile.getOriginalFilename().equals(model.getDeclarationsSignatureFileName())) {
                
            	kycFileName=validateUploadFile(errors, model.getId(), model.getDeclarationsSignatureFileName(), "declarationsSignatureFileName", model.getFilePath(), multipartFile, "signature");
            	model.setDeclarationsSignatureFileName(kycFileName);
            	deletedFileList.remove(kycFileName);
            }
        }
        
        model.setDeleteFile(String.join(",", deletedFileList));
    }
    
    
    private boolean uploadAttachment(String filename, String filePath, MultipartFile multipartFile) {
        attachmentHelper.saveFile(multipartFile, filePath, filename);
        InputStream io = attachmentHelper.retrieveFileInputStream(filePath, filename);
        if (io != null) {
            return true;
        }
        return false;
    }

    @Override
    public User postSave(User model) {
        model = super.postSave(model);
        if (model.getCreatingNewObject()) {
            createUserCashAccount(model);
            userNotificationDispatcher.sendEmailNewUserCreated(model);
        }

        return model;
    }
    
    private void createUserCashAccount(User model) {
        UserCashAccount cashAccount = new UserCashAccount();
        cashAccount.setUser(model);
        userCashAccountDao.save(cashAccount);
    }

    public User retrieveByEmailAddress(String emailAddress) {
        return ((UserDao) dao).retrieveByEmailAddress(emailAddress);
    }

    // get particular user base on id.
    public User getUser(String id) {
        return ((UserDao) dao).getUser(id);
    }

    /**
     * This is required since we need to load existing user from DB and compare it with updatedModel which requires two objects with same ID
     * And it cause NonUniqueObjectException: A different object with the same identifier value was already associated with the session
     * @param id userId
     * @return evicted User Object
     */
    public User getUserAndEvictIt(String id) {
        final User user = ((UserDao) dao).getUser(id);
        dao.evict(user);
        return user;
    }

    public User retrieveByEmailAddressAndPassword(String emailAddress, String password) {
        User ret = ((UserDao) dao).retrieveByEmailAddress(emailAddress);
        if (ret != null && password != null) {
            // check password
            String hashed = HashUtils.getInstance().getHash(password, ret.getHashSalt());
            // is it the same?
            if (hashed.equals(ret.getPassword())) {
                return ret;
            }
        }
        return null;
    }

    public User retrieveByPhoneOrEmail(String emailOrPhone, String password) {
        User ret = ((UserDao) dao).retrieveByPhone(emailOrPhone);
        if (ret == null) {
            ret = ((UserDao) dao).retrieveByEmailAddress(emailOrPhone);
        }
        if (ret != null && password != null) {
            // check password
            String hashed = HashUtils.getInstance().getHash(password, ret.getHashSalt());
            // is it the same?
            if (hashed.equals(ret.getPassword())) {
                return ret;
            }
        }
        return null;
    }

    public boolean isEmailAddressUsed(String emailAddress) {
        return ((UserDao) dao).isEmailAddressUsed(emailAddress);
    }

    public User retrieveByPhone(String phone) {
        return ((UserDao) dao).retrieveByPhone(phone);
    }

//    public void updateLastLoggedIn(String userId, String ipAddress, String userAgent) {
//        ((UserDao) dao).updateLastLoggedIn(userId, ipAddress, userAgent);
//    }


    public User retrieveByToken(String token) {
        return ((UserDao) dao).retrieveByToken(token);
    }

    public User saveForFBLogin(User model) {
        return ((UserDao) dao).save(model);
    }

    public void sendNewMessageNotification(final User finalModel) {
        if (finalModel != null) {
            if (!ValidationUtils.getInstance().isEmptyString(finalModel.getEmail())) {
                // send out html email
                new AsynchronousTask(userOperationContextService) {
                    @Override
                    public void body() throws Exception {
                        String content = new String(FileUtils.getInstance().getBytes(getClass().getClassLoader().getResourceAsStream("email_tpl/user_new_message_notification.html")));

                        // variables
                        content = StringUtils.getInstance().replace(content, "{{name}}", finalModel.getEmail());
                        String signInUrlEndpoint = propertiesHelper.appWebUrlSignIn;
                        content = StringUtils.getInstance().replace(content, "{{signInLink}}", securityService.generateSecureLink(finalModel, signInUrlEndpoint, null, true,null));

                        mailSenderHelper.sendMail(finalModel.getEmail() + ", you received a new message!", new String[] { finalModel.getEmail() }, content, true, false);
                    }
                }.execute();
            }
        }
    }

    public User getUserBySocialId(String apiKey, String emailAddress) {
        User user = ((UserDao) dao).getUserByFBApiKey(apiKey, emailAddress);
        return user;
    }

//    public User sendOtpToAgent(User user, Agent agent) {
//        boolean response = false;
//        String agentOtp = null;
//        if (ValidationUtils.getInstance().isEmptyString(user.getAgentOtp())) {
//            agentOtp = StringUtils.getInstance().generateRandomTokenInDigits(4);
//            response = sendCodeToNexmo(agent.getName(), agent.getMobileNumber(), agentOtp);
//        } else {
//            agentOtp = user.getAgentOtp();
//            response = sendCodeToNexmo(agent.getName(), agent.getMobileNumber(), agentOtp);
//
//        }
//
//        user.setAgentOtp(agentOtp);
//        if (response) {
//            user.setAgentOTPStatus(AgentOTPStatus.SentToAgent);
//            user.setAgent(agent);
//        } else {
//            user.setAgentOTPStatus(AgentOTPStatus.NotCompleted);
//        }
//        saveWithoutPrePost(user);
//        return user;
//    }

    public boolean sendCodeToNexmo(String agentName, String phoneNumber, String agentOtp) {
        Map<String, String> data = new HashMap<>();
        data.put("name", agentName);
        data.put("number", phoneNumber);
        data.put("code", agentOtp);
        StringWriter writer = new StringWriter();
        try {
            String templateString = IOUtils.toString(resourceLoader.getResource("classpath:email_tpl" + File.separator + "otp_message_template.html").getInputStream(),"UTF8");
            velocityEngine.evaluate(new VelocityContext(data), writer, "", templateString);
        } catch (IOException e) {
        	throw new SystemException(e);
        }
        String content = writer.toString();
        return smsSender.sendSms(phoneNumber, content);
    }

    public boolean otpVerification(User user, String otp) {
        boolean otpVerified = false;
//        user.setAgentOTPStatus(AgentOTPStatus.OtpVerificationFailed);
//        if (ValidationUtils.getInstance().isEmptyString(user.getAgentOtp()))
//            return false;
//        if (user.getAgentOtp().equals(otp)) {
//            user.setAgentOTPStatus(AgentOTPStatus.Completed);
//            otpVerified = true;
//            // Removed PrivateBVI for OTP people
//            // user.setPortfolioCategory(PortfolioAssignmentCategory.PrivateBVI);
//        }
        saveWithoutPrePost(user);
        return otpVerified;
    }

    public ErrorsDto validateBankDetails(String name, String accountName, String accountNumber, ErrorsDto errors) {
        if (ValidationUtils.getInstance().isEmptyString(name)) {
            errors.add(new ErrorDto("name", "error.required", "Bank Name"));
        }
        if (ValidationUtils.getInstance().isEmptyString(accountName)) {
            errors.add(new ErrorDto("accountName", "error.required", "Account Name"));
        }
        if (ValidationUtils.getInstance().isEmptyString(accountNumber)) {
            errors.add(new ErrorDto("accountNumber", "error.required", "Account Number"));
        }

        return errors;
    }

    public PagingDto<User> retrieveForListPage(String[] accountStatus, String[] kycStatus, String[] bankDetailsStatus, PagingDto<User> pagingDto, Boolean isAdmin, String emailAddress) {

        return ((UserDao) dao).retrieveForListPage(accountStatus, kycStatus, bankDetailsStatus, pagingDto, isAdmin, emailAddress);
    }

    public boolean isAgentInUse(String agentId) {
        return ((UserDao) dao).isAgentUsed(agentId);
    }

//    @Override
//    public void delete(User model) {
//        super.delete(model);
//        String filePath = model.getFilePath();
//        try {
//        	if (model.getKyc1FileName() != null) {
//                attachmentHelper.deleteFile(filePath, model.getKyc1FileName());
//            }
//            if (model.getKyc2FileName() != null) {
//            	attachmentHelper.deleteFile(filePath, model.getKyc2FileName());
//            }
//            if (model.getKyc3FileName() != null) {
//            	attachmentHelper.deleteFile(filePath, model.getKyc3FileName());
//            }
//            if (model.getDeclarationsSignatureFileName() != null) {
//            	attachmentHelper.deleteFile(filePath, model.getDeclarationsSignatureFileName());
//            }
//        }catch(SystemException e) {
////        	logger.error(e.getMessage(), e);
//        	logger.warn("Error Occurs while delete image");
//        }
//        
//    }

    public List<User> retrieveByRole(List<Role> roles) {
        return ((UserDao) dao).retrieveByRole(roles);
    }

    public List<User> retrieveByEmail(String emailAddress) {
        return ((UserDao) dao).retrieveByEmail(emailAddress);
    }
    
    public PagingDto<User> retrieveUsers(PagingDto<User> pagingDto, Boolean isAdmin) {
        return ((UserDao) dao).retrieveUsers(pagingDto,isAdmin);
    }

    public void inActiveUsersIfTheirPortfolioNotFundedForYear(LocalDate date) {
        if (date.isLeapYear()) {
            date = date.minusDays(propertiesHelper.inActiveUserRxpireDays + 1);
        } else {
            date = date.minusDays(propertiesHelper.inActiveUserRxpireDays);
        }

        List<User> users = getListByCreatedOnAndAccountStatus(date, AccountStatus.Approved);

        if (!CollectionUtils.isEmpty(users)) {
            for (User user : users) {
                List<Remittance> userRemittances = remittanceService.getRemittancesByUserId(user.getId());
                if (!CollectionUtils.isEmpty(userRemittances)) {
                    Optional<Remittance> optionalUserRemittance = userRemittances.stream().filter(p -> InvestorRemittanceStatus.Completed.equals(p.getInvestorRemittanceStatus())).findFirst();
                    if (!optionalUserRemittance.isPresent()) {
                        user.setAccountStatus(AccountStatus.InActive);
                        save(user);
                        String subject = "Security email - User account status is updated to InActive";
                        String body = user.getFirstName() + " " + user.getLastName() + "'s account is updated to InActive as he/she has not funded his/her account since last 1 year.";
                        // securityHelper.sendSecurityNotificationEmailToTechTeam(subject, body);
                        userNotificationDispatcher.sendSecurityNotificationEmailAsUserAccountStatusIsUpdated(user.getFirstName(),user.getLastName(), user.getEmail(), AccountStatus.InActive);
                    }
                } else {
                    user.setAccountStatus(AccountStatus.InActive);
                    save(user);
                    String subject = "Security email - User account status is updated to InActive";
                    String body = user.getFirstName() + " " + user.getLastName() + "'s account is updated to InActive as he/she has not funded his/her account since last 1 year.";
                    // securityHelper.sendSecurityNotificationEmailToTechTeam(subject, body);
                    userNotificationDispatcher.sendSecurityNotificationEmailAsUserAccountStatusIsUpdated(user.getFirstName(),user.getLastName(), user.getEmail(), AccountStatus.InActive);
                }
            }
        }
    }

    public List<User> getListByCreatedOnAndAccountStatus(LocalDate date, AccountStatus accountStatus) {
        return ((UserDao) dao).getListByCreatedOnAndAccountStatus(date, accountStatus);
    }

    public List<User> getListByAccountStatus(AccountStatus accountStatus) {
        return ((UserDao) dao).getListByAccountStatus(accountStatus);
    }
    
    public void initializeUser(User user) {
        if (!((UserDao) dao).isInitialized(user)) {
            ((UserDao) dao).initializeUser(user);
        }
    }
    
    public ErrorsDto validatePagingParams(String pageNumberStr, String pageSizeStr, ErrorsDto error) {

        if (!ValidationUtils.getInstance().isEmptyString(pageNumberStr) && !ValidationUtils.getInstance().isInteger(pageNumberStr)) {
            error.add(new ErrorDto(PropertiesHelper.API_PAGE_NUMBER_PARAM, "error.invalid", PropertiesHelper.API_PAGE_NUMBER_PARAM));
        }

        if (!ValidationUtils.getInstance().isEmptyString(pageSizeStr) && !ValidationUtils.getInstance().isInteger(pageSizeStr)) {
            error.add(new ErrorDto(PropertiesHelper.API_PAGE_SIZE_PARAM, "error.invalid", PropertiesHelper.API_PAGE_SIZE_PARAM));
        }
        return error;
    }

    public void paymentInstructionNotification(String userPortfolioId, BigDecimal amount, Currency curreny) {
        userNotificationDispatcher.notifyPaymentInstruction(userPortfolioId, amount, curreny);
    }
    
    public Boolean hasPerformedFirstFunding(String userId) {
        Boolean hasPerformedFirstFunding = Boolean.FALSE;
        List<Remittance> remittances = remittanceService.getRemittancesByUserId(userId);
        if (!CollectionUtils.isEmpty(remittances)) {
            hasPerformedFirstFunding = Boolean.TRUE;
        }
        return hasPerformedFirstFunding;
    }

    public UserStatusDto getUserStatusDto(User user) {
        UserStatusDto userStatusDto = new UserStatusDto();
        String status = propertiesHelper.getContextMessage("message.kyc.submission");
        String color = UserStatusDto.AMBER;
        if (KycStatus.Completed.equals(user.getKycStatus()) && !BankDetailsStatus.Completed.equals(user.getBankDetailsStatus())) {
            status = propertiesHelper.getContextMessage("message.bank.details");
        } else if (BankDetailsStatus.Completed.equals(user.getBankDetailsStatus())) {
            status = propertiesHelper.getContextMessage("message.fund.portfolio");
        }

        List<Remittance> remittances = remittanceService.getRemittancesByUserId(user.getId());
        Remittance remittance = null;
        if (!CollectionUtils.isEmpty(remittances)) {
            // get last remittance
            remittance = remittances.get(remittances.size() - 1);
            if (InvestorRemittanceStatus.Draft.equals(remittance.getInvestorRemittanceStatus()) || InvestorRemittanceStatus.Submitted.equals(remittance.getInvestorRemittanceStatus())
                    || InvestorRemittanceStatus.Received.equals(remittance.getInvestorRemittanceStatus())) {
                status = propertiesHelper.getContextMessage("message.fund.inprogress");
            } else if (InvestorRemittanceStatus.Issues.equals(remittance.getInvestorRemittanceStatus())) {
                status = remittance.getRemittanceIssueNote();
                color = UserStatusDto.RED;
            } else if (InvestorRemittanceStatus.Completed.equals(remittance.getInvestorRemittanceStatus())) {
                // check status of last cash flow
                Boolean isLastCashFlowProcessed = userPortfolioCashflowService.checkLastCashflowProcessedOrNotByUserIdAndStatus(user.getId(), BuySell.Buy);
                if (isLastCashFlowProcessed) {
                    status = propertiesHelper.getContextMessage("message.fund.completed");
                    color = UserStatusDto.GREEN;
                } else {
                    status = propertiesHelper.getContextMessage("message.fund.inprogress");
                }
            }
        }

        if (KycStatus.SubmissionIssues.equals(user.getKycStatus()) && !ValidationUtils.getInstance().isEmptyString(user.getKycIssueNote())) {
            status = user.getKycIssueNote();
            color = UserStatusDto.RED;
        } else if (BankDetailsStatus.SubmissionIssues.equals(user.getBankDetailsStatus()) && !ValidationUtils.getInstance().isEmptyString(user.getBankIssueNote())) {
            status = user.getBankIssueNote();
            color = UserStatusDto.RED;
        }

        userStatusDto.setKyc(user.getKycStatus());
        userStatusDto.setBankDetails(user.getBankDetailsStatus());
        userStatusDto.setStatus(status.trim());
        userStatusDto.setColor(color);
        return userStatusDto;
    }
    
}
