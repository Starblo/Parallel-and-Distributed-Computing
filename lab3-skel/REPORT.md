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

For `find()`, the linearization point is the last call to `curr = pred.next[level].getReference();`

For `add()`, if the operation is unsuccessful, the linearization point is the same as the successful `find()` (see above). If the add operation is successful, the linearization point is at:

```
pred.next[bottomLevel].compareAndSet(succ, newNode, false, false)
 ```

 The linearization point of an unsuccessful `remove()` is the `find()` method call. If remove is successful, the linearization point is at line 96 `nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true)`. If the `compareAndSet()` call failed, but the next reference is marked, then another thread must have conrrently removed it. The linearization point of this unsuccessful `remove()` is the linearization of the `remove()` method by the thread that succssfully marked the next field. This linearization point must occur during the `remove()` call because the `find()` call found the node unmarked before it found it marked.

 The linearization point of `contains()` is the last call to `curr = pred.next[level].getReference()`

## 2.2 Develop a validation method

Source files: 
- `Task3.java`

## 2.3. Locked time sampling

The discrepancy is 0 in our tests. In the tests, max value is set to 100000. But it doesn't mean our sampling implementation is fully correct, I believe it is because the max value is too large and is hard to 

## 2.4. Lock-free time sampling with local log

## 2.5. Lock-free time sampling with global Log

## 2.6. PDC experiments

## 2.7. Extra task: global log from scratch
