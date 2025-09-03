public class MainB {
    static int N = 5;
    static final Object[] chop = new Object[N];
    static volatile boolean start = false;

    static {
        for (int i = 0; i < N; i++) {
            chop[i] = new Object();
        }
    }
    
    static void log(int id, String msg) {
        System.out.printf("P%d: %s%n", id, msg);
    }

    public static class Philosopher implements Runnable {
        final int id;
        final Object left, right;

        Philosopher(int id, Object left, Object right) {
            this.id = id;
            this.left = left;
            this.right = right;
        }

        public void run() {
            while (!start) 
                Thread.onSpinWait();

            if (id % 2 == 0) {
                // Even philosophers pick up right first
                synchronized (right) {
                    log(id, "Picked Right");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    synchronized (left) {
                        log(id, "Picked Left, eating");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    log(id, "Put Left");
                }
                log(id, "Put Right");
            } else {
                // Odd philosophers pick up left first
                synchronized (left) {
                    log(id, "Picked Left");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    synchronized (right) {
                        log(id, "Picked Right, eating");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    log(id, "Put Right");
                }
                log(id, "Put Left");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            N = Integer.parseInt(args[0]);
        Thread[] ts = new Thread[N];

		long startTime = System.nanoTime();
        for (int i = 0; i < N; i++) {
            Object left  = chop[i];
            Object right = chop[(i + 1) % N];
            ts[i] = new Thread(new Philosopher(i, left, right), "P" + i);
            ts[i].start();
        }

        start = true;

        Thread.sleep(3000);

        boolean allBlocked = true;
        for (Thread t : ts) {
            Thread.State s = t.getState();
            System.out.printf("%s state = %s%n", t.getName(), s);
            if (s != Thread.State.BLOCKED) allBlocked = false;
        }

		long endTime = System.nanoTime();
        if (allBlocked) {
            System.out.println(">>> Deadlock detected (all philosophers are BLOCKED waiting for the right chopstick).");
        } else {
            System.out.println(">>> No full deadlock observed in this run (try again, or add a loop to increase chance).");
        }
		long elapsedTime = endTime - startTime; // Calculate the elapsed time
        System.out.println("Deadlock time for " + N + " philosophers: " + elapsedTimeSec + " seconds");
    }
}
