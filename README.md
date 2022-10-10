# SheetAnalyzer

SheetAnalyzer is a library for analyzing the depenency and formula structure of a spreadsheet. This branch is the code repository of our ICDE 2023 paper.

The main code for formula dependency management is [here](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/src/main/java/org/dataspread/sheetanalyzer/dependency) and scripts for running experiments are [here](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/src/main/java/org/dataspread/sheetanalyzer/mainTest)

The full dataset we tested in the paper is [here](https://github.com/dataspread/dataset). [graphbuild_top10](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/graphbuild_top20) and [lookup_top10](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/lookup_top20) contain the max1 - max10 files from 2 datasets we used for comparison testing in paper.

[experiment_results](https://github.com/dataspread/sheetanalyzer/tree/taco_icde/experiment_results) contains the experimental results we showed in paper and a [Jupyter Notebook](https://github.com/dataspread/sheetanalyzer/blob/taco_icde/experiment_analysis.ipynb) is provided to analyze the results.
