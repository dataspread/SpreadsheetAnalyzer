package org.dataspread.sheetanalyzer.mainTest;

import org.dataspread.sheetanalyzer.SheetAnalyzer;
import org.dataspread.sheetanalyzer.dependency.DependencyGraph;
import org.dataspread.sheetanalyzer.dependency.DependencyGraphNoComp;
import org.dataspread.sheetanalyzer.dependency.DependencyGraphTACO;
import org.dataspread.sheetanalyzer.dependency.util.PatternType;
import org.dataspread.sheetanalyzer.util.Pair;
import org.dataspread.sheetanalyzer.util.Ref;
import org.dataspread.sheetanalyzer.util.RefImpl;
import org.dataspread.sheetanalyzer.util.SheetNotSupportedException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class MainTestUtil {

    public static void writePerSheetStat(SheetAnalyzer sheetAnalyzer,
                                         PrintWriter statPW,
                                         boolean inRowCompression) {
        String fileName = sheetAnalyzer.getFileName().replace(",", "-");
        long numFormulae = sheetAnalyzer.getNumOfFormulae();
        long numEdges = sheetAnalyzer.getNumEdges();
        long numVertices = sheetAnalyzer.getNumVertices();
        long numCompEdges = sheetAnalyzer.getNumCompEdges();
        long numCompVertices = sheetAnalyzer.getNumCompVertices();

        Pair<Ref, Long> mostDeps = new Pair(new RefImpl(-1, -1), 0);
        Pair<Ref, Long> longestDeps = new Pair(new RefImpl(-1, -1), 0);

        long[] numCompEdgesPerPattern = new long[PatternType.values().length];
        long[] numEdgesPerPattern = new long[PatternType.values().length];
        long mostDepLookupTime = 0;
        long longestDepLookupTime = 0;
        long mostDepLookupSize = 0;
        long longestDepLookupSize = 0;

        if (!inRowCompression) {
            mostDeps = sheetAnalyzer.getRefWithMostDeps();
            longestDeps = sheetAnalyzer.getRefWithLongestDepChain();

            // MostDeps
            long start = System.currentTimeMillis();
            String depSheetName = mostDeps.first.getSheetName();
            DependencyGraph depGraph = sheetAnalyzer.getDependencyGraphs().get(depSheetName);
            mostDepLookupSize = depGraph.getDependents(mostDeps.first).size();
            mostDepLookupTime = System.currentTimeMillis() - start;
            // LongestDeps
            start = System.currentTimeMillis();
            depSheetName = longestDeps.first.getSheetName();
            depGraph = sheetAnalyzer.getDependencyGraphs().get(depSheetName);
            longestDepLookupSize = depGraph.getDependents(longestDeps.first).size();
            longestDepLookupTime = System.currentTimeMillis() - start;

            if (sheetAnalyzer.getIsCompression()) {
                sheetAnalyzer.getTACODepGraphs().forEach((sheetName, tacoGraph) -> {
                    tacoGraph.forEach((prec, depWithMetaList) -> {
                        depWithMetaList.forEach(depWithMeta -> {
                            Ref dep = depWithMeta.getRef();
                            PatternType patternType = depWithMeta.getPatternType();

                            int patternIndex = patternType.ordinal();
                            numCompEdgesPerPattern[patternIndex] += 1;

                            long numPatternEdges = dep.getCellCount();
                            if (patternType.ordinal() >= PatternType.TYPEFIVE.ordinal() &&
                                    patternType != PatternType.NOTYPE) {
                                long gap = patternType.ordinal() - PatternType.TYPEFIVE.ordinal() + 1;
                                numPatternEdges = (numPatternEdges - 1) / (gap + 1) + 1;
                            }
                            numEdgesPerPattern[patternIndex] += numPatternEdges;
                        });
                    });
                });
            }
        }

        if (numEdges >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(fileName).append(",")
                    .append(numFormulae).append(",")
                    .append(numVertices).append(",")
                    .append(numEdges).append(",")
                    .append(numCompVertices).append(",")
                    .append(numCompEdges);
            if (!inRowCompression) {
                stringBuilder.append(",")
                        .append(mostDeps.first.getSheetName()).append(",")
                        .append(mostDeps.first).append(",")
                        .append(mostDeps.second).append(",")
                        .append(mostDepLookupTime).append(",")
                        .append(mostDepLookupSize).append(",")
                        .append(longestDeps.first.getSheetName()).append(",")
                        .append(longestDeps.first).append(",")
                        .append(longestDeps.second).append(",")
                        .append(longestDepLookupTime).append(",")
                        .append(longestDepLookupSize).append(",");
                if (sheetAnalyzer.getIsCompression()) {
                    for (int pIdx = 0; pIdx < numCompEdgesPerPattern.length; pIdx++) {
                        stringBuilder.append(numCompEdgesPerPattern[pIdx]).append(",")
                                .append(numEdgesPerPattern[pIdx]).append(",");
                    }
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("\n");
            statPW.write(stringBuilder.toString());
        }
    }

    public static void TestComparisonStat(PrintWriter statPW, String filePath) {
        boolean inRowCompression = false;
        try {
            SheetAnalyzer sheetCompAnalyzer = new SheetAnalyzer(filePath, inRowCompression, true);
            SheetAnalyzer sheetNoCompAnalyzer = new SheetAnalyzer(filePath, inRowCompression, false);

            Pair<Ref, Long> mostDeps = new Pair(new RefImpl(-1, -1), 0);
            Pair<Ref, Long> longestDeps = new Pair(new RefImpl(-1, -1), 0);
            long mostDepCompLookupTime = 0, longestDepCompLookupTime = 0,
                    mostDepCompLookupSize = 0, longestDepCompLookupSize = 0,
                    mostDepCompPostProcesedLookupSize = 0, longestDepCompPostProcesedLookupSize = 0,
                    mostDepCompPostProcessedLookupTime = 0, longestDepCompPostProcessedLookupTime = 0;
            long mostDepNoCompLookupTime = 0, longestDepNoCompLookupTime = 0,
                    mostDepNoCompLookupSize = 0, longestDepNoCompLookupSize = 0,
                    mostDepNoCompPostProcesedLookupSize = 0, longestDepNoCompPostProcesedLookupSize = 0,
                    mostDepNoCompPostProcessedLookupTime = 0, longestDepNoCompPostProcessedLookupTime = 0;

            String fileName = sheetCompAnalyzer.getFileName().replace(",", "-");
            long numEdges = sheetCompAnalyzer.getNumEdges();
            mostDeps = sheetCompAnalyzer.getRefWithMostDeps();
            longestDeps = sheetCompAnalyzer.getRefWithLongestDepChain();
            Set<Ref> result, processedResult;

            // MostDeps - Comp
            long start = System.currentTimeMillis();
            String depSheetName = mostDeps.first.getSheetName();
            DependencyGraphTACO depCompGraph = (DependencyGraphTACO) sheetCompAnalyzer.getDependencyGraphs().get(depSheetName);
            result = depCompGraph.getDependents(mostDeps.first);
            mostDepCompLookupSize = result.size();
            mostDepCompLookupTime = System.currentTimeMillis() - start;
            processedResult = depCompGraph.postProcessDependents(result);
            mostDepCompPostProcesedLookupSize = processedResult.size();
            mostDepCompPostProcessedLookupTime = System.currentTimeMillis() - start;

            // MostDeps - NoComp
            start = System.currentTimeMillis();
            DependencyGraphNoComp depNoCompGraph = (DependencyGraphNoComp) sheetNoCompAnalyzer.getDependencyGraphs().get(depSheetName);
            result = depNoCompGraph.getDependents(mostDeps.first);
            mostDepNoCompLookupSize = result.size();
            mostDepNoCompLookupTime = System.currentTimeMillis() - start;
            processedResult = depNoCompGraph.postProcessDependents(result);
            mostDepNoCompPostProcesedLookupSize = processedResult.size();
            mostDepNoCompPostProcessedLookupTime = System.currentTimeMillis() - start;

            // LongestDeps - Comp
            start = System.currentTimeMillis();
            depSheetName = longestDeps.first.getSheetName();
            depCompGraph = (DependencyGraphTACO) sheetCompAnalyzer.getDependencyGraphs().get(depSheetName);
            result = depCompGraph.getDependents(longestDeps.first);
            longestDepCompLookupSize = result.size();
            longestDepCompLookupTime = System.currentTimeMillis() - start;
            processedResult = depCompGraph.postProcessDependents(result);
            longestDepCompPostProcesedLookupSize = processedResult.size();
            longestDepCompPostProcessedLookupTime = System.currentTimeMillis() - start;

            // LongestDeps - NoComp
            start = System.currentTimeMillis();
            depNoCompGraph = (DependencyGraphNoComp) sheetNoCompAnalyzer.getDependencyGraphs().get(depSheetName);
            result = depNoCompGraph.getDependents(longestDeps.first);
            longestDepNoCompLookupSize = result.size();
            longestDepNoCompLookupTime = System.currentTimeMillis() - start;
            processedResult = depNoCompGraph.postProcessDependents(result);
            longestDepNoCompPostProcesedLookupSize = processedResult.size();
            longestDepNoCompPostProcessedLookupTime = System.currentTimeMillis() - start;

            if (numEdges >= 10) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(fileName).append(",")
                        .append(mostDeps.first.getSheetName()).append(",")
                        .append(mostDeps.first).append(",")
                        .append(mostDeps.second).append(",")
                        .append(mostDepCompLookupTime).append(",")
                        .append(mostDepCompLookupSize).append(",")
                        .append(mostDepCompPostProcesedLookupSize).append(",")
                        .append(mostDepCompPostProcessedLookupTime).append(",")
                        .append(mostDepNoCompLookupTime).append(",")
                        .append(mostDepNoCompLookupSize).append(",")
                        .append(mostDepNoCompPostProcesedLookupSize).append(",")
                        .append(mostDepNoCompPostProcessedLookupTime).append(",")
                        .append(longestDeps.first.getSheetName()).append(",")
                        .append(longestDeps.first).append(",")
                        .append(longestDeps.second).append(",")
                        .append(longestDepCompLookupTime).append(",")
                        .append(longestDepCompLookupSize).append(",")
                        .append(longestDepCompPostProcesedLookupSize).append(",")
                        .append(longestDepCompPostProcessedLookupTime).append(",")
                        .append(longestDepNoCompLookupTime).append(",")
                        .append(longestDepNoCompLookupSize).append(",")
                        .append(longestDepNoCompPostProcesedLookupSize).append(",")
                        .append(longestDepNoCompPostProcessedLookupTime);
                stringBuilder.append("\n");
                statPW.write(stringBuilder.toString());
            }
        } catch (SheetNotSupportedException e) {
            System.out.println(e.getMessage());
        } catch (OutOfMemoryError e) {
            System.out.println(e.getMessage());
        }
    }

    public static Ref cellStringToRef(String cellString) {
        int colIndex = cellString.charAt(0) - 'A';
        int rowIndex = Integer.parseInt(cellString.substring(1)) - 1;

        return new RefImpl(rowIndex, colIndex);
    }
}
