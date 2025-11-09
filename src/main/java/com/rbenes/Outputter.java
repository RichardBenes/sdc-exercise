package com.rbenes;

import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Outputter implements Runnable {

    ArrayBlockingQueue<EnhancedCellI> PC2OutQ;
    PriorityQueue<EnhancedCellI> sortQ;

    int nextExpectedVisualIndex;

    public Outputter(ArrayBlockingQueue<EnhancedCellI> PC2OutQ) {
        this.PC2OutQ = PC2OutQ;
        this.nextExpectedVisualIndex = 1;
        this.sortQ = new PriorityQueue<>();
    }

    @Override
    public void run() {

        while (true) {

            try {
                sortQ.add(PC2OutQ.take());

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

        while (!sortQ.isEmpty()) {

            EnhancedCellI head = sortQ.peek();

            switch (head) {
                case EnhancedCell ec -> {
                    if (ec.getVisualRowIndex() == nextExpectedVisualIndex) {
                        sortQ.remove();
                        nextExpectedVisualIndex += 1;
                        processCell.accept(ec);
                    } else {
                        return false;
                    }
                }
                case EndOfProcessingCell _ -> { 
                    // If the head, that is, the element with the _lowest_ number
                    // is endOfProcessing, it's clear that all the other elements
                    // have been processed as well                    
                    return true; 
                }
            }
        }

        return false;
    }

    private void logCell(EnhancedCell cell) {

        // TODO: Maybe a crazy idea - introduce PrimeCell and match on that here?
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
