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

mvn compile

for time in `seq 1 $TIMES`
do
    for i in "${!MODEL_TYPE[@]}"
    do
        model=${MODEL_TYPE[i]}
        filename="$SHEETNAME"
        if [ ${model} == "TACO" ]
        then
            filename="${filename}_taco_modify_${time}.csv"
        else
            filename="${filename}_nocomp_modify_${time}.csv"
        fi
        echo $INPUT_PATH $SHEETNAME $filename $DATASET_PATH $model
        mvn exec:java -Dexec.mainClass="org.dataspread.sheetanalyzer.mainTest.GraphModifyTest" -Dexec.args="$INPUT_PATH $SHEETNAME $filename $DATASET_PATH all $model"
    done
done