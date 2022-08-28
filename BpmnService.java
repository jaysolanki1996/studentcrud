package com.bpmnxmlgenerator.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bpmnxmlgenerator.dto.ProcessTableDto;
import com.bpmnxmlgenerator.enums.ShapeType;
import com.bpmnxmlgenerator.helper.BpmnHelper;
import com.bpmnxmlgenerator.infra.dto.ErrorDto;
import com.bpmnxmlgenerator.infra.dto.ErrorsDto;
import com.bpmnxmlgenerator.infra.exception.SystemException;
import com.bpmnxmlgenerator.infra.exception.ValidateException;
import com.bpmnxmlgenerator.infra.helper.ExcelParserHelper;
import com.bpmnxmlgenerator.infra.helper.PropertiesHelper;
import com.bpmnxmlgenerator.utils.ValidationUtils;


/**
 * Service class use for upload excel document functionality
 */
@Service
public class BpmnService {

    private static Logger logger = LogManager.getLogger(BpmnService.class);

    @Autowired
    private Environment env;

    @Autowired
    private BpmnHelper bpmnHelper;

    @Autowired
    ExcelParserHelper excelParserHelper;

    @Autowired
    private MessageSource messageSource;

    Map<String, Object> errorData = new LinkedHashMap<>(2);
    Map<String, Object> response = new LinkedHashMap<>(2);

    private int PADDING_SIZE = 2;
    private String NEW_LINE = "\n";
    private String TABLE_JOINT_SYMBOL = "+";
    private String TABLE_V_SPLIT_SYMBOL = "|";
    private String TABLE_H_SPLIT_SYMBOL = "-";

    /**
     * Used for get Properties from application.properties.
     *
     * @param property
     * @return
     */
    public String getProperty(String property) {
    	return env.getProperty(property);
    }

