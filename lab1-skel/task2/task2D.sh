# For Task D, compare the performance
javac MainB.java
javac MainC.java

# Warm up
for i in $(seq 1 3); do java MainB >/dev/null; done
for i in $(seq 1 3); do java MainC >/dev/null; done

# Record
: > D.out
for i in {1..10}; do
  { echo "Run $i (B) :"; java MainB; } >> D.out
  { echo "Run $i (C) :"; java MainC; } >> D.out
done