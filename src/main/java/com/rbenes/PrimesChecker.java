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
public class PrimesChecker {

    // TODO: handle exceptions more gracefully
    // TODO: handle the file being used by some other process
    public static void main( String[] args ) throws InvalidFormatException, IOException {

        if (args.length != 2) {
            log.error("Expected file name as a single argument");
            System.exit(-1);
        }

        ArrayBlockingQueue<EnhancedCell> abq = new ArrayBlockingQueue<>(5);

        PrimalityChecker pca = new PrimalityChecker(abq, "pcA");
        PrimalityChecker pcb = new PrimalityChecker(abq, "pcB");

        Thread ta = new Thread(pca, "ta");
        Thread tb = new Thread(pcb, "tb");

        ta.start();
        tb.start();

        var filename = args[1];
        
        try (Workbook w = new XSSFWorkbook(new File(filename))) {

            Sheet sheet = w.getSheetAt(0);

            // Counting is zero based in Java, but 1-based in Excel...

            log.info("Last row has number {}, and there are {} phys. rows", 
                sheet.getLastRowNum(),
                sheet.getPhysicalNumberOfRows());

            for (Row row : sheet) {

                EnhancedCell cellB = new EnhancedCell(row.getCell(1));

                abq.put(cellB);
                log.info("Added cell B{} to the queue.", cellB.getVisualRowIndex());
            }

            abq.put(EnhancedCell.createEndOfProcessingCell());

            ta.join();
            tb.join();

            // Now there should be exactly a single None in the queue

        } catch (FileNotFoundException f) {
            log.error("Could not open the file {}. Isn't it opened by a different process?", filename);
        } catch (Exception e) {
            log.error("Workbook processing has failed", e);
        }
    }
}
