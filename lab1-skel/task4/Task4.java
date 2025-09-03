import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Task4 {

    public static class Semaphore {
        private int value;
        private final LinkedList<WaitThread> threads = new LinkedList<>();

        private static class WaitThread {
            boolean granted = false;
        }

        public Semaphore(int initial) {
            this.value = initial;
        }

        public void signal() {
            WaitThread toWake = null;
            synchronized (this) {
                value++;
                if (value <= 0) {
                    toWake = threads.removeFirst();
                }
            }
            if (toWake != null) {
                synchronized (toWake) {
                    toWake.granted = true;
                    toWake.notify();
                }
            }
        }

        public void s_wait() {
            WaitThread currentThread = null;
            synchronized (this) {
                value--;
                if (value < 0) {
                    currentThread = new WaitThread();
                    threads.addLast(currentThread);
                } else {
                    return;
                }
            }

            synchronized (currentThread) {
                while (!currentThread.granted) {
                    try {
                        currentThread.wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        test(3, 10, 500);
        simpleTest();         // test signal from main wakes one waiter
        System.out.println("All tests finished.");
    }


    static void test(int capacity, int threadCount, int iterationsPerThread) throws InterruptedException {
        System.out.printf("test: capacity=%d threads=%d iters=%d",
                capacity, threadCount, iterationsPerThread);

        final Semaphore sem = new Semaphore(capacity);
        final AtomicInteger active = new AtomicInteger(0);
        final AtomicInteger maxActive = new AtomicInteger(0);
        final AtomicBoolean violation = new AtomicBoolean(false);

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < threadCount; t++) {
            Thread thr = new Thread(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    sem.s_wait();
                    int cur = active.incrementAndGet();
                    maxActive.getAndUpdate(prev -> Math.max(prev, cur));
                    if (cur > capacity) {
                        violation.set(true);
                    }
                    try {
                    Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    active.decrementAndGet();
                    sem.signal();
                }
            });
            threads.add(thr);
        }

        long start = System.currentTimeMillis();
        threads.forEach(Thread::start);
        for (Thread th : threads) th.join();
        long dur = System.currentTimeMillis() - start;

        System.out.printf("maxActive=%d, violation=%s, ticurrentThread=%dms\n",
                maxActive.get(), violation.get(), dur);

        if (violation.get()) {
            System.out.println("ERROR!");
        } else {
            System.out.println("OK!");
        }
    }

    static void simpleTest() throws InterruptedException {
        System.out.println("simple test");

        final Semaphore sem = new Semaphore(1);

        Thread holdThread = new Thread(() -> {
            sem.s_wait();
            System.out.println("[holdThread] acquired semaphore, holding...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sem.signal();
            System.out.println("[holdThread] released semaphore");
        });

        Thread waitThread = new Thread(() -> {
            System.out.println("[waitThread] trying to acquire semaphore...");
            sem.s_wait();
            System.out.println("[waitThread] acquired semaphore!");
            sem.signal();
        });

        holdThread.start();
        Thread.sleep(200);
        waitThread.start();

        Thread.sleep(500);
        System.out.println("[main] waitThread state: " + waitThread.getState());

        holdThread.join();
        waitThread.join();
        System.out.println("[main] test finished.");
    }

}
