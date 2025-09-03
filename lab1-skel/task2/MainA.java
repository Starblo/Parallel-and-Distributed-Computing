public class MainA {
	static int sharedInt = 0;
	public static class Incrementer implements Runnable {
		public void run() {
			for (int i = 0; i < 1000000; i++) {
				sharedInt++;
			}
		}
	}

	public static class Printer implements Runnable {
		public void run() {
			System.out.println("Current value of sharedInt: " + sharedInt);
		}
	}

	public static void main(String [] args) {
		Thread incrementerThread = new Thread(new Incrementer());
		Thread printerThread = new Thread(new Printer());
		
		incrementerThread.start();
		printerThread.start();
	}
}
