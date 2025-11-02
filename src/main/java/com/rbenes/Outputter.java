package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Outputter implements Runnable {

    // PrimalityCheckers output the cells in potentially random order;
    // so we use this type of queue to order them back.
    ArrayBlockingQueue<EnhancedCell> inputAbq;
    int lastCheckedVisualIndex;

    public Outputter(ArrayBlockingQueue<EnhancedCell> inputAbq) {
        this.inputAbq = inputAbq;
        this.lastCheckedVisualIndex = 0;
    }

    @Override
    public void run() {

        EnhancedCell cell;

        while(true) {

            try {
                cell = inputAbq.take();

                if (cell.isEndOfProcessingCell()) {
                    break;
                }

                // if (cell. lastCheckedVisualIndex) {

                // }

            } catch (InterruptedException ie) {
                log.error("Outputter has been interrupted", ie);
                return;
            }

            if (cell.isPrime()) {
                log.info("B{} has PRIME value {}", 
                    cell.getVisualRowIndex(),
                    cell.getNumericValue());
            } else {
                log.debug("B{} has {} value {}", 
                    cell.getVisualRowIndex(),
                    cell.getPrimality(),
                    cell.getNumericValue());
            }
        }

        log.debug("Thread {} is done.",
            Thread.currentThread().getName());            
    }
    
}
