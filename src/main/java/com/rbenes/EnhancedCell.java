package com.rbenes;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EnhancedCell implements Comparable<EnhancedCell> {

    @Getter
    Cell cell;

    Primality primality;

    @Getter
    // Using kind of the Null Object Pattern
    // https://youtu.be/rQ7BzfRz7OY?si=0xRl9nzpCdA8v6xn
    final boolean endOfProcessingCell;

    public EnhancedCell(Cell c) {
        this.endOfProcessingCell = false;
        this.cell = c;
        this.primality = Primality.UNKNOWN_YET;
    }

    private EnhancedCell() {
        this.endOfProcessingCell = true;
        this.cell = null;
        this.primality = Primality.INVALID;
    }

    public static EnhancedCell createEndOfProcessingCell() {
        return new EnhancedCell();
    }

    @Override
    public int compareTo(EnhancedCell o) {
        return Integer.compare(
            cell.getRowIndex(), o.getCell().getRowIndex());
    }

    public int getVisualRowIndex() {
        return cell.getRowIndex() + 1;
    }

    public boolean computePrimality(AKS aks) {

        Long longVal = getCellsNumericContent();

        if (longVal == null) {
            this.primality = Primality.INVALID;
            return false;
        }

        // I'll consider 2 more likely to occur - so I'll check it first,
        // before computing the oddity via remainder
        if (longVal == 2 || (longVal % 2 == 0) || (longVal % 5 == 0)) {
            this.primality = Primality.COMPOSITE;
            return false;
        }
        
        if (aks.checkIsPrime(longVal)) {
            this.primality = Primality.PRIME;
        } else {
            this.primality = Primality.COMPOSITE;
        }

        return this.primality == Primality.PRIME;
    }

    public String getCellInfo() {

        int rowNum = cell.getRowIndex() + 1;

        if (cell.getCellType() == CellType.NUMERIC) {

            return "Cell B%s has num value %s, prime: %s"
                .formatted(
                    getVisualRowIndex(),
                    Double.toString(cell.getNumericCellValue()),
                    primality);

        } else if (cell.getCellType() == CellType.STRING) {

            return "Cell B%s has str value %s, prime: %s"
                .formatted(
                    getVisualRowIndex(),
                    cell.getStringCellValue(),
                    primality);

        } else {

            return "Cell B{} is of type {} - such cells are ignored by this program"
                .formatted(
                    rowNum,
                    cell.getCellType());
        }
    }

    public Long getCellsNumericContent() {

        if (cell.getCellType() == CellType.STRING) {

            // I don't want the overhead of an exception every time there
            // is a bad input - so I'm using Apache's lib
            // Unfortunately, not even here can I properly distinguish between
            // properly parsed default value, and failed parse.
            // Luckily though, I don't care - if parsing failed,
            // it's not an prime, and that's all I need to know.
            return NumberUtils.toLong(cell.getStringCellValue().strip(), 2);

        } else if (cell.getCellType() == CellType.NUMERIC) {
            
            // This won't work for numbers with a large "span",
            // like 500000001.00000001
            if (cell.getNumericCellValue() % 1.0D != 0.0D) {
                return null;
            }
            
            return Double.valueOf(cell.getNumericCellValue()).longValue();

        } else {
            return null;
        }
    }

    
}
