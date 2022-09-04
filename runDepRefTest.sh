#!/bin/bash

DATASET=$1
TIMES=$2
PWD=`pwd`
if [ ${DATASET} == "enron" ]
then 
    INPUT_PATH="${PWD}/enron_dep_ref_10k.xlsx"
    DATASET_PATH="${PWD}/enron_xls"
    SHEETNAME="Enron"
else
    INPUT_PATH="${PWD}/github_dep_ref_10k.xlsx"
    DATASET_PATH="${PWD}/github_xlsx"
    SHEETNAME="Github"
fi
MODEL_TYPE=("TACO" "NoComp")
DEP_TYPE=("M" "L")

mvn compile

for time in `seq 1 $TIMES`
do
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
          filename="${filename}_max_taco_${time}.csv"
        else
          filename="${filename}_max_nocomp_${time}.csv"
        fi
      else
        if [ "$model" == "TACO" ]
        then
          filename="${filename}_long_taco_${time}.csv"
        else
          filename="${filename}_long_nocomp_${time}.csv"
        fi
      fi
      echo $INPUT_PATH $SHEETNAME $filename $DATASET_PATH $dep $model
      mvn exec:java -Dexec.mainClass="org.dataspread.sheetanalyzer.mainTest.DepRefTest" -Dexec.args="$INPUT_PATH $SHEETNAME $filename $DATASET_PATH all $dep $model"
    done
  done
done

