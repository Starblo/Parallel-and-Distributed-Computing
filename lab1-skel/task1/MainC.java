public class MainC {
    private static int counter = 0;

    public static synchronized void increment() {
        counter++;
    }

    public static class Incrementer implements Runnable {
        private int increments;

        public Incrementer(int increments) {
            this.increments = increments;
        }

        @Override
        public void run() {
            for (int i = 0; i < increments; i++) {
                increment();
            }
        }
    }

    public static long run_experiment(int nThreads, int incrementsPerThread) throws InterruptedException {
        counter = 0;
        Thread[] threads = new Thread[nThreads];
        long start = System.nanoTime();

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(new Incrementer(incrementsPerThread));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long end = System.nanoTime();
        return end - start;
    }

    public static double mean(long[] values) {
        long sum = 0;
        for (long v : values) sum += v;
        return (double) sum / values.length;
    }

    public static double stddev(long[] values) {
        double m = mean(values);
        double sumSq = 0;
        for (long v : values) sumSq += (v - m) * (v - m);
        return Math.sqrt(sumSq / values.length);
    }

    public static void main(String[] args) throws InterruptedException {
        int warmupIterations = 5;
        int measureIterations = 10;
        int[] threadCounts = {1, 2, 4, 8, 16, 32, 64};
        int incrementsPerThread = 1_000_000;

        for (int n : threadCounts) {
            for (int i = 0; i < warmupIterations; i++) {
                run_experiment(n, incrementsPerThread);
            }

            long[] times = new long[measureIterations];
            for (int i = 0; i < measureIterations; i++) {
                times[i] = run_experiment(n, incrementsPerThread);
            }

            double avg = mean(times) / 1_000_000.0;
            double sd = stddev(times) / 1_000_000.0;

            System.out.printf("Threads: %2d, Avg: %.3f ms, StdDev: %.3f ms%n",
                    n, avg, sd);
        }
    }
}
