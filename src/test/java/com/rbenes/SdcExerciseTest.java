package com.rbenes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SdcExerciseTest {

    static final String TEST_WORKBOOK_PATH 
        = "src\\test\\resources\\vzorek_dat - kopie.xlsx";
    
    static final String TEST_APPENDER_NAME = "addedTestAppender";

    SdcExercise sdcExercise;

    Logger sdcExercisesLogger;
    Logger sdcExercisesRootLogger;
    LoggerContext sdcExercisesLoggerContext;
    Configuration sdcExercisesConfig;
    ByteArrayOutputStream sdcExercisesByteOutput;
    OutputStreamAppender osa;

    @BeforeEach
    void beforeEach() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        sdcExercise = new SdcExercise();

        Field logField = sdcExercise.getClass().getDeclaredField("log");
        logField.setAccessible(true);

        sdcExercisesLogger = ((Logger) logField.get(null));
        sdcExercisesLoggerContext = sdcExercisesLogger.getContext();
        sdcExercisesConfig = sdcExercisesLoggerContext.getConfiguration();
        sdcExercisesRootLogger = sdcExercisesLoggerContext.getRootLogger();

        sdcExercisesByteOutput = new ByteArrayOutputStream();

        osa = OutputStreamAppender.newBuilder()
            .setName(TEST_APPENDER_NAME)
            .setTarget(sdcExercisesByteOutput)
            .build();
        
        sdcExercisesConfig.addLoggerAppender(sdcExercisesRootLogger, osa);

        osa.start();
    }

    @AfterEach
    void afterEach() {

        sdcExercisesConfig.getAppenders().remove(TEST_APPENDER_NAME);
        sdcExercisesLoggerContext.reconfigure();
    }

    @Test
    void basicPositiveTest() throws InvalidFormatException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        String[] args = new String[] {".", TEST_WORKBOOK_PATH};

        SdcExercise.main(args);

        // Assert
        osa.stop();

        var strLoggedOutput = sdcExercisesByteOutput.toString();

        assertThat(strLoggedOutput).isEqualToIgnoringNewLines("""
            5645657
            15619
            1234187
            211
            7
            9788677
            23311
            54881
            2147483647""");
    }

}
