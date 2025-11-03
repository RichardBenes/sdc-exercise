package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<EnhancedCell> inputAbq;
    ArrayBlockingQueue<EnhancedCell> outputAbq;

    public PrimalityChecker(
        ArrayBlockingQueue<EnhancedCell> inputAbq,
        ArrayBlockingQueue<EnhancedCell> outputAbq,
        AKS aks
    ) {
        this.aks = aks;
        this.inputAbq = inputAbq;
        this.outputAbq = outputAbq;
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
                    break;
                }

                log.debug("Processing cell B{}", cell.getVisualRowIndex());

                cell.computePrimality(aks);
                
                outputAbq.put(cell);                

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker has been interrupted", ie);
                return;
            }
        }

        log.debug("Thread is done.");        
    }
}

