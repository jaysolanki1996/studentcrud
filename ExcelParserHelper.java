package com.bpmnxmlgenerator.infra.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

/**
 * Helper class for excel
 *
 */
@Component
public class ExcelParserHelper {

    DataFormatter dataFormatter = new DataFormatter();

    public boolean isCellBlank(Cell cell) {
        return ((cell == null) || (getCellValue(cell) == null) || (getCellValue(cell).equals("")));
    }

    /**
     * Get cell value based on data type
     *
     * @param cell
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType cellTypeEnum = cell.getCellTypeEnum();
        Object cellValue = null;
        switch (cellTypeEnum) {
            case STRING:
                cellValue = cell.getRichStringCellValue().getString().trim();
                break;
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                // Data Formatter will identify the column value (Date/Numeric) and returns the
                // string value
                cellValue = dataFormatter.formatCellValue(cell);
                break;
            case BLANK:
                cellValue = null;
                break;
            case FORMULA:
                switch (cell.getCachedFormulaResultTypeEnum()) {
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            cellValue = cell.getDateCellValue();
                        } else {
                            cellValue = cell.getNumericCellValue();
                        }
                        break;
                    case STRING:
                        cellValue = cell.getStringCellValue().trim();
                        break;
                }
                break;
        }

        return cellValue;
    }

    /**
     * Check if row is blank
     *
     * @param r
     * @return
     */
    public boolean isRowBlank(Row rowVal) {
        boolean retVal = true;

        if (rowVal != null) {
            Iterator<Cell> cellIter = rowVal.cellIterator();
            while (cellIter.hasNext()) {
                if (!isCellBlank(cellIter.next())) {
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    /**
     * Get header names from header row
     *
     * @param headerRow
     * @return
     */
    public List<String> getHeaderNames(Row headerRow) {
        List<String> headerColumnNames = new ArrayList<>();

        short minColIx = headerRow.getFirstCellNum();
        short maxColIx = headerRow.getLastCellNum();
        for (short colIx = minColIx; colIx < maxColIx; colIx++) {
            Cell cell = headerRow.getCell(colIx);
            if (cell == null) {
                continue;
            } else {
                if ((cell.getStringCellValue().trim() != null) && !cell.getStringCellValue().trim().equals("")) {
                    headerColumnNames.add(cell.getStringCellValue().trim());
                }
            }
        }
        return headerColumnNames;
    }

}
