package com.rbenes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SdcExercise {

    // TODO: use properties for constants (n of threads, sheet number, column...)
    public static void main( String[] args ) throws InvalidFormatException, IOException {

        if (args.length != 2) {
            log.error("Expected file name as a single argument");
            System.exit(-1);
        }

        // TODO: queue capacities - as config properties
        ArrayBlockingQueue<EnhancedCell> main2PCQ = new ArrayBlockingQueue<>(5);
        ArrayBlockingQueue<EnhancedCell> PC2OutQ = new ArrayBlockingQueue<>(5);
        
        int cores = Runtime.getRuntime().availableProcessors();

        Thread[] threads = new Thread[cores];

        log.debug("Running with {} threads", cores);

        for (int i = 0; i < threads.length; i += 1) {
            threads[i] = new Thread(
                new PrimalityChecker(
                    main2PCQ, 
                    PC2OutQ,
                    new AKS()), 
                    "tw%s".formatted(i));

            threads[i].start();
        }

        Outputter ou = new Outputter(PC2OutQ);
        Thread tou = new Thread(ou, "tou");
        tou.start();

        var filename = args[1];
        log.debug("Processing file {}", filename);
        
        try (Workbook w = new XSSFWorkbook(new File(filename))) {

            Sheet sheet = w.getSheetAt(0);

            log.debug("Last row has number {}, and there are {} phys. rows", 
                sheet.getLastRowNum(),
                sheet.getPhysicalNumberOfRows());

            for (Row row : sheet) {

                EnhancedCell cellB = new EnhancedCell(row.getCell(1));

                main2PCQ.put(cellB);
                log.debug("Added cell B{} to the queue.", cellB.getVisualRowIndex());
            }

            main2PCQ.put(EnhancedCell.createEndOfProcessingCell());

            for (Thread thread : threads) {
                thread.join();
            }

            PC2OutQ.put(EnhancedCell.createEndOfProcessingCell());
            tou.join();

        } catch (FileNotFoundException f) {
            log.error("Could not open the file {}. Isn't it opened by a different process?", filename);
        } catch (Exception e) {
            log.error("Workbook processing has failed", e);
        }
    }
}
