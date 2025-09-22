# Lab 3 - Validating Linearizability of Lock-free Skiplists

- Group x&zx
- Li, Xin and Hong, Zixiang

# 1. Measuring execution time

## 1.1 Measurement program
Source files: 
- `task1.1.sh`
- `plot1.py`

![Task1.1 plot](src/task1.1_summary.png)

## 1.2 PDC experiments
Source files: 
- `task1.2.sh`
- `plot1.py`

![Task1.2 plot](src/task1.2_summary.png)

### Observation
For write-heavy cases we consistently see A.2 > B.2. For read-heavy cases, on PDC A.1 > B.1, while locally A.1 < B.1 at small thread counts and only crosses over when threads are large.

### Possible explanations
1. A Normal distribution concentrates keys, yielding more duplicates and a smaller effective working set, so `add/remove` bring shorter traversals, hence B.2 faster.
2. For read-heavy mixes, a Normal distribution improves locality but also creates hot spots that disturb readers; on PDC the locality benefit dominates so B.1 is faster, whereas on the local machine at small thread counts hot-spot effects dominate, making A.1 < B.1 until T increases.


# 2. Identify and validate linearization points

## 2.1 Identify linearization points

## 2.2 Develop a validation method

## 2.3. Locked time sampling

## 2.4. Lock-free time sampling with local log

## 2.5. Lock-free time sampling with global Log

## 2.6. PDC experiments

## 2.7. Extra task: global log from scratch
