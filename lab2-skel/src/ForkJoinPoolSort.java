/**
 * Sort using Java's ForkJoinPool.
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinWorkerThread;

public class ForkJoinPoolSort implements Sorter {
        public final int threads;
        private final ForkJoinPool pool;
        // Keep a modest cutoff to avoid tiny tasks; tune if needed
        private static final int SEQ_CUTOFF = 1 << 15; // 32768 by default

        public ForkJoinPoolSort(int threads) {
                this.threads = Math.max(1, threads);
                // Use a daemon thread factory so JVM can exit after measurements
                this.pool = new ForkJoinPool(
                        this.threads,
                        new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                                @Override
                                public ForkJoinWorkerThread newThread(ForkJoinPool p) {
                                        ForkJoinWorkerThread t =
                                                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
                                        t.setDaemon(true);
                                        t.setName("FJPSort-" + t.getPoolIndex());
                                        return t;
                                }
                        },
                        null,
                        false
                );
        }

        @Override
        public void sort(int[] arr) {
                if (arr == null || arr.length <= 1) return;
                // Invoke a single task for the whole array
                pool.invoke(new Worker(arr, 0, arr.length - 1));
        }

        @Override
        public int getThreads() {
                return threads;
        }

        // === SequentialSort-compatible sequential mergesort (base case and merge style) ===
        private void mergesort(int[] arr, int left, int right)
        {
                if (right - left <= 1)
                {
                        if (left < right && arr[left] > arr[right])
                        {
                                int tempt = arr[right];
                                arr[right] = arr[left];
                                arr[left] = tempt;
                        }
                        return;
                }
                int mid = left + (right - left) / 2;
                mergesort(arr, left, mid);
                mergesort(arr, mid + 1, right);
                merge(arr, left, mid, right);
        }

        // merge() restored to match SequentialSort's variable names
        private void merge(int[] arr, int left, int mid, int right)
        {
                int len = right - left + 1;
                int[] tempt_arr = new int[len];
                int left_ptr = left;
                int right_ptr = mid + 1;
                for(int i = 0; i < len; i++)
                {
                        if (left_ptr > mid || (right_ptr <= right && arr[right_ptr] < arr[left_ptr]))
                        {
                                tempt_arr[i] = arr[right_ptr];
                                right_ptr++;
                        }
                        else
                        {
                                tempt_arr[i] = arr[left_ptr];
                                left_ptr++;
                        }
                }
                System.arraycopy(tempt_arr, 0, arr, left, len);
        }

        private class Worker extends RecursiveAction {
                private final int[] arr;
                private final int left;
                private final int right;

                Worker(int[] arr, int left, int right) {
                        this.arr = arr;
                        this.left = left;
                        this.right = right;
                }

                @Override
                protected void compute() {
                        int len = right - left + 1;
                        if (len <= 1) return;

                        if (len <= SEQ_CUTOFF) {
                                // run the original sequential mergesort to keep behavior identical
                                mergesort(arr, left, right);
                                return;
                        }

                        int mid = left + (right - left) / 2;
                        Worker leftTask = new Worker(arr, left, mid);
                        Worker rightTask = new Worker(arr, mid + 1, right);
                        // fork both and wait
                        invokeAll(leftTask, rightTask);
                        // After both halves are sorted, merge in current thread
                        merge(arr, left, mid, right);
                }
        }
}
