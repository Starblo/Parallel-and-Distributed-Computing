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
                int d = floorLog2(threads);
                pool.invoke(new Worker(arr, 0, arr.length - 1, d));
        }

        @Override
        public int getThreads() {
                return threads;
        }

        private int floorLog2(int n) {
                int lg = 0;
                while ((n >>= 1) > 0) ++lg;
                return lg;
        }

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
                private final int depth;

                Worker(int[] arr, int left, int right, int depth) {
                        this.arr = arr;
                        this.left = left;
                        this.right = right;
                        this.depth = depth;
                }

                @Override
                protected void compute() {
                        if (right <= left) return;

                        if (depth == 0) {
                                mergesort(arr, left, right);
                                return;
                        }

                        int mid = left + (right - left) / 2;
                        Worker a = new Worker(arr, left, mid, depth - 1);
                        Worker b = new Worker(arr, mid + 1, right, depth - 1);
                        invokeAll(a, b);
                        merge(arr, left, mid, right);
                }
        }
}
