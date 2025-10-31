package com.rbenes;

import java.io.IOException;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

public class PrimesCheckerTest {

    static final String TEST_WORKBOOK_PATH 
        = "src\\test\\resources\\vzorek_dat - kopie.xlsx";

    @Test
    void basicPositiveTest() throws InvalidFormatException, IOException {

        var l = LoggerContext.getContext().getLogger("com.rbenes.PrimesChecker");
        System.out.println("Logger obtained from test is:" + l);

        String[] args = new String[] {".", TEST_WORKBOOK_PATH};

        PrimesChecker.main(args);
    }

}
