# Lab 2 - Java Parallel Programming and Sorting Algorithms
- Group x&zx
- Li, Xin and Hong, Zixiang

## Task 1: Sequential Sort
We chose to implement MergeSort

Source files:

- `SequentialSort.java`

## Task 2: Amdahl's Law

Classical Amdahl's law:

$$
S(N)=\frac{1}{(1-p)+\frac{p}{N}}.
$$

Here is a plot of our version of Amdahl's law 

![amdahl's law plot](data/amdahl.png)

We see that when $threads<15$, the speedup increases sharply; beyond that point, adding more threads brings negligible returns.

- As the thread count $N$ grows, recursion produces smaller subproblems.
- As thread increases, task scheduling, synchronization and cache effects cost more


### Our model: Amdahl + overhead

We add a linear overhead term to classical Amdahl’s law:
$$
S(N)=\frac{1}{(1-p)+\frac{p}{N}+\delta(N)}
$$

$$
\delta(N)=a+bN\quad (N>1),\qquad \delta(1)=0.
$$

Classical Amdahl assumes perfect division of work and negligible overhead, which overestimates speedup.  
By adding $\delta(N)$, we capture $N$-dependent costs (scheduling, synchronization, cache effects), so the curves visualized realistically as $N$ grows.

![amdahl + overhead plot](data/plot_overhead1.png)
![amdahl + overhead plot](data/plot_overhead2.png)


## Task 3: ExecutorServiceSort

Source files:

- `ExecutorServiceSort.java`

We implement a fixed thread pool with daemon workers to ensure the JVM exits cleanly after measurements, stop parallel recursion when the depth limit $d=\lfloor\log_2 Thread\rfloor$ is reached. This keeps the merge logic identical to the sequential version and prevents task explosion.

## Task 4: ForkJoinPoolSort

Source files:

- `ForkJoinPoolSort.java`

We implement a parallel merge sort with a `ForkJoinPool` per subrange. Each task splits at the midpoint, runs both halves in parallel via `invokeAll`, and merges in the parent thread after the joins; stop parallel recursion when the depth limit $d=\lfloor\log_2 Thread\rfloor$ is reached.

## Task 5: ParallelStreamSort

Source files:

- `ParallelStreamSort.java`

We first use 
```
ForkJoinPool pool = new ForkJoinPool(threads);
```
to create a thread pool.


Then, we use `Arrays.stream` to have an stream object of the int array. We use `.parallel()` to have a parallel stream of the int array. We use `.sorted()` to sort the stream and use `.toArray()` to convert the result to array.

We use 
```
System.arraycopy(tempt, 0, arr, 0, arr.length);
```
to copy the tempt result to `arr`

## Task 6: Performance measurements with PDC
Out parameters:
- `SIZE=1000000`
- `WARMUP=10`
- `MEASURE=1000`
- `SEED=42`

![time plot](data/time_perf.png)
![speedup plot](data/speedup_perf.png)

#### Are the performance gains/drops between different numbers of threads what you expected?
Yes. Speed improves from 2→8–16 threads, then flattens as thread-management and memory overheads dominate.

#### What implementation ran the fastest/slowest? Was this in accordance with your expectations?
Fastest is `ParallelStream`; slowest is `Sequential`. ExecutorService and ForkJoin are close, which is expected since they share the same merge and depth cap and pay extra scheduling overhead compared to `ParallelStream`.

#### What method was the easiest to implement?
`ParallelStream`, no manual recursion, merge code, or tuning; just a one-line parallel stream.

#### What method do you prefer?
`ParallelStream`, because it is easy and performs best.
