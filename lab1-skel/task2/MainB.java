public class MainB {
	static int sharedInt = 0;
	static volatile boolean done = false;
	static volatile long tDone, tSeen;

	public static class Incrementer implements Runnable {
		public void run() {
			for (int i = 0; i < 1000000; i++) {
				sharedInt++;
			}
			tDone = System.nanoTime();
			done = true;
		}
	}

	public static class Printer implements Runnable {
		public void run() {
			while (!done) {
				Thread.onSpinWait();
			}
			tSeen = System.nanoTime();
			System.out.println("B: delay_ns=" + (tSeen - tDone));
			System.out.println("Current value of sharedIntB: " + sharedInt);
		}
	}

	public static void main(String [] args) {
		Thread incrementerThread = new Thread(new Incrementer());
		Thread printerThread = new Thread(new Printer());
		
		incrementerThread.start();
		printerThread.start();
	}
}
