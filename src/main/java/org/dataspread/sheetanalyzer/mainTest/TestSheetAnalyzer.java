package org.dataspread.sheetanalyzer.mainTest;

import org.dataspread.sheetanalyzer.SheetAnalyzer;
import org.dataspread.sheetanalyzer.dependency.util.PatternType;
import org.dataspread.sheetanalyzer.util.SheetNotSupportedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class TestSheetAnalyzer {

    static String numRefDistFile = "numRefDist.csv";
    static String numEdgesFile = "new_tested_TACO_Dollars_stat_2.csv";

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("Need four arguments: \n" +
                    "1) a folder for xls(x) files or a xls(x) file \n" +
                    "2) a folder for stat output \n" +
                    "3) TACO or NoComp \n" +
                    "4) isDollar True or False \n"
            );
            System.exit(-1);
        }

        boolean inRowCompression = false;
        boolean isCompression = args[2].equals("TACO");
        boolean isDollar = args[3].equals("True");

        String statFolder = args[1];
        String numRefDistPath = statFolder + "/" + numRefDistFile;
        String statPath = statFolder + "/" + numEdgesFile;
        HashMap<Integer, Long> numRefDist = new HashMap<>();

        File inputFile = new File(args[0]);
        File [] fileArray;
        if (inputFile.isDirectory()) {
            fileArray = inputFile.listFiles();
        } else {
            fileArray = new File[] {inputFile};
        }

        if (fileArray != null) {
            int counter = 0;
            try (PrintWriter distPW = new PrintWriter(new FileWriter(numRefDistPath, false));
                 PrintWriter statPW = new PrintWriter(new FileWriter(statPath, false))) {

                // Write headers in stat
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("fileName").append(",")
                        .append("numFormulae").append(",")
                        .append("numVertices").append(",")
                        .append("numEdges").append(",")
                        .append("numCompVertices").append(",")
                        .append("numCompEdges");
                if (!inRowCompression) {
                    stringBuilder.append(",")
                            .append("mostDeps_sheetname").append(",")
                            .append("mostDeps_ref").append(",")
                            .append("mostDeps_count").append(",")
                            .append("mostDepLookupTime").append(",")
                            .append("mostDepLookupSize").append(",")
                            .append("longestDeps_sheetname").append(",")
                            .append("longestDeps_ref").append(",")
                            .append("longestDeps_count").append(",")
                            .append("longestDepLookupTime").append(",")
                            .append("longestDepLookupSize").append(",");
                    if (isCompression) {
                        long numType = PatternType.values().length;
                        for (int pIdx = 0; pIdx < numType; pIdx++) {
                            stringBuilder.append(PatternType.values()[pIdx].label + "_Comp").append(",")
                                    .append(PatternType.values()[pIdx].label + "_NoComp").append(",");
                        }
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("\n");
                } else {
                    stringBuilder.append("\n");
                }
                statPW.write(stringBuilder.toString());

                for (File file: fileArray) {
                    counter += 1;
                    System.out.println("[" + counter + "/" +
                            fileArray.length + "]: "+ "processing " + file.getName());
                    String filePath = file.getAbsolutePath();
                    try {
                        SheetAnalyzer sheetAnalyzer = new SheetAnalyzer(filePath, inRowCompression,
                                isCompression, isDollar);
                        MainTestUtil.writePerSheetStat(sheetAnalyzer, statPW, inRowCompression);

                        HashMap<Integer, Integer> numRefDistPerSheet = sheetAnalyzer.getRefDistribution();
                        numRefDistPerSheet.forEach((numRefs, count) -> {
                            long existingCount = numRefDist.getOrDefault(numRefs, 0L);
                            numRefDist.put(numRefs, existingCount + count);
                        });

                    } catch (SheetNotSupportedException | OutOfMemoryError | NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
                }
                if (!inRowCompression) {
                    numRefDist.forEach((numRefs, count) ->
                            distPW.write(numRefs + "," + count + "\n"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
