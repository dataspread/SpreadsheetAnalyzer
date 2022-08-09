#!/bin/bash

PWD=`pwd`
INPUT_PATH="${PWD}/enron_dep_ref.xlsx"
DATASET_PATH="${PWD}/enron_xls"
SHEETNAME=Enron
MODEL_TYPE=("TACO" "NoComp")
DEP_TYPE=("M" "L")

mvn compile

for i in "${!MODEL_TYPE[@]}"
do
    model=${MODEL_TYPE[i]}
    for j in "${!DEP_TYPE[@]}"
    do
      dep=${DEP_TYPE[j]}
      filename="$SHEETNAME"
      if [ "$dep" == "M" ]
      then
        if [ "$model" == "TACO" ]
        then
          filename="${filename}_max_taco.csv"
        else
          filename="${filename}_max_nocomp.csv"
        fi
      else
        if [ "$model" == "TACO" ]
        then
          filename="${filename}_long_taco.csv"
        else
          filename="${filename}_long_nocomp.csv"
        fi
      fi
      echo $INPUT_PATH $SHEETNAME $filename $DATASET_PATH $dep $model
      mvn exec:java -Dexec.mainClass="org.dataspread.sheetanalyzer.mainTest.DepRefTest" -Dexec.args="$INPUT_PATH $SHEETNAME $filename $DATASET_PATH $dep $model"
    done
done