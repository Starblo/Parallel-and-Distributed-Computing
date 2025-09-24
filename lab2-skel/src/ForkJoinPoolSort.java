/**
 * Sort using Java's ForkJoinPool.
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinWorkerThread;

public class ForkJoinPoolSort implements Sorter {
        public final int threads;
        private final ForkJoinPool pool;

        public ForkJoinPoolSort(int threads) {
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
                int d = ceilLog2(threads);
                int[] aux = new int[arr.length];
                pool.invoke(new Worker(arr, aux, 0, arr.length - 1, d));
        }

        @Override
        public int getThreads() {
                return threads;
        }

        private int ceilLog2(int n) {
        if (n <= 1) return 0;
        return 32 - Integer.numberOfLeadingZeros(n - 1);
        }


        private void mergesort(int[] arr, int[] aux, int left, int right)
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
                mergesort(arr, aux, left, mid);
                mergesort(arr, aux, mid + 1, right);
                merge(arr, aux, left, mid, right);
        }

        private void merge(int[] arr, int[] aux, int left, int mid, int right)
        {
                int len = right - left + 1;
                int left_ptr = left;
                int right_ptr = mid + 1;
                for(int i = 0; i < len; i++)
                {
                        if (left_ptr > mid || (right_ptr <= right && arr[right_ptr] < arr[left_ptr]))
                        {
                                aux[left + i] = arr[right_ptr];
                                right_ptr++;
                        }
                        else
                        {
                                aux[left + i] = arr[left_ptr];
                                left_ptr++;
                        }
                }
                System.arraycopy(aux, left, arr, left, len);
        }

        private class Worker extends RecursiveAction {
                private final int[] arr;
                private final int[] aux;
                private final int left;
                private final int right;
                private final int depth;

                Worker(int[] arr, int[] aux, int left, int right, int depth) {
                        this.arr = arr;
                        this.aux = aux;
                        this.left = left;
                        this.right = right;
                        this.depth = depth;
                }

                @Override
                protected void compute() {
                        if (right <= left) return;

                        if (depth == 0) {
                                mergesort(arr, aux, left, right);
                                return;
                        }

                        int mid = left + (right - left) / 2;
                        Worker a = new Worker(arr, aux, left, mid, depth - 1);
                        Worker b = new Worker(arr, aux, mid + 1, right, depth - 1);
                        invokeAll(a, b);
                        merge(arr, aux, left, mid, right);
                }
        }
}
