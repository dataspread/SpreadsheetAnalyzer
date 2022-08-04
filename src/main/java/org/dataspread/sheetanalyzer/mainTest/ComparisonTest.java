package org.dataspread.sheetanalyzer.mainTest;

import org.dataspread.sheetanalyzer.SheetAnalyzer;
import org.dataspread.sheetanalyzer.dependency.util.PatternType;
import org.dataspread.sheetanalyzer.util.SheetNotSupportedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class ComparisonTest {
    static String numEdgesFile = "new_test_compare_stat.csv";

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Need two arguments: \n" +
                    "1) a folder for xls(x) files or a xls(x) file \n" +
                    "2) a folder for stat output \n");
            System.exit(-1);
        }

        String statFolder = args[1];
        String statPath = statFolder + "/" + numEdgesFile;

        File inputFile = new File(args[0]);
        File [] fileArray;
        if (inputFile.isDirectory()) {
            fileArray = inputFile.listFiles();
        } else {
            fileArray = new File[] {inputFile};
        }

        if (fileArray != null) {
            int counter = 0;
            try (PrintWriter statPW = new PrintWriter(new FileWriter(statPath, true))) {

                // Write headers in stat
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("fileName").append(",")
                            .append("mostDeps_sheetname").append(",")
                            .append("mostDeps_ref").append(",")
                            .append("mostDeps_count").append(",")
                            .append("mostDepCompLookupTime").append(",")
                            .append("mostDepCompLookupSize").append(",")
                            .append("mostDepCompPostProcessedLookupSize").append(",")
                            .append("mostDepCompPostProcessedLookupTime").append(",")
                            .append("mostDepNoCompLookupTime").append(",")
                            .append("mostDepNoCompLookupSize").append(",")
                            .append("mostDepNoCompPostProcessedLookupSize").append(",")
                            .append("mostDepNoCompPostProcessedLookupTime").append(",")
                            .append("longestDeps_sheetname").append(",")
                            .append("longestDeps_ref").append(",")
                            .append("longestDeps_count").append(",")
                            .append("longestDepCompLookupTime").append(",")
                            .append("longestDepCompLookupSize").append(",")
                            .append("longestDepCompPostProcessedLookupSize").append(",")
                            .append("longestDepCompPostProcessedLookupTime").append(",")
                            .append("longestDepNoCompLookupTime").append(",")
                            .append("longestDepNoCompLookupSize").append(",")
                            .append("longestDepNoCompPostProcessedLookupSize").append(",")
                            .append("longestDepNoCompPostProcessedLookupTime").append("\n");
                statPW.write(stringBuilder.toString());

                for (File file: fileArray) {
                    counter += 1;
                    System.out.println("[" + counter + "/" +
                            fileArray.length + "]: "+ "processing " + file.getName());
                    String filePath = file.getAbsolutePath();
                    MainTestUtil.TestComparisonStat(statPW, filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
