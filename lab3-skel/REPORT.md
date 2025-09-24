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
- For write-heavy mixes, B.2 < A.2, with the gap widening as threads increase.
- For read-heavy mixes, on PDC B.1 < A.1, while locally A.1 ≲ B.1 at small thread counts and only crosses as threads grow.
- Within each distribution, A.1 < A.2 and B.1 < B.2 (read-heavy beats write-heavy).

### Possible explanations
1. Normal concentrates keys, reducing the effective set and shortening traversals for updates—hence B.2 faster despite some extra contention.
2. Normal improves cache locality but creates hot spots; on PDC locality dominates, while locally at low T hot-spot interference offsets it.
3. Read-heavy mixes avoid the level-0 CAS bottleneck and helping costs, so .1 is consistently faster than .2 for both distributions.

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
