public class MainC {
    static final Object lock = new Object();
    static int sharedInt = 0;
    static boolean done = false;
	static long tDone, tSeen;

	public static class Incrementer implements Runnable {
        @Override public void run() {
            for (int i = 0; i < 1000000; i++)
				sharedInt++;
            synchronized (lock) {
                done = true;
                tDone = System.nanoTime();
                lock.notifyAll();
            }
        }
	}

	public static class Printer implements Runnable {
		@Override public void run() {
			synchronized (lock) {
				while (!done) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
			tSeen = System.nanoTime();
			System.out.println("C: delay_ns=" + (tSeen - tDone));
			System.out.println("Current value of sharedIntC: " + sharedInt);
		}
	}

	public static void main(String [] args) throws InterruptedException {
		Thread incrementerThread = new Thread(new Incrementer());
		Thread printerThread = new Thread(new Printer());

		incrementerThread.start();
		printerThread.start();
		incrementerThread.join();
		printerThread.join();
	}
}
