package com.TWReceipt.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class ExcelUtil {
    private static final ExcelUtil INSTANCE = new ExcelUtil();

    private TypeUtil typeUtil = TypeUtil.getInstance();

    private ExcelUtil() {}

    public static ExcelUtil getInstance() {
        return INSTANCE;
    }

    public List<Map<String, String>> readExcel(String excelFilePath, String excelFile, int sheetNum, int headerRow) {
        return readExcel(new File(excelFilePath + "\\" + excelFile), sheetNum, headerRow);
    }

    // K:V -> Field Name:Corresponding Value
    public List<Map<String, String>> readExcel(File excelFile, int sheetNum, int headerRow) {
        List<String> headerRowNames = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<>();

        // In Excel, the row starts on 1 but we follow the standard 0-indexing. Thus we have to -1
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(sheetNum);
            for (Cell cell : sheet.getRow(headerRow - 1)) {
                if (CellType.STRING == cell.getCellType()) {
                    headerRowNames.add(cell.getStringCellValue());
                }
            }

            int oneBelowHeaderRow = headerRow;
            for (int i = oneBelowHeaderRow; i <= sheet.getLastRowNum() - 1; ++i) {
                Map<String, String> dataMap = new HashMap<>();
                for (int j = 0; j < headerRowNames.size(); ++j) {
                    Cell cell = sheet.getRow(i).getCell(j);
                    if (cell == null) {
                        dataMap.put(headerRowNames.get(j), "");
                        continue;
                    }
                    if (CellType.STRING == cell.getCellType()) {
                        dataMap.put(headerRowNames.get(j), cell.getStringCellValue());
                    } else if (CellType.NUMERIC == cell.getCellType()) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            dataMap.put(headerRowNames.get(j), cell.getLocalDateTimeCellValue().toLocalDate().toString());
                        } else {
                            dataMap.put(headerRowNames.get(j), new BigDecimal(cell.getNumericCellValue()).toPlainString());
                        }
                    } else if (CellType.BLANK == cell.getCellType()) {
                        // Add as empty String
                        dataMap.put(headerRowNames.get(j), "");
                    }
                }
                data.add(dataMap);
            }

            return data;


        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }



    public String printAsJson(List<Map<String, String>> jsonData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type mapType = typeUtil.getMapType(jsonData);
        return gson.toJson(jsonData, mapType);
    }

}
