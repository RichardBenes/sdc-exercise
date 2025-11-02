package com.rbenes;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker {

    Cell cell;
    boolean resultIsPrime;
    AKS aks;

    public PrimalityChecker(Cell c) {
        this.cell = c;
        this.aks = new AKS();
    }

    public boolean checkCell() {

        long longVal;

        if (cell.getCellType() == CellType.STRING) {

            // I don't want the overhead of an exception every time there
            // is a bad input - so I'm using Apache's lib
            // Unfortunately, not even here can I properly distinguish between
            // properly parsed default value, and failed parse.
            // Luckily though, I don't care - if parsing failed,
            // it's not an prime, and that's all I need to know.
            longVal = NumberUtils.toLong(cell.getStringCellValue().strip(), 2);

        } else if (cell.getCellType() == CellType.NUMERIC) {
            
            // This won't work for numbers with a large "span",
            // like 500000001.00000001
            if (cell.getNumericCellValue() % 1.0D != 0.0D) {
                return false;
            }
            
            longVal = Double.valueOf(cell.getNumericCellValue()).longValue();
        } else {
            return false;
        }

        // I'll consider 2 more likely to occur - so I'll check it first,
        // before computing the oddity via remainder
        if (longVal == 2 || (longVal % 2 == 0)) {
            return false;
        }
        
        return this.aks.checkIsPrime(longVal);
    }

    public void logCellPrimality(int rowNum) {

        if (cell.getCellType() == CellType.NUMERIC) {

            log.info("Cell B{} has num value {}, prime: {}", 
                rowNum, 
                Double.toString(cell.getNumericCellValue()), 
                checkCell());
        } else if (cell.getCellType() == CellType.STRING) {
            log.info("Cell B{} has str value {}, prime: {}", 
                rowNum, 
                cell.getStringCellValue(), 
                checkCell());
        } else {
            log.info("Cell B{} is of type {} - such cells are ignored by this program", 
                rowNum,
                cell.getCellType());
        }
    }
}

