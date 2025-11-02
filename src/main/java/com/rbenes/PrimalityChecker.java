package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<EnhancedCell> inputAbq;
    ArrayBlockingQueue<EnhancedCell> outputAbq;

    public PrimalityChecker(
        ArrayBlockingQueue<EnhancedCell> inputAbq,
        ArrayBlockingQueue<EnhancedCell> outputAbq
    ) {
        this.aks = new AKS();
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
                    outputAbq.put(cell);
                    break;
                }

                cell.computePrimality(aks);
                
                outputAbq.put(cell);                

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker {} has been interrupted", ie);
                return;
            }
        }

        log.debug("Thread {} is done.",
            Thread.currentThread().getName());        
    }
}

