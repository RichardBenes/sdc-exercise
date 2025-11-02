package com.rbenes;

import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Outputter implements Runnable {

    ArrayBlockingQueue<EnhancedCell> inputAbq;
    PriorityQueue<EnhancedCell> orderedQueue;

    int nextExpectedVisualIndex;

    public Outputter(ArrayBlockingQueue<EnhancedCell> inputAbq) {
        this.inputAbq = inputAbq;
        this.nextExpectedVisualIndex = 1;
        this.orderedQueue = new PriorityQueue<>();
    }

    @Override
    public void run() {

        while (true) {

            try {
                orderedQueue.add(inputAbq.take());

                if (drainPqAndFindEndOfProcessing(this::logCell)) {
                    break;
                }

            } catch (InterruptedException ie) {
                log.error("Outputter has been interrupted", ie);
                return;
            }
        }

        log.debug("Thread {} is done.",
            Thread.currentThread().getName());            
    }

    private boolean drainPqAndFindEndOfProcessing(Consumer<EnhancedCell> processCell) {

        while (!orderedQueue.isEmpty()) {

            EnhancedCell head = orderedQueue.peek();

            // If the head, that is, the element with the _lowest_ number
            // is endOfProcessing, it's clear that all the other elements
            // have been processed as well
            if (head.isEndOfProcessingCell()) {
                return true;
            }

            if (head.getVisualRowIndex() == nextExpectedVisualIndex) {
                head = orderedQueue.remove();
                nextExpectedVisualIndex += 1;
                processCell.accept(head);
            } else {
                return false;
            }
        }

        return false;
    }

    private void logCell(EnhancedCell cell) {

        if (cell.isPrime()) {
            log.info("{}", cell.getNumericValue());
            log.debug("B{} has PRIME value {}", 
                cell.getVisualRowIndex(),
                cell.getNumericValue());
        } else {
            log.debug("B{} has {} value {}", 
                cell.getVisualRowIndex(),
                cell.getPrimality(),
                cell.getOriginalValue());
        }        
    }
    
}
