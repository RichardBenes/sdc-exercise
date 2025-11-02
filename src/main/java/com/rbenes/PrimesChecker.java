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

    // TODO: use properties for constants (sheet number, column...)
    // TODO: why is the 'x' value considered as 2?
    public static void main( String[] args ) throws InvalidFormatException, IOException {

        if (args.length != 2) {
            log.error("Expected file name as a single argument");
            System.exit(-1);
        }

        ArrayBlockingQueue<EnhancedCell> abq = new ArrayBlockingQueue<>(5);
        ArrayBlockingQueue<EnhancedCell> pbq = new ArrayBlockingQueue<>(5);

        PrimalityChecker pca = new PrimalityChecker(abq, pbq);
        PrimalityChecker pcb = new PrimalityChecker(abq, pbq);
        Outputter ou = new Outputter(pbq);

        // TODO: use ThreadPool
        Thread ta = new Thread(pca, "ta");
        Thread tb = new Thread(pcb, "tb");
        Thread tou = new Thread(ou, "tou");

        ta.start();
        tb.start();
        tou.start();

        var filename = args[1];
        log.debug("Processing {}", filename);
        
        try (Workbook w = new XSSFWorkbook(new File(filename))) {

            Sheet sheet = w.getSheetAt(0);

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
            tou.join();

        } catch (FileNotFoundException f) {
            log.error("Could not open the file {}. Isn't it opened by a different process?", filename);
        } catch (Exception e) {
            log.error("Workbook processing has failed", e);
        }
    }
}
