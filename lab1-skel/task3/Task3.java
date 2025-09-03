import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Buffer {
    private final int capacity;
    private final Queue<Integer> queue;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;
    private boolean closed;

    public Buffer(int N) {
        this.capacity = N;
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        this.closed = false;
    }

    public void add(int i) throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                throw new RuntimeException("Buffer is closed, cannot add.");
            }
            while (queue.size() == capacity) {
                notFull.await();
                if (closed) {
                    throw new RuntimeException("Buffer is closed, cannot add.");
                }
            }
            queue.add(i);
            notEmpty.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public int remove() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                if (closed) {
                    throw new RuntimeException("Buffer is closed and empty.");
                }
                notEmpty.await();
            }
            int value = queue.remove();
            notFull.signal();
            return value;
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        return -1;
    }

    public void close() {
        lock.lock();
        try {
            if (closed) {
                throw new RuntimeException("Buffer already closed.");
            }
            closed = true;
            notEmpty.signalAll();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

public class Task3 {
    public static void main(String[] args) throws InterruptedException {
        Buffer buffer = new Buffer(1000);

        Thread producerThread = new Thread(() -> {
            try {
                for (int i = 1; i <= 1_000_000; i++) {
                    buffer.add(i);
                }
                buffer.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                System.out.println("Producer: " + e.getMessage());
            }
        });

        Thread consumerThread = new Thread(() -> {
            while (true) {
                try {
                    int value = buffer.remove();
                    System.out.println(value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (RuntimeException e) {
                    System.out.println("Consumer: " + e.getMessage());
                    break;
                }
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();

        System.out.println("All done!");
    }
}