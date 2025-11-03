package com.rbenes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.jupiter.api.Test;

public class OutputterTest {
    
    @Test
    void singleThreaded_success() {

        // Arrange

        var inputQueue = new ArrayBlockingQueue<EnhancedCell>(5);

        Outputter outputter = new Outputter(inputQueue);

        EnhancedCell e1 = mock(EnhancedCell.class);
        when(e1.isEndOfProcessingCell()).thenReturn(false);
        when(e1.getVisualRowIndex()).thenReturn(1);
        when(e1.getNumericValue()).thenReturn(13L);
        when(e1.isPrime()).thenReturn(true);
        // This is quite cumbersome, but we need comparing
        // functionality to be working... and using full EnhancedCell
        // would be comparable cumbersome..
        when(e1.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e1.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCell.class).getVisualRowIndex()));

        EnhancedCell e2 = mock(EnhancedCell.class);
        when(e2.isEndOfProcessingCell()).thenReturn(false);
        when(e2.getVisualRowIndex()).thenReturn(2);
        when(e2.getNumericValue()).thenReturn(23L);
        when(e2.isPrime()).thenReturn(true);
        when(e2.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e2.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCell.class).getVisualRowIndex()));

        EnhancedCell e3 = mock(EnhancedCell.class);
        when(e3.isEndOfProcessingCell()).thenReturn(false);
        when(e3.getVisualRowIndex()).thenReturn(3);
        when(e3.getNumericValue()).thenReturn(31L);
        when(e3.isPrime()).thenReturn(true);
        when(e3.compareTo(any())).thenAnswer(
            inv -> Integer.compare(
                e3.getVisualRowIndex(),
                inv.getArgument(0, EnhancedCell.class).getVisualRowIndex()));

        EnhancedCell eEndOfProc = mock(EnhancedCell.class);
        when(eEndOfProc.isEndOfProcessingCell()).thenReturn(true);
        when(eEndOfProc.getVisualRowIndex()).thenReturn(Integer.MAX_VALUE);

        // Adding the cells _not_ sorted by visual row index
        inputQueue.add(e1);
        inputQueue.add(e3);
        inputQueue.add(e2);
        // But the EOP is always the last item added to Outputter's
        // input queue.
        inputQueue.add(eEndOfProc);

        // Act

        outputter.run();

        // Assert

        // Even the end of processing is removed and moved to an internal queue
        assertThat(inputQueue.size()).isEqualTo(0);

        // TODO: assert what Outputter outputted!
    }
}
