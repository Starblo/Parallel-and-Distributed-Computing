#!/usr/bin/env bash

# Do not run this script directly on PDC.

# This stops the script when a command's exit code is non-zero (i.e., error).
#SBATCH -A edu25.dd2443

# The name of the script is myjob
#SBATCH -J Sort

# The partition
#SBATCH -p shared

# 20 minutes wall clock time will be given to this job
#SBATCH -t 00:20:00

#SBATCH --output=task3.out    # Output file format: jobname_jobid.out

# Number of MPI processes
#SBATCH -n 1

#SBATCH --nodes=1

#SBATCH --cpus-per-task=96

ml PDC java

set -e

ALGO=ExecutorService
THREADS=1
SIZE=10000000
WARMUP=10
MEASURE=10
SEED=42

javac MeasureMain.java
# java MeasureMain $ALGO $THREADS $SIZE $WARMUP $MEASURE $SEED

for THREADS in 2 4 8 16 32 48 64 96; do
	java MeasureMain $ALGO $THREADS $SIZE $WARMUP $MEASURE $SEED
done
