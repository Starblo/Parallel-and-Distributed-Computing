#!/usr/bin/env bash

# Do not run this script directly on PDC.

# This stops the script when a command's exit code is non-zero (i.e., error).
#SBATCH -A edu25.dd2443

# The name of the script is myjob
#SBATCH -J Sort

# The partition
#SBATCH -p shared

# 10 minutes wall clock time will be given to this job
#SBATCH -t 00:10:00

# Number of MPI processes
#SBATCH -n 1

#SBATCH --nodes=1

#SBATCH --cpus-per-task=4

ml PDC java

set -e

ALGO=Sequential
THREADS=2
SIZE=1000000
WARMUP=10
MEASURE=1000
SEED=42

javac MeasureMain.java

for THREADS in 1 2 4 8; do
	java MeasureMain $ALGO $THREADS $SIZE $WARMUP $MEASURE $SEED
done
