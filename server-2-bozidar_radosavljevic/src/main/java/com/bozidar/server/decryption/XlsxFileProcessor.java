package com.bozidar.server.decryption;

import com.bozidar.encryption.suite.Encryptor;
import com.bozidar.server.exception.ServerException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class XlsxFileProcessor implements FileProcessor {

    private static final Set<String> EXTENSIONS = Collections.singleton("xlsx");

    @Override
    public Set<String> supportedExtensions() {
        return EXTENSIONS;
    }

    @Override
    public byte[] decrypt(byte[] encryptedContent, String key) throws ServerException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(encryptedContent);
             XSSFWorkbook workbook = new XSSFWorkbook(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        decryptCellIfNeeded(cell, key);
                    }
                }
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new ServerException("Failed to read/write xlsx workbook", ex);
        }
    }

    private void decryptCellIfNeeded(Cell cell, String key) throws ServerException {
        if (cell.getCellType() != CellType.STRING) {
            return;
        }
        String cipherText = cell.getStringCellValue();
        if (cipherText == null || cipherText.isEmpty()) {
            return;
        }

        String plainText;
        try {
            plainText = Encryptor.decrypt(key, cipherText);
        } catch (RuntimeException ex) {
            throw new ServerException(
                    "Decryption error for cell at " + cell.getAddress() + ": " + ex.getMessage(), ex);
        }
        if (plainText == null) {
            throw new ServerException(
                    "Encryptor returned null for cell '" + cipherText + "' at "
                            + cell.getAddress() + " - wrong key or corrupted content");
        }
        try {
            cell.setCellValue(Double.parseDouble(plainText));
        } catch (NumberFormatException ex) {
            cell.setCellValue(plainText);
        }
    }
}
