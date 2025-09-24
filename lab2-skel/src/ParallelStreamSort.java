/**
 * Sort using Java's ParallelStreams and Lambda functions.
 *
 * Hints:
 * - Do not take advice from StackOverflow.
 * - Think outside the box.
 *      - Stream of threads?
 *      - Stream of function invocations?
 *
 * By default, the number of threads in parallel stream is limited by the
 * number of cores in the system. You can limit the number of threads used by
 * parallel streams by wrapping it in a ForkJoinPool.
 *      ForkJoinPool myPool = new ForkJoinPool(threads);
 *      myPool.submit(() -> "my parallel stream method / function");
 */
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Stream;

public class ParallelStreamSort implements Sorter {

    public final int threads;
    private final ForkJoinPool pool;

    public ParallelStreamSort(int threads) {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Available Cores: " + cores);

        this.threads = Math.max(1, threads);
        this.pool = new ForkJoinPool(
                this.threads,
                new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                    @Override
                    public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                        ForkJoinWorkerThread t =
                                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
                        t.setDaemon(true);
                        t.setName("PStreamSort-" + t.getPoolIndex());
                        return t;
                    }
                },
                null,
                false
        );
    }

    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length < 2) return;
        int[] aux = new int[arr.length];
        pool.submit(() -> mergesortParallel(arr, aux, 0, arr.length - 1)).join();
    }

    @Override
    public int getThreads() {
        return threads;
    }

    private void mergesortParallel(int[] arr, int[] aux, int left, int right) {
        if (right - left <= 1) {
            if (arr[left] > arr[right]) {
                int t = arr[right];
                arr[right] = arr[left];
                arr[left] = t;
            }
            return;
        }
        int mid = left + ((right - left) >>> 1);

        // Run left/right recursively using Parallel Stream + lambdas
        Stream.<Runnable>of(
                () -> mergesortParallel(arr, aux, left, mid),
                () -> mergesortParallel(arr, aux, mid + 1, right)
        ).parallel().forEach(Runnable::run);

        merge(arr, aux, left, mid, right);
    }

    private void merge(int[] arr, int[] aux, int left, int mid, int right) {
        int len = right - left + 1;
        int leftPtr = left;
        int rightPtr = mid + 1;

        for (int i = 0; i < len; i++) {
            if (leftPtr > mid || (rightPtr <= right && arr[rightPtr] < arr[leftPtr])) {
                aux[left + i] = arr[rightPtr++];
            } else {
                aux[left + i] = arr[leftPtr++];
            }
        }
        System.arraycopy(aux, left, arr, left, len);
    }
}
