#!/bin/bash

ABS_PATH="$(readlink -f "${BASH_SOURCE}")"
TEST_HOME="$(dirname $ABS_PATH)"


if [ "$#" -ne 2 ]; then
	echo "Need two parameters: dataset (enron/github); test_approach"
    exit
fi

DATASET=$1
TEST_APPROACH=$2
FILE_NAME=all
RUNS=3
DEP_TYPE=("M" "L")

for run in `seq 1 $TIMES`
do
    for j in "${!DEP_TYPE[@]}"
    do
      DEP=${DEP_TYPE[j]}
      $TEST_HOME/runOneDepTest.sh $DATASET $FILE_NAME $TEST_APPROACH $DEP $RUN
    done
done

