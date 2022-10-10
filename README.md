# SheetAnalyzer

SheetAnalyzer is a library for spreadsheet's formula dependency management. This branch is the code repository of our ICDE 2023 paper.

## Dataset
- The full dataset we tested in the paper can be found [here](https://github.com/dataspread/dataset). 
- [graphbuild_top20](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/graphbuild_top20) and [lookup_top20](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/lookup_top20) contain the max1 - max10 files from 2 datasets we used for comparison testing in paper.

## Results
- [experiment_results](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/experiment_results) contains the experimental results we showed in paper.
- A [Jupyter Notebook](https://github.com/dataspread/sheetanalyzer/blob/taco_icde/experiment_analysis.ipynb) is provided to analyze the results.

## Usage
- The main code for formula dependency management is [here](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/src/main/java/org/dataspread/sheetanalyzer/dependency).
- Scripts for running experiments are [here](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/src/main/java/org/dataspread/sheetanalyzer/mainTest).
- For example, to collect statistics of TACO from files in Enron dataset, you can use the following command:
```bash
  mvn exec:java -Dexec.mainClass="org.dataspread.sheetanalyzer.mainTest.TestSheetAnalyzer" -Dexec.args="[Path of Enron Dataset] [Output Path] TACO True False"
```