    /**
     * Use for upload excel document
     *
     * @param file
     * @throws IOException
     * @throws SystemException
     */
    public Map<String, Object> convertExcelToBpmn(MultipartFile[] files,boolean isOverride) throws IOException, SystemException {
        logger.info("convertExcelToBpmn service start");

        // upload directory validation
        File excelDirectory = new File(getProperty(PropertiesHelper.UPLOAD_PATH));
        if (!excelDirectory.exists()) {
            logger.error(messageSource.getMessage("upload.path.error", null, LocaleContextHolder.getLocale()));
            throw new SystemException(
                    messageSource.getMessage("upload.path.error", null, LocaleContextHolder.getLocale()));
        }
        File xmlDirectory = new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH));
        if (!xmlDirectory.exists()) {
            logger.error(messageSource.getMessage("bpmn.xml.path.error", null, LocaleContextHolder.getLocale()));
            throw new SystemException(
                    messageSource.getMessage("bpmn.xml.path.error", null, LocaleContextHolder.getLocale()));
        }

        if (files.length > Integer.parseInt(getProperty(PropertiesHelper.UPLOAD_FILE_COUNT))) {
            logger.error(messageSource.getMessage("upload.file.count.message", new Object[] {getProperty(PropertiesHelper.UPLOAD_FILE_COUNT)}, LocaleContextHolder.getLocale()));
            throw new SystemException(
                    messageSource.getMessage("upload.file.count.message", new Object[] {getProperty(PropertiesHelper.UPLOAD_FILE_COUNT)}, LocaleContextHolder.getLocale()));
        }

        // Validation to check file extension
        errorData = new LinkedHashMap<>(2);
        response = new LinkedHashMap<>(2);
        String xmlFilePath = null;
        String folderName=""+getLocaleTime();
        for (MultipartFile file : files) {
            Map<String, Object> errorMessages = new LinkedHashMap<>(2);
            if (validExtension(file.getOriginalFilename())) {
                byte[] bytes = file.getBytes();

                File uploadExcelDirectory = new File(excelDirectory.getAbsolutePath() + File.separator
                        + "uploaded_excel"
                        + File.separator + folderName);
                if (!uploadExcelDirectory.exists()) {
                    uploadExcelDirectory.mkdirs();
                }

                String filePath = uploadExcelDirectory + File.separator + generateFileName(file.getOriginalFilename());
                File serverFile = new File(filePath);

                String fileExt = fileExtention(file);
                // Check if file is csv or xls based on that create file on server
                if ("csv".equalsIgnoreCase(fileExt)) {
                    Workbook wb = new XSSFWorkbook();
                    CreationHelper helper = wb.getCreationHelper();
                    Sheet sheet = wb.createSheet("new sheet");

                    Reader fileReader = new InputStreamReader(new BOMInputStream(file.getInputStream()), "UTF-8");
                    CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
                    int csvRow = 0;
                    for (CSVRecord csvRecord : csvParser) {
                        Iterator<String> rowIterator = csvRecord.iterator();
                        Row row = sheet.createRow((short) csvRow++);
                        int index = 0;
                        while (rowIterator.hasNext()) {
                            String rowVal = rowIterator.next();
                            row.createCell(index).setCellValue(rowVal);
                            index++;
                        }
                    }

                    // Write the csv output to xlsx file on server
                    BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(serverFile));
                    wb.write(fileOut);
                    fileOut.close();


                } else {
                    // Create the excel file on server
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                    stream.write(bytes);
                    stream.close();
                }
                String fileOriginalName = file.getOriginalFilename();
                String fileName = (fileOriginalName.substring(0, fileOriginalName.lastIndexOf(".")));

                File existingFile = new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator + folderName + File.separator +fileName + ".bpmn");

                if(existingFile.exists() && !isOverride) {
                	fileName = fileName +"_" +getLocaleTime();
                }

                // Get process table list dto from server excel file
                List<ProcessTableDto> processTableDtoList = getProcessTableData(serverFile, fileExt, file.getOriginalFilename(), errorMessages);


               if(!errorData.containsKey(file.getOriginalFilename())) {
                    File bpmnDirectory =
                            new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator + folderName);
                    if (!bpmnDirectory.exists()) {
                        bpmnDirectory.mkdirs();
                    }
                    xmlFilePath = bpmnHelper.generateXMLFileWithLane(processTableDtoList, fileName, folderName);
               }

            } else {
                logger.error(messageSource.getMessage("upload.invalid.format", null, LocaleContextHolder.getLocale()));
                errorMessages.put("", messageSource.getMessage("upload.invalid.format", null, LocaleContextHolder.getLocale()));
                errorData.put(file.getOriginalFilename(), errorMessages);
            }
        }

        if(!errorData.isEmpty()) {
            response.put("errors",errorData);
        }else {
            response.put("errors","");
        }
        if(xmlFilePath != null) {
            response.put("xmlFilePath", "Generated files are store in <b>"
                    + getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator + folderName + "</b>  folder");
        }else {
            response.put("xmlFilePath", "");
        }

        logger.info("convertExcelToBpmn service end");
        return response;
    }

    /**
     * Use for processing excel data and add to process table dto
     *
     * @param serverFile
     * @return
     * @throws IOException
     * @throws SystemException
     * @throws ValidateException
     */
    public List<ProcessTableDto> getProcessTableData(File serverFile, String fileExt, String originalFileName, Map<String, Object> errorMessages)
            throws IOException, SystemException, ValidateException {
        ErrorsDto errors = new ErrorsDto();
        List<ProcessTableDto> processTableDtoList = new ArrayList<>();

        // Check if file size must be less then or equals to 500 kb
        double fileSize = serverFile.length() / 1024;
        if (fileSize <= Double.valueOf(getProperty(PropertiesHelper.UPLOAD_FILE_SIZE))) {
            FileInputStream inputStream = null;
            XSSFWorkbook workbook = null;
            HSSFWorkbook hssfworkbook = null;
            // get all step ids to compare with next step ids
            List<String> allowedSteps = new ArrayList<>();
            List<String> shapeTypeList = new ArrayList<>();
            int lastRow = 0;
            try {
                inputStream = new FileInputStream(serverFile);
                Iterator<Row> rowIterator = null;

                if (!"xls".equalsIgnoreCase(fileExt)) {
                    workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    rowIterator = sheet.iterator();

                    sheet.forEach(row -> {
                        // Add to allowedSteps list to compare with next step id
                        Cell cell = row.getCell(0);
                        String step =
                                !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                        : null;
                        if (!ValidationUtils.getInstance().isEmptyString(step)) {
                            allowedSteps.add(step.trim().toUpperCase());
                        }
                        // Add to shapeTypeList to check startEvent and endEvent
                        Cell shapeTypeCell = row.getCell(4);
                        String shapeType = !excelParserHelper.isCellBlank(shapeTypeCell)
                                ? excelParserHelper.getCellValue(shapeTypeCell).toString()
                                : null;
                        if (!ValidationUtils.getInstance().isEmptyString(shapeType)) {
                            shapeTypeList.add(shapeType.trim().toUpperCase());
                        }
                    });
                    lastRow = sheet.getLastRowNum();
                } else {
                    hssfworkbook = new HSSFWorkbook(inputStream);
                    HSSFSheet sheet = hssfworkbook.getSheetAt(0);
                    rowIterator = sheet.iterator();
                    sheet.forEach(row -> {
                        // Add to allowedSteps list to compare with next step id
                        Cell cell = row.getCell(0);
                        String step =
                                !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                        : null;
                        if (!ValidationUtils.getInstance().isEmptyString(step)) {
                            allowedSteps.add(step.trim().toUpperCase());
                        }
                        // Add to shapeTypeList to check startEvent and endEvent
                        Cell shapeTypeCell = row.getCell(4);
                        String shapeType = !excelParserHelper.isCellBlank(shapeTypeCell)
                                ? excelParserHelper.getCellValue(shapeTypeCell).toString()
                                : null;
                        if (!ValidationUtils.getInstance().isEmptyString(shapeType)) {
                            shapeTypeList.add(shapeType.trim().toUpperCase());
                        }
                    });
                    lastRow = sheet.getLastRowNum();
                }

                if (!shapeTypeList.contains(ShapeType.EVENT_START_NONE.getLabel().toUpperCase())) {
                    logger.error(messageSource.getMessage("error.required",
                            new Object[] {ShapeType.EVENT_START_NONE.getLabel()}, LocaleContextHolder.getLocale()));
                    errorMessages.put("", messageSource.getMessage("error.required",
                            new Object[] {ShapeType.EVENT_START_NONE.getLabel()}, LocaleContextHolder.getLocale()));
                } else if (!shapeTypeList.contains(ShapeType.EVENT_END_NONE.getLabel().toUpperCase())) {
                    logger.error(messageSource.getMessage("error.required",
                            new Object[] {ShapeType.EVENT_END_NONE.getLabel()}, LocaleContextHolder.getLocale()));
                    errorMessages.put("", messageSource.getMessage("error.required",
                            new Object[] {ShapeType.EVENT_END_NONE.getLabel()}, LocaleContextHolder.getLocale()));
                }

                String validateHeader = validateHeader(rowIterator.next());

                // Validate and skip header row
                if (!ValidationUtils.getInstance().isEmptyString(validateHeader)) {
                    logger.error(messageSource.getMessage("upload.invalid.header", new Object[] {validateHeader},
                            LocaleContextHolder.getLocale()));
                    errorMessages.put("", messageSource.getMessage("upload.invalid.header",
                            new Object[] {validateHeader}, LocaleContextHolder.getLocale()));
                }

                Map<String,String> laneIdMap = new LinkedHashMap<String,String>();
                List<String> errorAlreadyUsedLaneIdList = new LinkedList<String>();
                List<String> errorSameParticipantLaneIdList = new LinkedList<String>();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    // Check if blank row exists
                    if (excelParserHelper.isRowBlank(row)) {
                        continue;
                    }

                    // Use to add excel data to DTO
                    ProcessTableDto processTableDto = new ProcessTableDto();

                    Cell cell = row.getCell(0); // Step Id
                    String stepId =
                            !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                    : null;

                    if (ValidationUtils.getInstance().isEmptyString(stepId)) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.required", "StepId"));
                    } else if (!excelParserHelper.getCellValue(cell).toString().matches("[0-9]+")) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.invalid", "StepId"));
                    } else {
                        for (ProcessTableDto model : processTableDtoList) {
                            if (!ValidationUtils.getInstance().isEmptyString(model.getStepId())
                                    && stepId.toUpperCase().equalsIgnoreCase(model.getStepId())) {
                                errors.add(new ErrorDto("" + row.getRowNum(), "error.already.used", new Object[] {"Step Id", stepId}));
                            }

                        }
                        processTableDto.setStepId(stepId.toUpperCase());
                    }


                    cell = row.getCell(1); // Step Description
                    processTableDto.setStepDescription(
                            !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                    : "");

                    cell = row.getCell(2); // Next Step Id
                    String nextStepId =
                            !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                    : null;
                    String nStepId = "";
                    if (!ValidationUtils.getInstance().isEmptyString(nextStepId)) {
                        String[] steps = nextStepId.split(",");
                        String errorStep = "";
                        for (String step : steps) {
                            step = step.trim().toUpperCase();
                            nStepId =
                                    ValidationUtils.getInstance().isEmptyString(nStepId) ? step : nStepId + "," + step;
                            if (!allowedSteps.contains(step.toUpperCase())) {
                                errorStep = ValidationUtils.getInstance().isEmptyString(errorStep) ? step
                                        : errorStep + "," + step;
                            }
                        }
                        if (!ValidationUtils.getInstance().isEmptyString(errorStep)) {
                            errors.add(new ErrorDto("" + row.getRowNum(), "error.nextStepId", errorStep));
                        }
                    }
                    processTableDto.setNextStepId(nStepId);

                    cell = row.getCell(3); // Connector Label
                    processTableDto.setConnectorLabel(
                            !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                    : null);

                    boolean isNextStepValid = true;
                    int noOfStep = 0;
                    boolean isConnectorLabelValid = true;
                    int noOfLabel = 0;

                    if (!ValidationUtils.getInstance().isEmptyString(nextStepId)) {

                        if (nextStepId.contains(",")) {
                            if ((nextStepId.trim().charAt(0) == ',')
                                    || (nextStepId.trim().charAt(nextStepId.trim().length() - 1) == ',')) {
                                isNextStepValid = false;

                            }
                            for (String stepDescription : nextStepId.split(",")) {
                                if (!ValidationUtils.getInstance().isEmptyString(stepDescription)) {
                                    noOfStep += 1;
                                } else {
                                    isNextStepValid = false;
                                }
                            }

                        }
                    }
                    if (!ValidationUtils.getInstance().isEmptyString(processTableDto.getConnectorLabel())) {


                        if (processTableDto.getConnectorLabel().contains(",")) {
                            if (processTableDto.getConnectorLabel().trim()
                                    .lastIndexOf(",") == (processTableDto.getConnectorLabel().trim().length())) {
                                isNextStepValid = false;

                            }
                            for (String connectorLabel : processTableDto.getConnectorLabel().split(",")) {
                                if (!ValidationUtils.getInstance().isEmptyString(connectorLabel)) {
                                    noOfLabel += 1;
                                } else {
                                    isConnectorLabelValid = false;
                                }
                            }
                        }
                    }


                    if (!isNextStepValid) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.invalid", "Next Step Id"));
                    }
                    if (!isConnectorLabelValid) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.invalid", "Connector Label"));
                    } else if ((!ValidationUtils.getInstance().isEmptyString(processTableDto.getConnectorLabel())
                            && (noOfStep != noOfLabel))) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.invalid", "Connector Label"));
                    }


                    cell = row.getCell(4); // Shape Type
                    String shapetype =
                            !excelParserHelper.isCellBlank(cell) ? excelParserHelper.getCellValue(cell).toString()
                                    : null;
                    if ((ValidationUtils.getInstance().isEmptyString(shapetype))) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.required", "Shape Type"));
                    } else if (ShapeType.get(excelParserHelper.getCellValue(cell).toString()) == null) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.shape.type.invalid", shapetype));
                    }
                    // if (row.getRowNum() == 1) {
                    // if (!shapetype.equalsIgnoreCase(ShapeType.EVENT_START_NONE.getLabel())) {
                    // errors.add(new ErrorDto("" + row.getRowNum(), "error.startevent"));
                    // }
                    // } else if (lastRow == row.getRowNum()) {
                    // if (!shapetype.equalsIgnoreCase(ShapeType.EVENT_END_NONE.getLabel())) {
                    // errors.add(new ErrorDto("" + row.getRowNum(), "error.endevent"));
                    // }
                    // }
                    processTableDto.setShapeType(!excelParserHelper.isCellBlank(cell)
                            ? ShapeType.get(excelParserHelper.getCellValue(cell).toString())
                            : null);

                    if ((processTableDto.getShapeType() != null) && !processTableDto.getShapeType().isEnable()) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.shape.not.allowed",
                                processTableDto.getShapeType().getLabel()));
                    }

                    if (ValidationUtils.getInstance().isEmptyString(processTableDto.getStepDescription())) {
                        if (ShapeType.EVENT_START_NONE.equals(processTableDto.getShapeType())
                                || ShapeType.EVENT_END_NONE.equals(processTableDto.getShapeType())) {

                            processTableDto.setStepDescription(processTableDto.getShapeType().getLabel());
                        } else if (!ShapeType.GATEWAY_EXCLUSIVE.equals(processTableDto.getShapeType())) {
                            // ExclusiveGateway not required description
                            errors.add(new ErrorDto("" + row.getRowNum(), "error.required", "Description"));
                        }
                    }

                    if (!ShapeType.EVENT_END_NONE.equals(processTableDto.getShapeType())) {
                        if (ValidationUtils.getInstance().isEmptyString(processTableDto.getNextStepId())) {
                            errors.add(new ErrorDto("" + row.getRowNum(), "error.required", "Next Step Id"));
                        }
                    } else if (!ValidationUtils.getInstance().isEmptyString(processTableDto.getNextStepId())) {
                        errors.add(new ErrorDto("" + row.getRowNum(), "error.not.allowed",
                                new Object[] {"Next Step Id", ShapeType.EVENT_END_NONE.getLabel()}));
                    }

                    cell = row.getCell(5); // LaneId

                    String laneId = !excelParserHelper.isCellBlank(cell)
                            ? excelParserHelper.getCellValue(cell).toString().toUpperCase()
                            : "";

                    processTableDto.setLaneId(laneId);

                    cell = row.getCell(6); // Participant

                    String participant = "";
                    if(!ValidationUtils.getInstance().isEmptyString(laneId) && !excelParserHelper.isCellBlank(cell)) {
                    	participant = excelParserHelper.getCellValue(cell).toString();
                    }
                    processTableDto.setParticipant(participant);

                    boolean isLaneAndParticipantValid = true;
                    if (!ValidationUtils.getInstance().isEmptyString(laneId)) {

                        if(laneIdMap.containsKey(laneId.toUpperCase())) {
                            boolean isErrorAlreadyAdded = false;
                            for (String lane : errorSameParticipantLaneIdList) {
                                if (lane.equalsIgnoreCase(laneId)) {
                                    isErrorAlreadyAdded = true;
                                    break;
                                }
                            }
                            if (!isErrorAlreadyAdded) {
                                String participantText = laneIdMap.get(laneId.toUpperCase());
                                if (!participantText.equalsIgnoreCase(participant)) {
                                    errors.add(new ErrorDto("" + row.getRowNum(), "error.same.participant",
                                            new Object[] {laneId}));
                                    errorSameParticipantLaneIdList.add(laneId.toUpperCase());
                                    isLaneAndParticipantValid = false;
                                }
                            }

                        }

                        if (!ValidationUtils.getInstance().isEmptyString(participant)) {
                            for (String lane : laneIdMap.keySet()) {
                                if (!lane.equalsIgnoreCase(laneId)) {
                                    boolean isErrorAlreadyAdded = false;
                                    for (String laneIDFromList : errorAlreadyUsedLaneIdList) {
                                        if (laneIDFromList.equalsIgnoreCase(laneId)) {
                                            isErrorAlreadyAdded = true;
                                            break;
                                        }
                                    }

                                    if (!isErrorAlreadyAdded) {
                                        String participantString = laneIdMap.get(lane);
                                        if (!ValidationUtils.getInstance().isEmptyString(participantString)
                                                && participantString.equalsIgnoreCase(participant)) {
                                            errors.add(new ErrorDto("" + row.getRowNum(),
                                                    "error.participant.already.used", new Object[] {lane}));
                                            isLaneAndParticipantValid = false;
                                            break;
                                        }
                                    }

                                }

                            }
                        }

                        if (isLaneAndParticipantValid) {
                            boolean isParticipantExist = false;
                            for (String lane : laneIdMap.keySet()) {
                                String participantString = laneIdMap.get(lane);
                                if (participant.equalsIgnoreCase(participantString)) {
                                    isParticipantExist = true;
                                    break;
                                }
                            }
                            if (!isParticipantExist) {
                                laneIdMap.put(laneId.toUpperCase(), participant);
                            }

                        }
                    }

                    cell = row.getCell(7); // TextAnnotation

                    String textAnnotation = !excelParserHelper.isCellBlank(cell)
                            ? excelParserHelper.getCellValue(cell).toString().toUpperCase()
                            : null;
                    processTableDto.setTextAnnotation(textAnnotation);

                    processTableDtoList.add(processTableDto);
                }

                // Check if errors found throw validationException
                if (errors.hasErrors()) {
                    logger.error(errors);
                    Map<String, Object> errorMessage = generateErrorData(errors, errorMessages);

                } else {
                    Collections.sort(processTableDtoList);
                }

            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
                if (hssfworkbook != null) {
                    hssfworkbook.close();
                }
            }
        } else

        {
            logger.error(messageSource.getMessage("upload.filesize.message",
                    new Object[] {getProperty(PropertiesHelper.UPLOAD_FILE_SIZE)}, LocaleContextHolder.getLocale()));
            errorMessages.put("", messageSource.getMessage("upload.filesize.message",
                    new Object[] {getProperty(PropertiesHelper.UPLOAD_FILE_SIZE)}, LocaleContextHolder.getLocale()));
        }

        if(errorMessages != null && !errorMessages.isEmpty()) {
            errorData.put(originalFileName, errorMessages);
        }

