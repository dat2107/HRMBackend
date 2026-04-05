package com.toto.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toto.backend.service.ExcelParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class ExcelParserServiceImpl implements ExcelParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String parseToJson(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = filename.endsWith(".xls")
                    ? new HSSFWorkbook(is)
                    : new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return null;

            List<String> headers = new ArrayList<>();
            List<Map<String, String>> rows = new ArrayList<>();

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();

            if (lastRow < firstRow) return null;

            Row headerRow = sheet.getRow(firstRow);
            if (headerRow == null) return null;

            for (int col = headerRow.getFirstCellNum(); col < headerRow.getLastCellNum(); col++) {
                Cell cell = headerRow.getCell(col);
                headers.add(cell != null ? getCellString(cell) : "Col" + col);
            }

            for (int r = firstRow + 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean hasData = false;
                Map<String, String> rowMap = new LinkedHashMap<>();

                for (int col = 0; col < headers.size(); col++) {
                    Cell cell = row.getCell(col);
                    String val = cell != null ? getCellString(cell) : "";
                    rowMap.put(headers.get(col), val);
                    if (!val.isBlank()) hasData = true;
                }

                if (hasData) rows.add(rowMap);
            }

            workbook.close();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("headers", headers);
            result.put("rows", rows);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("[EXCEL] Parse failed: {}", e.getMessage());
            return null;
        }
    }

    private String getCellString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue(); }
            }
            default -> "";
        };
    }
}
