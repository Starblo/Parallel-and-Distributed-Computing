/**
 * Sort using Java's ExecutorService.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

public class ExecutorServiceSort implements Sorter {
        public final int threads;
        private final ExecutorService pool;
        private static final int SEQ_CUTOFF = 65536;

        public ExecutorServiceSort(int threads) {
                this.threads = Math.max(1, threads);
                this.pool = Executors.newFixedThreadPool(this.threads, new ThreadFactory() {
                    @Override public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("ExecSort-" + t.getId());
                        return t;
                    }
                });
        }

        public void sort(int[] arr) {
                if (arr == null || arr.length <= 1) return;
                if (threads <= 1) {
                        mergesort(arr, 0, arr.length - 1);
                        return;
                }
                int maxDepth = floorLog2(threads);
                try {
                        parallelMergeSort(arr, 0, arr.length - 1, maxDepth);
                } catch (InterruptedException | ExecutionException e) {
                        // Fallback: sequential mergesort
                        mergesort(arr, 0, arr.length - 1);
                }
        }

        public int getThreads() {
                return threads;
        }

        private int floorLog2(int n) {
                int lg = 0;
                while ((n >>= 1) > 0) ++lg;
                return lg;
        }

        private void parallelMergeSort(int[] arr, int left, int right, int depth)
                        throws InterruptedException, ExecutionException {
                if (right <= left) return;
                if ((right - left + 1) <= SEQ_CUTOFF || depth == 0) {
                        mergesort(arr, left, right);
                        return;
                }

                int mid = left + (right - left) / 2;

                Future<?> leftTask = pool.submit(() -> {
                        try {
                                parallelMergeSort(arr, left, mid, depth - 1);
                        } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                        }
                });

                parallelMergeSort(arr, mid + 1, right, depth - 1);
                leftTask.get();

                merge(arr, left, mid, right);
        }

        public void mergesort(int[] arr, int left, int right)
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

        public void merge(int[] arr, int left, int mid, int right)
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
}
