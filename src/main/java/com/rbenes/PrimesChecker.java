package com.rbenes;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PrimesChecker {

    static final String FILENAME = "vzorek_dat - kopie.xlsx";

    // TODO: handle exceptions more gracefully
    // TODO: use logger instead of stdout
    // TODO: handle the file being used by some other process
    public static void main( String[] args ) throws InvalidFormatException, IOException {
        
        try (Workbook w = new XSSFWorkbook(new File(FILENAME))) {

            Sheet sheet = w.getSheetAt(0);

            // Counting is zero based in Java, but 1-based in Excel...
            Row row0 = sheet.getRow(1);
            Cell cellB2 = row0.getCell(1);

            System.out.println(cellB2.getStringCellValue());
        }        
    }
}
