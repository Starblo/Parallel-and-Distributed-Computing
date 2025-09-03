public class MainA {
    private static volatile int counter = 0;

    public static class Incrementer implements Runnable {
        private int increments;

        public Incrementer(int increments) {
            this.increments = increments;
        }

        @Override
        public void run() {
            for (int i = 0; i < increments; i++) {
                counter++;
            }
        }
    }

    public static void main(String [] args) throws InterruptedException {
        int n = 4;
        int increments = 1_000_000;
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(new Incrementer(increments));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Final counter value: " + counter);
        System.out.println("Expected value: " + (n * increments));
    }
}
