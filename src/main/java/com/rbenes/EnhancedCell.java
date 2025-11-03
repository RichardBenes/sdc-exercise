package com.rbenes;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

// TODO: remove hardcoded column B
@Log4j2
public class EnhancedCell implements Comparable<EnhancedCell> {

    @Getter
    private Cell cell;

    @Getter
    private Primality primality;

    private Long numericValue;

    @Getter
    // Using kind of the Null Object Pattern
    // https://youtu.be/rQ7BzfRz7OY?si=0xRl9nzpCdA8v6xn
    private final boolean endOfProcessingCell;

    public EnhancedCell(Cell c) {
        this.endOfProcessingCell = false;
        this.cell = c;
        this.primality = Primality.UNKNOWN_YET;
        this.numericValue = null;
    }

    private EnhancedCell() {
        this.endOfProcessingCell = true;
        this.cell = null;
        this.primality = Primality.INVALID;
        this.numericValue = null;
    }

    public static EnhancedCell createEndOfProcessingCell() {
        return new EnhancedCell();
    }

    @Override
    public int compareTo(EnhancedCell o) {
        return Integer.compare(
            getVisualRowIndex(), o.getVisualRowIndex());
    }

    public int getVisualRowIndex() {

        if (isEndOfProcessingCell()) {
            // This is safe; max. number of rows in Excel is around 1 bilion,
            // we're returning value above 2 bilions.
            return Integer.MAX_VALUE;
        }

        return cell.getRowIndex() + 1;
    }

    public boolean isPrime() {
        return this.primality == Primality.PRIME;
    }

    public Long getNumericValue() {

        if (this.primality == Primality.UNKNOWN_YET) {
            computeCellsNumericContent();
            return numericValue;
        }
        
        return numericValue;
    }

    public String getOriginalValue() {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return "<%s>".formatted(cell.getCellType());
        }
    }

    public boolean computePrimality(AKS aks) {

        if (primality == Primality.UNKNOWN_YET) {
            computeCellsNumericContent();
        }

        if (primality == Primality.INVALID) {
            return false;
        }

        // Let's first check for trivial cases
        if ((numericValue % 2 == 0) || (numericValue % 5 == 0)) {
            this.primality = Primality.COMPOSITE;
            return false;
        }
        
        if (aks.checkIsPrime(numericValue)) {
            this.primality = Primality.PRIME;
        } else {
            this.primality = Primality.COMPOSITE;
        }

        return this.primality == Primality.PRIME;
    }

    public String getCellInfo() {

        return switch (cell.getCellType()) {
            case CellType.NUMERIC ->
                "Cell B%s has num value %s, prime: %s"
                    .formatted(
                        getVisualRowIndex(),
                        Double.toString(cell.getNumericCellValue()),
                        primality);
            case CellType.STRING ->
                "Cell B%s has str value %s, prime: %s"
                    .formatted(
                        getVisualRowIndex(),
                        cell.getStringCellValue(),
                        primality);
            default ->
                "Cell B%s is of type %s - such cells are ignored by this program"
                    .formatted(
                        getVisualRowIndex(),
                        cell.getCellType());
        };
    }

    public void computeCellsNumericContent() {

        this.primality = Primality.UNKNOWN_YET_NUMVAL_COMPUTED;

        switch (cell.getCellType()) {
            case CellType.STRING -> {
                    try {
                        numericValue = Long.parseLong(
                            cell.getStringCellValue().strip(), 
                            10);
                    } catch (NumberFormatException nfe) {
                        numericValue = null;
                        primality = Primality.INVALID;
                    }
                }

            case CellType.NUMERIC -> {
                    // Check for non-integer numbers.
                    // This won't unfortunately work for numbers with a large "span",
                    // like 500000001.00000001...
                    if (cell.getNumericCellValue() % 1.0D != 0.0D) {
                        this.numericValue = null;
                        primality = Primality.INVALID;
                    } else {
                        this.numericValue = Double.valueOf(
                            cell.getNumericCellValue()).longValue();
                    }
                }
            default -> {
                    this.numericValue = null;
                    primality = Primality.INVALID;                
                }
        };
    }
}
