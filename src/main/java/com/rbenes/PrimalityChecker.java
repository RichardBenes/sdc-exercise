package com.rbenes;

import java.util.concurrent.ArrayBlockingQueue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrimalityChecker implements Runnable {

    boolean resultIsPrime;
    AKS aks;
    ArrayBlockingQueue<EnhancedCellI> main2PCQ;
    ArrayBlockingQueue<EnhancedCellI> PC2OutQ;

    public PrimalityChecker(
        ArrayBlockingQueue<EnhancedCellI> main2PCQ,
        ArrayBlockingQueue<EnhancedCellI> PC2OutQ,
        AKS aks
    ) {
        this.aks = aks;
        this.main2PCQ = main2PCQ;
        this.PC2OutQ = PC2OutQ;
    }

    @Override
    public void run() {

        EnhancedCellI cell;

        mainloop: while (true) {

            try {
                cell = main2PCQ.take();

                switch (cell) {
                    case EnhancedCell ec -> {
                        log.debug("Processing cell B{}", ec.getVisualRowIndex());

                        ec.computePrimality(aks);
                        
                        PC2OutQ.put(ec);
                    }

                    case EndOfProcessingCell eoc -> {
                        // We need to inform everyone else too -
                        // - so we put it back first                        
                        main2PCQ.put(eoc);
                        break mainloop;
                    }
                }

            } catch (InterruptedException ie) {
                log.error("PrimalityChecker has been interrupted", ie);
                return;
            }
        }

        log.debug("Thread is done.");        
    }
}

