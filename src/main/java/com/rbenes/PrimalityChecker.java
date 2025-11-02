package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<EnhancedCell> inputAbq;
    final String primalityCheckerName;

    public PrimalityChecker(
        ArrayBlockingQueue<EnhancedCell> inputAbq,
        String name
    ) {
        this.aks = new AKS();
        this.inputAbq = inputAbq;
        this.primalityCheckerName = name;
    }

    @Override
    public void run() {

        EnhancedCell cell;

        while (true) {

            try {
                cell = inputAbq.take();

                if (cell.isEndOfProcessingCell()) {

                    // We need to inform everyone else too -
                    // - so we put it back first
                    inputAbq.put(cell);
                    log.debug("PrimalityChecker {} from thread {} is done.", 
                        primalityCheckerName,
                        Thread.currentThread().getName());

                    break;
                }

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker {} has been interrupted", primalityCheckerName, ie);
                return;
            }

            cell.computePrimality(aks);

            log.debug("P. r. of {}: {}", 
                primalityCheckerName, cell.getCellInfo());
        }
    }
}

