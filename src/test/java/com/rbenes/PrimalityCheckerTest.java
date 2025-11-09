package com.rbenes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.jupiter.api.Test;

public class PrimalityCheckerTest {

    @Test
    void test_singleThreaded() throws InterruptedException {

        // Arrange

        ArrayBlockingQueue<EnhancedCellI> testMain2PCQ = 
            new ArrayBlockingQueue<>(5);
        
        ArrayBlockingQueue<EnhancedCellI> outputAbq =
            new ArrayBlockingQueue<>(5);
        
        AKS aks = mock(AKS.class);

        PrimalityChecker pChecker = new PrimalityChecker(testMain2PCQ, outputAbq, aks);

        EnhancedCell e1 = mock(EnhancedCell.class);
        when(e1.getVisualRowIndex()).thenReturn(1);
        when(e1.getNumericValue()).thenReturn(7L);

        EnhancedCell e2 = mock(EnhancedCell.class);
        when(e2.getVisualRowIndex()).thenReturn(2);
        when(e2.getNumericValue()).thenReturn(8L);

        EnhancedCell e3 = mock(EnhancedCell.class);
        when(e3.getVisualRowIndex()).thenReturn(3);
        when(e3.getNumericValue()).thenReturn(11L);

        EnhancedCellI eEndOfProc = new EndOfProcessingCell();

        testMain2PCQ.addAll(List.of(e1, e2, e3, eEndOfProc));

        // Act
        pChecker.run();

        // Assert

        // The end of processing cell should remain in the queue
        assertThat(testMain2PCQ.size()).isEqualTo(1);
        assertThat(testMain2PCQ.take()).isInstanceOf(EndOfProcessingCell.class);

        // And there should be some results in the output queue...
        assertThat(outputAbq.size()).isEqualTo(3);

        var outputElements = new ArrayList<EnhancedCellI>();
        outputAbq.drainTo(outputElements);

        assertThat(outputElements)
            .extracting(ec -> ec instanceof EnhancedCell)
            .allMatch(b -> b.equals(false));

        assertThat(outputElements)
            .extracting(ec -> ec.getVisualRowIndex())
            .containsExactly(1, 2, 3);
        
        assertThat(outputElements)
            .filteredOn(ec -> ec instanceof EnhancedCell)
            .extracting(ec -> ((EnhancedCell) ec ).getNumericValue())
            .containsExactly(7L, 8L, 11L);    
    }
    
}
