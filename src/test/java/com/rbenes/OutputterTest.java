package com.rbenes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OutputterTest {

    Outputter testedOutputter;
    ArrayBlockingQueue<EnhancedCellI> testPC2OutQ;

    static final String TEST_APPENDER_NAME = "addedTestAppender";

    // This is all necessary to capture Outputter's output...
    Logger outputtersLogger;
    LoggerContext outputtersLoggerContext;
    Configuration outputtersConfiguration;
    ByteArrayOutputStream outputtersOutput;
    OutputStreamAppender osa;

    @BeforeEach
    void beforeEach() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        testPC2OutQ = new ArrayBlockingQueue<>(5);
        testedOutputter = new Outputter(testPC2OutQ);

        // Some reflection magic...
        Field logField = testedOutputter.getClass().getDeclaredField("log");
        logField.setAccessible(true);

        outputtersLogger = (Logger) logField.get(testedOutputter);
        outputtersLoggerContext = outputtersLogger.getContext();
        outputtersConfiguration = outputtersLoggerContext.getConfiguration();

        outputtersOutput = new ByteArrayOutputStream();

        osa = OutputStreamAppender.newBuilder()
            .setName(TEST_APPENDER_NAME)
            .setTarget(outputtersOutput)
            .build();
        
        outputtersConfiguration.addLoggerAppender(outputtersLogger, osa);

        osa.start();
    }

    @AfterEach
    void afterEach() {

        outputtersConfiguration.getAppenders().remove(TEST_APPENDER_NAME);
        outputtersLoggerContext.reconfigure();
    }

    
    @Test
    void singleThreaded_success() {

        // Arrange

        EnhancedCell e1 = mock(EnhancedCell.class);
        when(e1.getVisualRowIndex()).thenReturn(1);
        when(e1.getNumericValue()).thenReturn(13L);
        when(e1.isPrime()).thenReturn(true);
        // This is quite cumbersome, but we need comparing
        // functionality to be working... and using full EnhancedCell
        // would be comparable cumbersome..
        when(e1.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e1.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCellI.class).getVisualRowIndex()));

        EnhancedCell e2 = mock(EnhancedCell.class);
        when(e2.getVisualRowIndex()).thenReturn(2);
        when(e2.getNumericValue()).thenReturn(23L);
        when(e2.isPrime()).thenReturn(true);
        when(e2.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e2.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCellI.class).getVisualRowIndex()));

        EnhancedCell e3 = mock(EnhancedCell.class);
        when(e3.getVisualRowIndex()).thenReturn(3);
        when(e3.getNumericValue()).thenReturn(31L);
        when(e3.isPrime()).thenReturn(true);
        when(e3.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e3.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCellI.class).getVisualRowIndex()));

        EndOfProcessingCell eEndOfProc = new EndOfProcessingCell();

        // Adding the cells _not_ sorted by visual row index
        testPC2OutQ.add(e1);
        testPC2OutQ.add(e3);
        testPC2OutQ.add(e2);
        // But the EOP is always the last item added to Outputter's
        // input queue.
        testPC2OutQ.add(eEndOfProc);

        // Act

        testedOutputter.run();

        // Assert

        // Even the end of processing is removed and moved to an internal queue
        assertThat(testPC2OutQ.size()).isEqualTo(0);
        
        osa.stop();
        var output = outputtersOutput.toString();
        
        assertThat(output).isEqualToIgnoringNewLines(
            """
            13
            23
            31
            """);
    }
}
