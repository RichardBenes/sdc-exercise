package com.rbenes;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

public class PrimesCheckerTest {

    static final String TEST_WORKBOOK_PATH = "src\\test\\resources\\vzorek_dat - kopie.xlsx";

    @Disabled("Just for development")
    @Test
    public void findCurrentPath() throws FileNotFoundException, IOException {
        String currentPath = new File(".").getCanonicalPath();
        System.out.printf("Current path is: %s\n", currentPath);        
    }

    @Test
    public void readCellFromXlsxFile() throws FileNotFoundException, IOException, InvalidFormatException {

        try (Workbook w = new XSSFWorkbook(new File(TEST_WORKBOOK_PATH))) {

            Sheet sheet = w.getSheetAt(0);

            // Counting is zero based in Java, but 1-based in Excel...
            Row row0 = sheet.getRow(1);
            Cell cellB2 = row0.getCell(1);

            assertThat(cellB2.getCellType()).isEqualTo(CellType.STRING);
            assertThat(cellB2.getStringCellValue()).isEqualTo("5645641");
        }
    }
}