//        Collections.sort(processTableDtoList);

        return processTableDtoList;
    }

    private Map<String, Object> generateErrorData(ErrorsDto errors,Map<String, Object> errorMessage) {
        Map<String, Object> errorMessages = errorMessage;
        for (ErrorDto errorDto : errors.getErrors()) {
            String errorKey = errorDto.getErrorKey();
            String errorField = errorDto.getErrorField();
            Object[] errorArg = errorDto.getErrorArgs();
            if ((errorKey != null) && (errorArg != null) && (errorField != null)) {
                String errorMsg = (String) errorMessages.get(errorField);
                if (errorMsg != null) {
                    errorMessages.put(errorField, errorMsg + ", " + messageSource.getMessage(errorKey, errorArg, LocaleContextHolder.getLocale()));
                } else {
                    errorMessages.put(errorField, messageSource.getMessage(errorKey, errorArg,  LocaleContextHolder.getLocale()));
                }

            } else if ((errorKey != null) && (errorArg != null)) {
                errorMessages.put("errorField", messageSource.getMessage(errorKey, errorArg, LocaleContextHolder.getLocale()));
            }
        }
        return errorMessages;
    }

    /**
     * Validate file extension
     *
     * @param filename
     * @return
     */
    private boolean validExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")
                || extension.equalsIgnoreCase("csv")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Generate file name
     *
     * @param filename
     * @return
     */
    private String generateFileName(String filename) {
        String fileName = filename.substring(0, filename.lastIndexOf('.'));
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        if ("csv".equalsIgnoreCase(extension)) {
            extension = "xlsx";
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        String newFileName = fileName + "_" + instant.getEpochSecond() + "." + extension;
        return newFileName;
    }

    /**
     * Used for validate Row of file.
     *
     * @param headerRow
     * @return
     */
    private String validateHeader(Row headerRow) {
        String[] headerElements = getProperty(PropertiesHelper.EXCEL_DEFAULT_HEADERS).split(",");
        List<String> defaultHeader = Arrays.asList(headerElements);
        String validationHeader = "";
        List<String> headerList = excelParserHelper.getHeaderNames(headerRow);
        if (defaultHeader.size() == headerList.size()) {
            for (int header = 0; header < headerList.size(); header++) {
                if (!defaultHeader.get(header).toString().equalsIgnoreCase(headerList.get(header).toString())) {
                    validationHeader = validationHeader
                            + (ValidationUtils.getInstance().isEmptyString(validationHeader) ? headerList.get(header)
                                    : ", " + headerList.get(header));
                }
            }
        } else {
            validationHeader = "count";
        }
        return validationHeader;
    }

    /**
     *
     * @param file
     * @return
     */
    private String fileExtention(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
    }

    private long getLocaleTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.getEpochSecond();
    }

    public Map<String, Object> convertExcelToBpmn(String path, boolean isOverride) throws IOException, SystemException {

        logger.info("convertExcelToBpmn service start");

        errorData = new LinkedHashMap<>(2);
        response = new LinkedHashMap<>(2);
        String xmlFilePath = null;

        // upload directory validation
        File excelDirectory = new File(getProperty(PropertiesHelper.UPLOAD_PATH));
        if (!excelDirectory.exists()) {
            logger.error("\n\n"+messageSource.getMessage("upload.path.error", null, LocaleContextHolder.getLocale())+"\n\n");
            return response;
        }
        File xmlDirectory = new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH));
        if (!xmlDirectory.exists()) {
            logger.error("\n\n" + messageSource.getMessage("bpmn.xml.path.error", null, LocaleContextHolder.getLocale())
                    + "\n\n");
            return response;
        }

        // Validation to check file extension
        String folderName = "" + getLocaleTime();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles.length > Integer.parseInt(getProperty(PropertiesHelper.UPLOAD_FILE_COUNT_COMMAND))) {
            logger.error("\n\n" + messageSource.getMessage("upload.file.count.message",
                    new Object[] {getProperty(PropertiesHelper.UPLOAD_FILE_COUNT_COMMAND)},
                    LocaleContextHolder.getLocale()) + "\n\n");
            return response;
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Map<String, Object> errorMessages = new LinkedHashMap<>(2);
                if (validExtension(file.getName())) {
                    byte[] bytes = Files.readAllBytes(file.toPath());

                    File uploadExcelDirectory = new File(excelDirectory.getAbsolutePath() + File.separator
                            + "uploaded_excel"
                            + File.separator + folderName);
                    if (!uploadExcelDirectory.exists()) {
                        uploadExcelDirectory.mkdirs();
                    }

                    String filePath = uploadExcelDirectory + File.separator + generateFileName(file.getName());
                    File serverFile = new File(filePath);

                    String fileExt = FilenameUtils.getExtension(file.getName());
                    // Check if file is csv or xls based on that create file on server
                    if ("csv".equalsIgnoreCase(fileExt)) {
                        Workbook wb = new XSSFWorkbook();
                        CreationHelper helper = wb.getCreationHelper();
                        Sheet sheet = wb.createSheet("new sheet");

                        Reader fileReader =
                                new InputStreamReader(new BOMInputStream(new FileInputStream(file)), "UTF-8");
                        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
                        int csvRow = 0;
                        for (CSVRecord csvRecord : csvParser) {
                            Iterator<String> rowIterator = csvRecord.iterator();
                            Row row = sheet.createRow((short) csvRow++);
                            int index = 0;
                            while (rowIterator.hasNext()) {
                                String rowVal = rowIterator.next();
                                row.createCell(index).setCellValue(rowVal);
                                index++;
                            }
                        }

                        // Write the csv output to xlsx file on server
                        BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(serverFile));
                        wb.write(fileOut);
                        fileOut.close();


                    } else {
                        // Create the excel file on server
                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                        stream.write(bytes);
                        stream.close();
                    }
                    String fileOriginalName = file.getName();
                    String fileName = (fileOriginalName.substring(0, fileOriginalName.lastIndexOf(".")));

                    File existingFile = new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator
                            + folderName + File.separator + fileName + ".bpmn");

                    if (existingFile.exists() && !isOverride) {
                        fileName = fileName + "_" + getLocaleTime();
                    }

                    // Get process table list dto from server excel file
                    List<ProcessTableDto> processTableDtoList =
                            getProcessTableData(serverFile, fileExt, file.getName(), errorMessages);


                    if (errorData != null && !errorData.containsKey(file.getName())) {
                        File bpmnDirectory =
                                new File(getProperty(PropertiesHelper.BPMN_UPLOAD_PATH) + File.separator + folderName);
                        if (!bpmnDirectory.exists()) {
                            bpmnDirectory.mkdirs();
                        }
                        xmlFilePath = bpmnHelper.generateXMLFileWithLane(processTableDtoList, fileName, folderName);
                    }

                } else {
                    logger.error(
                            messageSource.getMessage("upload.invalid.format", null, LocaleContextHolder.getLocale()));
                    errorMessages.put("",
                            messageSource.getMessage("upload.invalid.format", null, LocaleContextHolder.getLocale()));
                    errorData.put(file.getName(), errorMessages);
                }
            }
        }

        if (errorData != null && !errorData.isEmpty()) {
            response.put("errors", errorData);
        }
        // else {
        // response.put("errors", "");
        // }
        if (xmlFilePath != null) {
            response.put("xmlFilePath", "Generated files are store in " + getProperty(PropertiesHelper.BPMN_UPLOAD_PATH)
                    + File.separator + folderName + " folder");
        }
        // else {
        // response.put("xmlFilePath", "");
        // }

        logger.info("convertExcelToBpmn service end");

        List<String> headersList = new ArrayList<>();
        headersList.add("File");
        headersList.add("Row");
        headersList.add("Error Message");

        logger.info(generateTable(headersList, response));

        return response;
    }

    public String generateTable(List<String> headersList, Map<String, Object> response, int... overRiddenHeaderHeight) {
        StringBuilder stringBuilder = new StringBuilder();

        int rowHeight = overRiddenHeaderHeight.length > 0 ? overRiddenHeaderHeight[0] : 1;

        if (response.containsKey("xmlFilePath")) {
            stringBuilder.append(NEW_LINE);
            stringBuilder.append(NEW_LINE);

            stringBuilder.append(response.get("xmlFilePath"));
        }

        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);

        if (response.containsKey("errors")) {

        Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidhtofTable(headersList, response);

        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);


        for (int headerIndex = 0; headerIndex < headersList.size(); headerIndex++) {
            fillCell(stringBuilder, headersList.get(headerIndex), headerIndex, columnMaxWidthMapping);
        }

        stringBuilder.append(NEW_LINE);

        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);


        Map<String, Object> errors = (Map<String, Object>) response.get("errors");
        Iterator errorsIterator = errors.entrySet().iterator();

        while (errorsIterator.hasNext()) {
            Map.Entry mapElements = (Map.Entry) errorsIterator.next();
            Map<String, Object> error = (Map<String, Object>) mapElements.getValue();
            Iterator errorIterator = error.entrySet().iterator();
            while (errorIterator.hasNext()) {
                for (int i = 0; i < rowHeight; i++) {
                    stringBuilder.append(NEW_LINE);
                }
                Map.Entry mapElement = (Map.Entry) errorIterator.next();
                fillCell(stringBuilder, mapElements.getKey().toString(), 0, columnMaxWidthMapping);
                fillCell(stringBuilder, mapElement.getKey().toString(), 1, columnMaxWidthMapping);
                fillCell(stringBuilder, mapElement.getValue().toString(), 2, columnMaxWidthMapping);
            }
        }

        stringBuilder.append(NEW_LINE);
        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);

        }

        return stringBuilder.toString();
    }

    private void fillSpace(StringBuilder stringBuilder, int length) {
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" ");
        }
    }

    private void createRowLine(StringBuilder stringBuilder, int headersListSize,
            Map<Integer, Integer> columnMaxWidthMapping) {
        for (int i = 0; i < headersListSize; i++) {
            if (i == 0) {
                stringBuilder.append(TABLE_JOINT_SYMBOL);
            }

            for (int j = 0; j < columnMaxWidthMapping.get(i) + PADDING_SIZE * 2; j++) {
                stringBuilder.append(TABLE_H_SPLIT_SYMBOL);
            }
            stringBuilder.append(" " + TABLE_JOINT_SYMBOL);
        }
    }


    private Map<Integer, Integer> getMaximumWidhtofTable(List<String> headersList, Map<String, Object> response) {
        Map<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            columnMaxWidthMapping.put(columnIndex, 0);
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {

            if (headersList.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
                columnMaxWidthMapping.put(columnIndex, headersList.get(columnIndex).length());
            }
        }

        Map<String, Object> errors = (Map<String, Object>) response.get("errors");
        Iterator errorsIterator = errors.entrySet().iterator();

        while (errorsIterator.hasNext()) {
            Map.Entry mapElements = (Map.Entry) errorsIterator.next();
            Map<String, Object> error = (Map<String, Object>) mapElements.getValue();
            Iterator errorIterator = error.entrySet().iterator();
            while (errorIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) errorIterator.next();

                if (mapElements.getKey().toString().length() > columnMaxWidthMapping.get(0)) {
                    columnMaxWidthMapping.put(0, mapElements.getKey().toString().length());
                }
                if (mapElement.getKey().toString().length() > columnMaxWidthMapping.get(1)) {
                    columnMaxWidthMapping.put(1, mapElement.getKey().toString().length());
                }
                if (mapElement.getValue().toString().length() > columnMaxWidthMapping.get(2)) {
                    columnMaxWidthMapping.put(2, mapElement.getValue().toString().length());
                }
            }
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {

            if (columnMaxWidthMapping.get(columnIndex) % 2 != 0) {
                columnMaxWidthMapping.put(columnIndex, columnMaxWidthMapping.get(columnIndex) + 1);
            }
        }


        return columnMaxWidthMapping;
    }

    private int getOptimumCellPadding(int cellIndex, int datalength, Map<Integer, Integer> columnMaxWidthMapping,
            int cellPaddingSize) {
        if (datalength % 2 != 0) {
            datalength++;
        }

        if (datalength < columnMaxWidthMapping.get(cellIndex)) {
            cellPaddingSize = cellPaddingSize + (columnMaxWidthMapping.get(cellIndex) - datalength) / 2;
        }

        return cellPaddingSize;
    }

    private void fillCell(StringBuilder stringBuilder, String cell, int cellIndex,
            Map<Integer, Integer> columnMaxWidthMapping) {

        int cellPaddingSize = getOptimumCellPadding(cellIndex, cell.length(), columnMaxWidthMapping, PADDING_SIZE);

        if (cellIndex == 0) {
            stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
        }

        // fillSpace(stringBuilder, cellPaddingSize);
        stringBuilder.append(" ");
        stringBuilder.append(cell);
        if (cell.length() % 2 != 0) {
            stringBuilder.append(" ");
        }

        fillSpace(stringBuilder, cellPaddingSize * 2);

        stringBuilder.append(TABLE_V_SPLIT_SYMBOL);

    }
}
