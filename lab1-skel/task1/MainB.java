public class MainB {
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

    long run_experiments(int nThreads, int incrementsPerThread) throws InterruptedException {
        counter = 0; // reset counter
        Thread[] threads = new Thread[nThreads];

        long startTime = System.nanoTime();

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(new Incrementer(incrementsPerThread));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.nanoTime();
        System.out.println("Final counter value: " + counter);
        System.out.println("Expected value: " + (nThreads * incrementsPerThread));

        return endTime - startTime;
    }

    public static void main(String[] args) throws InterruptedException {
        MainB experiment = new MainB();
        int nThreads = 4;
        int incrementsPerThread = 1_000_000;

        long duration = experiment.run_experiments(nThreads, incrementsPerThread);
        System.out.println("Elapsed time (ns): " + duration);
    }
}
