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
DEP_TYPE="M"

TEST_FILE_FOLDER=$TEST_HOME/dataset/graphbuild_top20/$DATASET

for RUN in `seq 1 $RUNS`
do
	for FULL_FILE_NAME in $TEST_FILE_FOLDER/*
	do
		FILE_NAME=$(basename "$FULL_FILE_NAME")
		$TEST_HOME/runOneDepRefTest.sh $DATASET "$FILE_NAME" $TEST_APPROACH $DEP_TYPE $RUN
	done	
done

