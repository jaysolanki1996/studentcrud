package com.bpmnxmlgenerator.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.bpmnxmlgenerator.infra.controller.AbstractController;
import com.bpmnxmlgenerator.infra.helper.PropertiesHelper;
import com.bpmnxmlgenerator.infra.utils.FileUtils;
import com.bpmnxmlgenerator.service.BpmnService;
import com.bpmnxmlgenerator.utils.ValidationUtils;

/**
 * Controller class use for upload excel document functionality
 */
@Controller
public class BpmnController extends AbstractController {

    private static Logger logger = LogManager.getLogger(BpmnController.class);

    @Autowired
    private BpmnService bpmnService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Method use to return upload page
     *
     * @return upload page
     */
    @RequestMapping({"/", "/upload"})
    public String index() {
        return "upload";
    }

    /**
     * @throws IOException
     * Method use for upload and validate excel document
     *
     * @param files
     * @return
     * @throws
     */
    @PostMapping("/upload")
    public ModelAndView upload(@RequestParam("file") MultipartFile[] files,HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("convertExcelToBpmn controller start");
        boolean newVersion = !ValidationUtils.getInstance().isEmptyString(request.getParameter("version"))
                && "checked".equalsIgnoreCase(request.getParameter("version")) ? true : false;
        // For multiple file upload
        String downloadLink = bpmnService.getProperty(PropertiesHelper.CONTEXT_PATH) + "/downloadfile";
        Map<String, Object> responseData = bpmnService.convertExcelToBpmn(files,!newVersion);
        downloadLink = downloadLink+"?fileName="+responseData.get("xmlFilePath");
        logger.info("convertExcelToBpmn controller end");
         ModelAndView modelAndView = new ModelAndView("upload");

         modelAndView.addObject("success", true);
         modelAndView.addObject("errors", responseData.get("errors"));
         modelAndView.addObject("message",messageSource.getMessage(PropertiesHelper.SUCCESS_MESSAGE,null,LocaleContextHolder.getLocale()));
         modelAndView.addObject("downloadLink",responseData.get("xmlFilePath"));
         logger.info("BPMN File generated successfully");

         return modelAndView;
    }

    /**
     *
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping("/downloadfile")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) {

        String fileName = request.getParameter("fileName");
        if (!ValidationUtils.getInstance().isEmptyString(fileName)) {
            ServletOutputStream out = null;
            try {
            	String bpmnFilePath = bpmnService.getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator+fileName;
                logger.info("File Path "+ bpmnFilePath);
            	File file = new File(bpmnFilePath);
                out = response.getOutputStream();
                response.setHeader("content-disposition", "attachment; filename = \"" + fileName + "\"");
                InputStream io = new FileInputStream(file);
                out.write(FileUtils.getInstance().getBytes(io));
                response.flushBuffer();
            } catch (IOException e) {
                logger.error(e);
            } finally {
                try {
                    if(out != null) {
                    	out.flush();
                        out.close();
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

}
