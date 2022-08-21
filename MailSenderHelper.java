package my.airo.roboadvisor.infra.helper;

import java.io.File;
import java.io.UnsupportedEncodingException;

/*
 * Some SMTP servers require a username and password authentication before you
 * can use their Server for Sending mail. This is most common with couple
 * of ISP's who provide SMTP Address to Send Mail.
 *
 * This Program gives any example on how to do SMTP Authentication
 * (User and Password verification)
 *
 * This is a free source code and is provided as it is without any warranties and
 * it can be used in any your code for free.
 *
 * Author : Sudhir Ancha
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import my.airo.roboadvisor.common.model.User;
import my.airo.roboadvisor.infra.controller.AbstractController;
import my.airo.roboadvisor.infra.exception.SystemException;
import my.airo.roboadvisor.infra.utils.StringUtils;

@Component
public class MailSenderHelper extends AbstractHelper {

	protected Logger logger = Logger.getLogger(MailSenderHelper.class);
	
	@Autowired
    private PropertiesHelper propertiesHelper;
    
    ExecutorService executorService = Executors.newCachedThreadPool();

    @PreDestroy
    private void distroy() {
        executorService.shutdown();
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(AbstractController.DATE_TIME_FORMAT_FOR_PRINT);

    public String sendMailToUser(final String subject, final String recipients[], final String message, final boolean isHtml, final User user, boolean log) {

        return sendMailToUser(subject, recipients, message, isHtml, user, log, null, null, null);
    }

//    public String sendMailToUser(String subject, InternetAddress recipientTo, String message, boolean isHtml) {
//
//        return sendMailToUser(subject, recipientTo, null, null, null, message, null, isHtml);
//    }

//    public String sendMailToUser(String subject, InternetAddress recipientTo, InternetAddress[] recipientsBcc, InternetAddress from, String message, boolean isHtml) {
//
//        return sendMailToUser(subject, recipientTo, recipientsBcc, null, from, message, null, isHtml);
//    }

//    public String sendMailToUser(final String subject, final InternetAddress recipientTo, final InternetAddress recipientsBcc[], final InternetAddress recipientsCc[], final InternetAddress from, final String message, final List<File> attachments,
//            final boolean isHtml) {
//        return sendMailToUser(subject, new InternetAddress[]{ recipientTo }, recipientsBcc, recipientsCc, from, message, attachments, isHtml, new InternetAddress[]{});
//    }

    public String sendMailToUser(final String subject, final InternetAddress recipientTo[], final InternetAddress recipientsBcc[], final InternetAddress recipientsCc[], final InternetAddress from, final String message, final List<File> attachments,
            final boolean isHtml, final InternetAddress replyTo[]) {

        //logger.info("sending email to: " + Arrays.asList(recipientTo).toString() + " on " + dateFormat.format(new Date()) + ", subject: " + subject + ", content: " + message);

        if (propertiesHelper.appIsSendEmail) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    doSendMail(subject, recipientTo, recipientsBcc, recipientsCc, from, message, attachments, isHtml, replyTo);
                }
            });
        }

        //return "sending email to: " + Arrays.asList(recipientTo).toString() + " on " + dateFormat.format(new Date()) + ", subject: " + subject + ", content: " + message;
        return null;
    }

//    public String sendMailToUser(final String subject, final InternetAddress recipientTo, final InternetAddress recipientsBcc[], final InternetAddress recipientsCc[], final InternetAddress from, final String message, final List<File> attachments,
//            final boolean isHtml, AsyncCallback callback) {
//        return sendMailToUser(subject, new InternetAddress[] {recipientTo}, recipientsBcc, recipientsCc, from, message, attachments, isHtml, new InternetAddress[]{}); 
//    }

    public String sendMailToUser(String subject, final String recipients[], final String message, final boolean isHtml, final User user, boolean log, final String CCRecipients[], final String BCCRecipients[], final List<File> AttachmentFile) {
        
        InternetAddress[] recipientsBcc = null;
        InternetAddress[] recipientsCc = null;
        InternetAddress[] recipientsTo = null;
        try {
            if (CCRecipients != null) {
                recipientsCc = new InternetAddress[CCRecipients.length];
                for (int i = 0; i < CCRecipients.length; i++)
                    recipientsCc[i] = new InternetAddress(CCRecipients[i]);
            }
            if (BCCRecipients != null) {
                recipientsBcc = new InternetAddress[BCCRecipients.length];
                for (int i = 0; i < BCCRecipients.length; i++)
                    recipientsBcc[i] = new InternetAddress(BCCRecipients[i]);
            }
            if (recipients != null) {
                recipientsTo = new InternetAddress[recipients.length];
                for (int i = 0; i < recipients.length; i++)
                    recipientsTo[i] = new InternetAddress(recipients[i]);
            }
        } catch (AddressException e) {
            userOperationContextService.error(e);
        }
        
        return sendMailToUser(subject, recipientsTo,recipientsBcc, recipientsCc, null, message, AttachmentFile, isHtml, new InternetAddress[]{});
    }

    public String sendMailToUser(String subject, String recipients[], String message, boolean isHtml, User user) {
        return sendMailToUser(subject, recipients, message, isHtml, user, true, null, null, null);
    }

    public String sendMail(String subject, String recipients[], String message, boolean isHtml, boolean log) {
        return sendMailToUser(subject, recipients, message, isHtml, null, log, null, null, null);

    }

    public String sendMail(String subject, String recipients[], String message, boolean isHtml) {
        return sendMailToUser(subject, recipients, message, isHtml, null, true, null, null, null);
    }

//    private void doSendMail(String subject, String recipients[], String message, boolean isHtml, String CCRecipients[], String BCCRecipients[], List<File> AttachmentFile) {
//
//        try {
//            InternetAddress[] recipientsBcc = null;
//            InternetAddress[] recipientsCc = null;
//            InternetAddress from = null;
//
//            InternetAddress[] addressTo = new InternetAddress[recipients.length];
//            for (int i = 0; i < recipients.length; i++)
//                addressTo[i] = new InternetAddress(recipients[i]);
//
//            if (CCRecipients != null) {
//                recipientsCc = new InternetAddress[CCRecipients.length];
//                for (int i = 0; i < CCRecipients.length; i++)
//                    recipientsCc[i] = new InternetAddress(CCRecipients[i]);
//            }
//            if (BCCRecipients != null) {
//                recipientsBcc = new InternetAddress[BCCRecipients.length];
//                for (int i = 0; i < BCCRecipients.length; i++)
//                    recipientsBcc[i] = new InternetAddress(BCCRecipients[i]);
//            }
//
//            doSendMail(subject, addressTo, recipientsBcc, recipientsCc, from, message, AttachmentFile, isHtml);
//        } catch (AddressException e) {
//        	logger.error(e);
//            userOperationContextService.warn(e);
//        }
//
//    }

//    private void doSendMail(String subject, InternetAddress recipientTo, InternetAddress recipientsBcc[], InternetAddress[] recipientsCc, InternetAddress from, String message, List<File> attachments, boolean isHtml) {
//        doSendMail(subject, new InternetAddress[] { recipientTo }, recipientsBcc, recipientsCc, from, message, attachments, isHtml);
//    }

//    private void doSendMail(String subject, InternetAddress recipientTo[], InternetAddress recipientsBcc[], InternetAddress[] recipientsCc, InternetAddress from, String message, List<File> attachments, boolean isHtml) {
//
//        doSendMail(subject, recipientTo, recipientsBcc, recipientsCc, from, message, attachments, isHtml, null);
//    }

    private void doSendMail(String subject, InternetAddress recipientTo[], InternetAddress recipientsBcc[], InternetAddress[] recipientsCc, InternetAddress from, String message, List<File> attachments, boolean isHtml, InternetAddress[] replyTo) {

        if (recipientTo == null || recipientTo.length == 0)
            return;

        if (message == null)
            message = "";

        boolean debug = false;

        // Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", propertiesHelper.mailHost);
        props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.socketFactory.port", propertiesHelper.mailPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        // props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", propertiesHelper.mailPort);
        props.put("mail.smtp.submitter", propertiesHelper.mailBounce);

        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getInstance(props, auth);
        session.setDebug(debug);
        
        // create a message
        Message msg = new MimeMessage(session);
        BodyPart messageBodyPart = new MimeBodyPart();
        try {
            if (recipientsBcc != null) {
                ArrayList<InternetAddress> bccRecipientList = new ArrayList<InternetAddress>();
                for (int i = 0; i < recipientsBcc.length; i++) {
                    InternetAddress bccAddress = recipientsBcc[i];
                    if (bccAddress != null) {
                        bccRecipientList.add(bccAddress);
                    }
                }

                recipientsBcc = bccRecipientList.toArray(new InternetAddress[bccRecipientList.size()]);
            }
            // set the from and to address
            if (from == null) {
                from = new InternetAddress(propertiesHelper.mailFrom);
                from.setPersonal(propertiesHelper.mailFromName);
            }
            msg.setFrom(from);

            msg.setRecipients(Message.RecipientType.TO, recipientTo);
            msg.setRecipients(Message.RecipientType.CC, recipientsCc);
            msg.setRecipients(Message.RecipientType.BCC, recipientsBcc);

            /*if (null != replyTo) {
                msg.setReplyTo(replyTo);
            }*/
            msg.setReplyTo(InternetAddress.parse(propertiesHelper.supportEmail));
            // Setting the Subject and Content Type
            if(!propertiesHelper.appIsProduction) {
            	String subjectPrefix = "[" + StringUtils.getInstance().getFirstToken(propertiesHelper.appDomainName, ".") + "] ";
            	subject = subjectPrefix + subject; 
            }
            msg.setSubject(subject);
            String type = isHtml ? "text/html; charset=utf-8" : "text/plain; charset=utf-8";
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, type);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            //logger.info("-------------------- doSendMail CONTENT: ------------------" + message);

            if (recipientsBcc != null && recipientsBcc.length > 0) {
                String bcc = "";
                for (InternetAddress address : recipientsBcc) {
                    bcc += " " + address.getAddress();
                }

             //   logger.info("-------------------- doSendMail BCC: ------------------" + bcc);
            }

            if (recipientsCc != null && recipientsCc.length > 0) {

                String cc = "";
                for (InternetAddress address : recipientsCc) {
                    cc += " " + address.getAddress();
                }

             //   logger.info("-------------------- doSendMail CC: ------------------" + cc);
            }
            if (recipientTo != null && recipientTo.length > 0) {

                String to = "";
                for (InternetAddress address : recipientTo) {
                    to += " " + address.getAddress();
                }

              //  logger.info("-------------------- doSendMail To: ------------------" + to);
            }

            // Attachment
            if (attachments != null) {
                for (File file : attachments) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.attachFile(file);
                    multipart.addBodyPart(attachPart);
                }

            }

            msg.setContent(multipart);
            Transport.send(msg);
        } catch (Exception e) {
        	throw new SystemException(e);
        }
    }

    public String sendMail(String subject, String recipients[], String message, boolean isHtml, String CCRecipients[], String BCCRecipients[], List<File> AttachmentFile) {
        return sendMailToUser(subject, recipients, message, isHtml, null, true, CCRecipients, BCCRecipients, AttachmentFile);
    }

    /**
     * SimpleAuthenticator is used to do simple authentication when the SMTP
     * server requires it.
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = propertiesHelper.mailUsername;
            String password = propertiesHelper.mailPassword;
            return new PasswordAuthentication(username, password);
        }
    }

    public InternetAddress convertToInternetAddress(String emailAddress) {
        InternetAddress inetAdd = null;
        try {
            inetAdd = new InternetAddress(emailAddress);
        } catch (AddressException e) {
            userOperationContextService.error(e);
        }
        return inetAdd;
    }

    public InternetAddress convertToInternetAddress(String emailAddress, String name) {
        try {
            return new InternetAddress(emailAddress, name);
        } catch (UnsupportedEncodingException e) {
        	 throw new SystemException(e);
        }
    }
}
