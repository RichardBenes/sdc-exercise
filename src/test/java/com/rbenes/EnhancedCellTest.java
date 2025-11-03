package com.rbenes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.poi.ss.usermodel.Cell;

public class EnhancedCellTest {

    Cell cell;

    @BeforeEach
    void beforeEach() {

        cell = mock(Cell.class);
    }

    @Test
    void test() {

        var ec = new EnhancedCell(cell);

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET);
    }
    
}
