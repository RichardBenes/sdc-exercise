package com.rbenes;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.poi.ss.usermodel.*;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<Optional<Cell>> inputAbq;
    final String primalityCheckerName;

    public PrimalityChecker(
        ArrayBlockingQueue<Optional<Cell>> inputAbq,
        String name
    ) {
        this.aks = new AKS();
        this.inputAbq = inputAbq;
        this.primalityCheckerName = name;
    }

    @Override
    public void run() {

        Optional<Cell> maybeC;

        while (true) {

            try {
                maybeC = inputAbq.take();

                if (maybeC.isEmpty()) {

                    // We need to inform everyone else too -
                    // - so we put it back first
                    inputAbq.put(Optional.empty());
                    log.debug("PrimalityChecker {} from thread {} is done.", 
                        primalityCheckerName,
                        Thread.currentThread().getName());
                    break;
                }

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker {} has been interrupted", primalityCheckerName, ie);
                return;
            }

            Cell c = maybeC.get();

            boolean isPrime = checkCell(c);

            log.debug("PrimalityChecker {} processed cell B{} - primality: {}", 
                primalityCheckerName, c.getRowIndex() + 1, isPrime);
        }
    }

    public boolean checkCell(Cell cell) {

        // TODO: this is too late to create EnhancedCell
        var ec = new EnhancedCell(cell);

        resultIsPrime = ec.computePrimality(aks);

        return resultIsPrime;
    }

    public void logCellPrimality(Cell cell) {

        int rowNum = cell.getRowIndex() + 1;

        if (cell.getCellType() == CellType.NUMERIC) {

            log.info("Cell B{} has num value {}, prime: {}", 
                rowNum, 
                Double.toString(cell.getNumericCellValue()), 
                checkCell(cell));
        } else if (cell.getCellType() == CellType.STRING) {
            log.info("Cell B{} has str value {}, prime: {}", 
                rowNum, 
                cell.getStringCellValue(), 
                checkCell(cell));
        } else {
            log.info("Cell B{} is of type {} - such cells are ignored by this program", 
                rowNum,
                cell.getCellType());
        }
    }
}

