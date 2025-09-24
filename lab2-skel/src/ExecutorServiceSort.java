import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

public class ExecutorServiceSort implements Sorter {
        public final int threads;
        private final ExecutorService pool;

        public ExecutorServiceSort(int threads) {
                int cores = Runtime.getRuntime().availableProcessors();
                System.out.println("Available Cores: " + cores);
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
                int[] aux = new int[arr.length];
                if (threads <= 1) {
                        mergesort(arr, aux, 0, arr.length - 1);
                        return;
                }
                int maxDepth = ceilLog2(threads);
                try {
                        parallelMergeSort(arr, aux, 0, arr.length - 1, maxDepth);
                } catch (InterruptedException | ExecutionException e) {
                        mergesort(arr, aux, 0, arr.length - 1);
                }
        }

        public int getThreads() {
                return threads;
        }

        private int ceilLog2(int n) {
        if (n <= 1) return 0;
        return 32 - Integer.numberOfLeadingZeros(n - 1);
        }

        private void parallelMergeSort(int[] arr, int[] aux, int left, int right, int depth)
                        throws InterruptedException, ExecutionException {
                if (right <= left) return;
                if (depth == 0) {
                        mergesort(arr, aux, left, right);
                        return;
                }

                int mid = left + (right - left) / 2;

                Future<?> leftTask = pool.submit(() -> {
                        try {
                                parallelMergeSort(arr, aux, left, mid, depth - 1);
                        } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                        }
                });

                parallelMergeSort(arr, aux, mid + 1, right, depth - 1);
                leftTask.get();

                merge(arr, aux, left, mid, right);
        }

        public void mergesort(int[] arr, int[] aux, int left, int right)
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

        public void merge(int[] arr, int[] aux, int left, int mid, int right)
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
}
