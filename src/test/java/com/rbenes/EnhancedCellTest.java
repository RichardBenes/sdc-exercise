package com.rbenes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class EnhancedCellTest {

    @Test
    void initialPrimality() {

        var ec = new EnhancedCell(mock(Cell.class));

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET);
        assertThat(ec.isEndOfProcessingCell()).isFalse();
    }

    @Test
    void endOfProcessing() {

        var ec = EnhancedCell.createEndOfProcessingCell();

        assertThat(ec.getPrimality()).isEqualTo(Primality.INVALID);
        assertThat(ec.isEndOfProcessingCell()).isTrue();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            5, 7, -1
            4, 2, 1
            2, 2, 0
            """)
    void compare(int a, int b, int exp) {

        var cellA = mock(Cell.class);
        var cellB = mock(Cell.class);

        when(cellA.getRowIndex()).thenReturn(a - 1);
        when(cellB.getRowIndex()).thenReturn(b - 1);

        var ecA = new EnhancedCell(cellA);
        var ecB = new EnhancedCell(cellB);

        assertThat(ecA.compareTo(ecB)).isEqualTo(exp);
    }

    @Test
    void getVisualRowIndex_validCell() {

        var cell = mock(Cell.class);

        when(cell.getRowIndex()).thenReturn(0);

        var ec = new EnhancedCell(cell);

        assertThat(ec.getVisualRowIndex()).isEqualTo(1);
    }

    @Test
    void getVisualRowIndex_endOfProcessingCell() {

        var ec = EnhancedCell.createEndOfProcessingCell();

        assertThat(ec.getVisualRowIndex()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void getNumericValue_precomputed_validNumber() {

        var c = mock(Cell.class);

        when(c.getCellType()).thenReturn(CellType.STRING);
        when(c.getStringCellValue()).thenReturn("10");

        var ec = new EnhancedCell(c);

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET);

        ec.computeCellsNumericContent();

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET_NUMVAL_COMPUTED);

        assertThat(ec.getNumericValue()).isEqualTo(10L);

        verify(c, times(1)).getCellType();
        verify(c, times(1)).getStringCellValue();
    }

    @Test
    void getNumericValue_notPrecomputed_validNumber() {

        var c = mock(Cell.class);

        when(c.getCellType()).thenReturn(CellType.STRING);
        when(c.getStringCellValue()).thenReturn("10");

        var ec = new EnhancedCell(c);

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET);

        assertThat(ec.getNumericValue()).isEqualTo(10L);

        verify(c, times(1)).getCellType();
        verify(c, times(1)).getStringCellValue();
    }    

    @Test
    void getNumericValue_precomputed_invalidNumber() {

        var c = mock(Cell.class);

        when(c.getCellType()).thenReturn(CellType.STRING);
        when(c.getStringCellValue()).thenReturn("abc");

        var ec = new EnhancedCell(c);

        assertThat(ec.getPrimality()).isEqualTo(Primality.UNKNOWN_YET);

        ec.computeCellsNumericContent();

        assertThat(ec.getPrimality()).isEqualTo(Primality.INVALID);

        assertThat(ec.getNumericValue()).isNull();

        verify(c, times(1)).getCellType();
        verify(c, times(1)).getStringCellValue();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 10, STRING, 10
            15, 15, NUMERIC, 15.0
            0, 0, BOOLEAN, <BOOLEAN>
            -1, -1, FORMULA, <FORMULA>
            """)
    void getOriginalValue_string(
        double dbVal, 
        String stringVal, 
        CellType ctype, 
        String expected
    ) {
        var c = mock(Cell.class);
        
        when(c.getCellType()).thenReturn(ctype);

        when(c.getStringCellValue()).thenReturn(stringVal);
        when(c.getNumericCellValue()).thenReturn(dbVal);

        var ec = new EnhancedCell(c);
        
        assertThat(ec.getOriginalValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            22, 22, COMPOSITE, false, false
            55, 55, COMPOSITE, false, false
            100, 100, COMPOSITE, false, false
            abc, 0, INVALID, false, false
            7, 7, PRIME, true, true
            211, 211, PRIME, true, true
            231, 231, COMPOSITE, false, true
            """)
    void computePrimality_validNumber(
        String strNum,
        long longNum,
        Primality expectedPrimality,
        boolean shouldBePrime,
        boolean shouldCallAks
    ) {

        var aks = mock(AKS.class);
        when(aks.checkIsPrime(any(Long.class))).thenReturn(shouldBePrime);

        var c = mock(Cell.class);

        when(c.getCellType()).thenReturn(CellType.STRING);
        when(c.getStringCellValue()).thenReturn(strNum);

        var ec = new EnhancedCell(c);

        var prime = ec.computePrimality(aks);

        assertThat(prime).isEqualTo(shouldBePrime);
        assertThat(ec.getPrimality()).isEqualTo(expectedPrimality);

        verify(c, times(1)).getStringCellValue();
        verify(c, times(1)).getCellType();
        verify(aks, times(shouldCallAks ? 1 : 0)).checkIsPrime(eq(longNum));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            NUMERIC, 33, 33, 3, 'Cell B4 has num value 33.0, prime: UNKNOWN_YET'
            STRING, 77, 77, 4, 'Cell B5 has str value 77, prime: UNKNOWN_YET'
            FORMULA, 0, 0, 0, 'Cell B1 is of type FORMULA - such cells are ignored by this program'
            BLANK, 0, 0, 10, 'Cell B11 is of type BLANK - such cells are ignored by this program'
            """)
    void getCellInfo(
        CellType cellType, 
        String strValue, 
        double dblValue,
        int zeroBasedRowIdx,
        String expectedOutput
    ) {
        var c = mock(Cell.class);

        when(c.getRowIndex()).thenReturn(zeroBasedRowIdx);
        when(c.getCellType()).thenReturn(cellType);
        when(c.getStringCellValue()).thenReturn(strValue);
        when(c.getNumericCellValue()).thenReturn(dblValue);

        var ec = new EnhancedCell(c);

        assertThat(ec.getCellInfo()).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            STRING, 10, 10, 10, false
            STRING, '  10  ', 10, 10, false
            STRING, abc, 0, null, true
            NUMERIC, 10, 10, 10, false
            NUMERIC, 10.03, 10.03, null, true
            FORMULA, 0, 0, null, true
            BLANK, 0, 0, null, true
            """, nullValues = {"null"})
    void computeCellsNumericContent(
        CellType cellType,
        String strValue,
        double dblValue,
        Long expectedValue,
        boolean expectedInvalidPrimality
    ) {
        var c = mock(Cell.class);

        when(c.getCellType()).thenReturn(cellType);
        when(c.getStringCellValue()).thenReturn(strValue);
        when(c.getNumericCellValue()).thenReturn(dblValue);

        var ec = new EnhancedCell(c);

        ec.computeCellsNumericContent();

        assertThat(ec.getNumericValue()).isEqualTo(expectedValue);
        assertThat(ec.getPrimality()).isNotEqualTo(Primality.UNKNOWN_YET);
        assertThat(ec.getPrimality() == Primality.INVALID).isEqualTo(expectedInvalidPrimality);
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void isPrime(
        boolean aksDecidedThatItIsPrime
    ) {
        var c = mock(Cell.class);
        when(c.getCellType()).thenReturn(CellType.STRING);
        when(c.getStringCellValue()).thenReturn("3");

        var aks = mock(AKS.class);
        when(aks.checkIsPrime(anyLong())).thenReturn(aksDecidedThatItIsPrime);

        var ec = new EnhancedCell(c);

        ec.computePrimality(aks);

        assertThat(ec.isPrime()).isEqualTo(aksDecidedThatItIsPrime);
    }
}
