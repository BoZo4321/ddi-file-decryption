package com.bozidar.server.decryption;

import com.bozidar.server.exception.ServerException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class XlsxFileProcessorTest {

    private final XlsxFileProcessor processor = new XlsxFileProcessor();

    @Test
    void decrypt_plainTextStringCell_throwsServerException() throws Exception {
        assertThrows(ServerException.class, () -> processor.decrypt(buildXlsx("Sender"), "BozidarSecKey16!"));
    }

    @Test
    void decrypt_numericCellOnly_doesNotThrow() throws Exception {
        assertDoesNotThrow(() -> processor.decrypt(buildXlsxNumeric(42.0), "BozidarSecKey16!"));
    }

    private byte[] buildXlsx(String value) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(value);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildXlsxNumeric(double value) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(value);
            wb.write(out);
            return out.toByteArray();
        }
    }
}
