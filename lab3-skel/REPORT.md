# Lab 3 - Validating Linearizability of Lock-free Skiplists

- Group X
- Lastname, Firstname and Lastname, Firstname

# 1. Measuring execution time

## 1.1 Measurement program

We modified the measurement program to ...

## 1.2 PDC experiments

# 2. Identify and validate linearization points

## 2.1 Identify linearization points

For `find()`, the linearization point is at the last call to `succ = curr.next[level].get(marked);`

For `add()`, if the operation is unsuccessful, the linearization point is the same as the successful `find()` (see above). If the add operation is successful, the linearization point is at line 55.

```
 if(!pred.next[bottomLevel].compareAndSet(succ,newNode, false,false))
 ```

 The linearization point of the unsuccessful `remove()` is the `find()` method call on line 77. It's the same as the unsuccesssful `find()` (see above). If remove is successful, the linearization point is at line 96 `nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true)`. If the `compareAndSet()` call failed, but the next reference is marked, then another thread must have conrrently removed it. The linearization point of this unsuccessful `remove()` is the linearization of the `remove()` method by the thread that succssfully marked the next field. This linearization point must occur during the `remove()` call because the `find()` call found the node unmarked before it found it marked.

 The linearization point of `contains()` is the last call to line 147 or line 150 `succ=curr.next[level].get(marked);`

## 2.2 Develop a validation method

## 2.3. Locked time sampling

## 2.4. Lock-free time sampling with local log

## 2.5. Lock-free time sampling with global Log

## 2.6. PDC experiments

## 2.7. Extra task: global log from scratch
