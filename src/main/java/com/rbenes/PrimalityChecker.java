package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<EnhancedCell> main2PCQ;
    ArrayBlockingQueue<EnhancedCell> PC2OutQ;

    public PrimalityChecker(
        ArrayBlockingQueue<EnhancedCell> main2PCQ,
        ArrayBlockingQueue<EnhancedCell> PC2OutQ,
        AKS aks
    ) {
        this.aks = aks;
        this.main2PCQ = main2PCQ;
        this.PC2OutQ = PC2OutQ;
    }

    @Override
    public void run() {

        EnhancedCell cell;

        while (true) {

            try {
                cell = main2PCQ.take();

                if (cell.isEndOfProcessingCell()) {

                    // We need to inform everyone else too -
                    // - so we put it back first
                    main2PCQ.put(cell);
                    break;
                }

                log.debug("Processing cell B{}", cell.getVisualRowIndex());

                cell.computePrimality(aks);
                
                PC2OutQ.put(cell);                

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker has been interrupted", ie);
                return;
            }
        }

        log.debug("Thread is done.");        
    }
}

