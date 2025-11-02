package com.rbenes;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import lombok.extern.log4j.Log4j2;

import static org.assertj.core.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

@Disabled
@Log4j2
public class DevTest {

    static final String TEST_WORKBOOK_PATH = "src\\test\\resources\\";

    @Disabled("Just for development")
    @Test
    public void findCurrentPath() throws FileNotFoundException, IOException {
        String currentPath = new File(".").getCanonicalPath();
        log.info("Current path is: {}", currentPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {"vzorek_dat - kopie.xlsx", "formatted-cell.xlsx"})
    public void readCellFromXlsxFile(String filename) throws FileNotFoundException, IOException, InvalidFormatException {

        try (Workbook w = new XSSFWorkbook(new File(TEST_WORKBOOK_PATH + filename))) {

            Sheet sheet = w.getSheetAt(0);

            // Counting is zero based in Java, but 1-based in Excel...
            Row row0 = sheet.getRow(1);
            Cell cellB2 = row0.getCell(1);

            assertThat(cellB2.getCellType()).isEqualTo(CellType.STRING);
            assertThat(cellB2.getStringCellValue()).isEqualTo("5645641");
        }
    }
}
