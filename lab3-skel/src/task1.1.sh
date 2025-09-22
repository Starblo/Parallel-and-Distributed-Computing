ml PDC java
set -e
exec > task1.1.out 2>&1

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
MAX_VALUE=100000         # <V>
OPS_PER_THREAD=100000    # <O>
WARMUP_ROUNDS=3          # <W>
MEASURE_ROUNDS=5         # <M>

# Loop parameters (edit this block to select the matrix)
DISTS="Uniform Normal"           # <D>
MIXES="1:1:8 1:1:0"              # <A>:<R>:<C>
THREADS_LIST="1 2 4 8"           # <T>

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