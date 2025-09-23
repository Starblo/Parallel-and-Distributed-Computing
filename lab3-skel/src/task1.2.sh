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

#SBATCH --output=task1.2.out    # Output file format: jobname_jobid.out

# Number of MPI processes
#SBATCH -n 1

#SBATCH --nodes=1

#SBATCH --cpus-per-task=64

ml PDC java
set -e

# -----------------------------------------
# java Main <T> <S> <D> <V> <A>:<R>:<C> <O> <W> <M>
# <T>  Number of threads to use.
# <S>  Default, Locked, LocalLog, GlobalLog version of LockFreeSkipList.
# <D>  Normal or Uniform of sampling.
# <V>  Max value to sample (samples 0-MaxValue).
# <A>:<R>:<C>  Distribution of adds, removes, and contains.
# <O>  Number of operations to execute per thread.
# <W>  Measurement rounds to warm up the JVM.
# <M>  Number of measurements for the final statistics.
# -----------------------------------------

SET_VERSION=Default      # <S>
MAX_VALUE=1000         # <V>
OPS_PER_THREAD=1000000   # <O>
WARMUP_ROUNDS=3          # <W>
MEASURE_ROUNDS=5         # <M>

# Loop parameters (edit this block to select the matrix)
DISTS="Uniform Normal"           # <D>
MIXES="1:1:8 1:1:0"              # <A>:<R>:<C>
THREADS_LIST="1 2 4 8 16 32 48"  # <T>

# Build
javac *.java

# Runs
for D in $DISTS; do
  for MIX in $MIXES; do
    for T in $THREADS_LIST; do
      echo "=== T=$T S=$SET_VERSION D=$D V=$MAX_VALUE MIX=$MIX O=$OPS_PER_THREAD W=$WARMUP_ROUNDS M=$MEASURE_ROUNDS ==="
      java Main $T $SET_VERSION $D $MAX_VALUE $MIX $OPS_PER_THREAD $WARMUP_ROUNDS $MEASURE_ROUNDS
      echo
    done
  done
done