package com.rbenes;

import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Outputter implements Runnable {

    ArrayBlockingQueue<EnhancedCell> inputAbq;
    PriorityQueue<EnhancedCell> pq;

    int nextExpectedVisualIndex;

    public Outputter(ArrayBlockingQueue<EnhancedCell> inputAbq) {
        this.inputAbq = inputAbq;
        this.nextExpectedVisualIndex = 1;
        this.pq = new PriorityQueue<>();
    }

    @Override
    public void run() {

        while (true) {

            try {
                pq.add(inputAbq.take());

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

        while (!pq.isEmpty()) {

            EnhancedCell head = pq.peek();

            if (pq.size() == 1 && head.isEndOfProcessingCell()) {
                return true;
            }

            if (head.getVisualRowIndex() == nextExpectedVisualIndex) {
                head = pq.remove();
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
    
}
