package org.dataspread.sheetanalyzer.tacoTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dataspread.sheetanalyzer.SheetAnalyzer;
import org.dataspread.sheetanalyzer.dependency.util.RefWithMeta;
import org.dataspread.sheetanalyzer.util.Pair;
import org.dataspread.sheetanalyzer.util.Ref;
import org.dataspread.sheetanalyzer.util.RefImpl;
import org.dataspread.sheetanalyzer.util.SheetNotSupportedException;
import org.dataspread.sheetanalyzer.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPruneOverlappingRefs {

    private static SheetAnalyzer sheetAnalyzer;
    private static final String sheetName = "OverlapSheet";
    private static final int maxRows = 10;

    private static File createOverlapSheet1() throws IOException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        int colA = 0, colB = 1, colC = 2;
        Row row = sheet.createRow(0);
        Cell cellA = row.createCell(colA);
        Cell cellB = row.createCell(colB);
        Cell cellC = row.createCell(colC);
        cellA.setCellValue(1);
        cellB.setCellValue(10);
        cellC.setCellFormula("A1 + B1");
        for (int i = 1; i < maxRows; i++) {
            row = sheet.createRow(i);
            cellA = row.createCell(colA);
            cellB = row.createCell(colB);
            cellC = row.createCell(colC);
            cellA.setCellValue(i + 1);
            cellB.setCellValue(10);
            cellC.setCellFormula("A" + (i + 1));
        }
        TestUtil.createAnEmptyRowWithTwoCols(sheet, maxRows, colA, colB);

        File xlsTempFile = TestUtil.createXlsTempFile();
        FileOutputStream outputStream = new FileOutputStream(xlsTempFile);

        workbook.write(outputStream);
        workbook.close();

        return xlsTempFile;
    }

    private static File createNormalSheet() throws IOException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        int colA = 0, colB = 1, colC = 2;
        for (int i = 0; i < maxRows; i++) {
            Row row = sheet.createRow(i);
            Cell cellA = row.createCell(colA);
            Cell cellB = row.createCell(colB);
            Cell cellC = row.createCell(colC);
            cellA.setCellValue(i + 1);
            cellB.setCellValue(10);
            cellC.setCellFormula("A" + (i + 1));
        }
        TestUtil.createAnEmptyRowWithTwoCols(sheet, maxRows, colA, colB);

        File xlsTempFile = TestUtil.createXlsTempFile();
        FileOutputStream outputStream = new FileOutputStream(xlsTempFile);

        workbook.write(outputStream);
        workbook.close();

        return xlsTempFile;
    }

    @Test
    public void testNormalSheet() throws IOException, SheetNotSupportedException {
        File normalSheet = createNormalSheet();
        sheetAnalyzer = SheetAnalyzer.createSheetAnalyzer(normalSheet.getAbsolutePath());
        SheetAnalyzer sheetAnalyzer2 = SheetAnalyzer.createSheetAnalyzer(normalSheet.getAbsolutePath());

        Map<String, Pair<Map<Ref, List<RefWithMeta>>, Map<Ref, List<RefWithMeta>>>> depGraph1 = sheetAnalyzer
                .getTACODepGraphs();

        Map<String, Pair<Map<Ref, List<RefWithMeta>>, Map<Ref, List<RefWithMeta>>>> depGraph2 = sheetAnalyzer2
                .getNonOverlappingGraphs();

        Set<Ref> depToPrec = depGraph1.get(sheetName).first.keySet();
        Set<Ref> depToPrecGroundTruth = depGraph2.get(sheetName).first.keySet();
        Assertions.assertTrue(TestUtil.hasSameRefs(depToPrec, depToPrecGroundTruth));

        Set<Ref> precToDep = depGraph1.get(sheetName).first.keySet();
        Set<Ref> precToDepGroundTruth = depGraph2.get(sheetName).first.keySet();
        Assertions.assertTrue(TestUtil.hasSameRefs(precToDep, precToDepGroundTruth));
    }

    /**
     * A1 and B1 of RF pattern is only referenced by cell C1.
     * | 1 | 10 | =A1+B1 |
     * | 2 | 10 | =A2 |
     * | 3 | 10 | =A3 |
     * | 4 | 10 | =A4 |
     * 
     * Originally is A1:13 -> C1:C3, B1 -> C1
     * Transformed into A2:A3 -> C2:C3, A1 -> C1, B1 -> C1
     */
    @Test
    public void testOverlapSheet1() throws IOException, SheetNotSupportedException {
        File sheet1 = createOverlapSheet1();
        sheetAnalyzer = SheetAnalyzer.createSheetAnalyzer(sheet1.getAbsolutePath());

        Map<String, Pair<Map<Ref, List<RefWithMeta>>, Map<Ref, List<RefWithMeta>>>> depGraph = sheetAnalyzer
                .getNonOverlappingGraphs();
        Pair<Map<Ref, List<RefWithMeta>>, Map<Ref, List<RefWithMeta>>> sheet = depGraph.get(sheetName);
        Assertions.assertEquals(3, sheet.first.size());
        Assertions.assertEquals(2, sheet.second.size());
        Set<Ref> depToPrecSet = sheet.first.keySet();
        Set<Ref> depToPrecGroundTruth = new HashSet<>();
        depToPrecGroundTruth.add(new RefImpl(0, 0));
        depToPrecGroundTruth.add(new RefImpl(0, 1));
        depToPrecGroundTruth.add(new RefImpl(1, 0, maxRows - 1, 0));
        Assertions.assertTrue(TestUtil.hasSameRefs(depToPrecSet, depToPrecGroundTruth));

        Set<Ref> precToDepSet = sheet.second.keySet();
        Set<Ref> precToDepGroundTruth = new HashSet<>();
        precToDepGroundTruth.add(new RefImpl(0, 2));
        precToDepGroundTruth.add(new RefImpl(1, 2, maxRows - 1, 2));
        Assertions.assertTrue(TestUtil.hasSameRefs(precToDepSet, precToDepGroundTruth));
    }

}
