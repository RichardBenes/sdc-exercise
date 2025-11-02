package com.rbenes;

import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;

public class SdcExerciseTest {

    static final String TEST_WORKBOOK_PATH 
        = "src\\test\\resources\\vzorek_dat - kopie.xlsx";

    @Test
    void basicPositiveTest() throws InvalidFormatException, IOException {

        String[] args = new String[] {".", TEST_WORKBOOK_PATH};

        SdcExercise.main(args);
    }

}
