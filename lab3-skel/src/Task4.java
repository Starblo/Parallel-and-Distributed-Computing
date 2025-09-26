import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class Task4<T extends Comparable<T>> implements LockFreeSet<T> {
    /* Number of levels */
    private static final int MAX_LEVEL = 16;
    private static final int MAX_THREADS = 48;

    private final ReentrantLock tsLock = new ReentrantLock();
    HashMap<Integer, LinkedList<Log.Entry>> map = new HashMap<Integer, LinkedList<Log.Entry>>();

    private final Node<T> head = new Node<T>();
    private final Node<T> tail = new Node<T>();

    public Task4() {
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference<Task4.Node<T>>(tail, false);
        }
        for (int i = 0; i < MAX_THREADS; i++){
            map.put(i, new LinkedList<>());
        }
    }

    private static final class Node<T> {
        private final T value;
        private final AtomicMarkableReference<Node<T>>[] next;
        private final int topLevel;

        @SuppressWarnings("unchecked")
        public Node() {
            value = null;
            next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[MAX_LEVEL + 1];
            for (int i = 0; i < next.length; i++) {
                next[i] = new AtomicMarkableReference<Node<T>>(null, false);
            }
            topLevel = MAX_LEVEL;
        }

        @SuppressWarnings("unchecked")
        public Node(T x, int height) {
            value = x;
            next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[height + 1];
            for (int i = 0; i < next.length; i++) {
                next[i] = new AtomicMarkableReference<Node<T>>(null, false);
            }
            topLevel = height;
        }
    }

    /* Returns a level between 0 to MAX_LEVEL,
     * P[randomLevel() = x] = 1/2^(x+1), for x < MAX_LEVEL.
     */
    private static int randomLevel() {
        int r = ThreadLocalRandom.current().nextInt();
        int level = 0;
        r &= (1 << MAX_LEVEL) - 1;
        while ((r & 1) != 0) {
            r >>>= 1;
            level++;
        }
        return level;
    }

    @SuppressWarnings("unchecked")
    public boolean add(int threadId, T x) {
        int topLevel = randomLevel();
        int bottomLevel = 0;
        Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];

        Log.Entry entry = record(Log.Method.ADD, x);

        while (true) {
            boolean found = find(x, preds, succs, entry);
            if (found) {
                recordRetAndEnqueue(entry, threadId, false);
                return false;
            } else {
                Node<T> newNode = new Node(x, topLevel);
                for (int level = bottomLevel; level <= topLevel; level++) {
                    Node<T> succ = succs[level];
                    newNode.next[level].set(succ, false);
                }
                Node<T> pred = preds[bottomLevel];
                Node<T> succ = succs[bottomLevel];

                boolean ret = pred.next[bottomLevel].compareAndSet(succ, newNode, false, false);
                recordTimeStamp(entry);

                if (!ret) {
                    continue;
                }
                for (int level = bottomLevel + 1; level <= topLevel; level++) {
                    while (true) {
                        pred = preds[level];
                        succ = succs[level];
                        if (pred.next[level].compareAndSet(succ, newNode, false, false))
                            break;
                        find(x, preds, succs, null);
                    }
                }
                recordRetAndEnqueue(entry, threadId, true);
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean remove(int threadId, T x) {
        int bottomLevel = 0;
        Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T> succ;

        Log.Entry entry = record(Log.Method.REMOVE, x);

        while (true) {
            boolean found = find(x, preds, succs, entry);
            if (!found) {
                recordRetAndEnqueue(entry, threadId, false);
                return false;
            } else {
                Node<T> nodeToRemove = succs[bottomLevel];
                for (int level = nodeToRemove.topLevel; level >= bottomLevel + 1; level--) {
                    boolean[] marked = {false};
                    succ = nodeToRemove.next[level].get(marked);
                    while (!marked[0]) {
                        nodeToRemove.next[level].compareAndSet(succ, succ, false, true);
                        succ = nodeToRemove.next[level].get(marked);
                    }
                }
                boolean[] marked = {false};
                succ = nodeToRemove.next[bottomLevel].get(marked);
                while (true) {
                    boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                    recordTimeStamp(entry);
                    succ = succs[bottomLevel].next[bottomLevel].get(marked);
                    if (iMarkedIt) {
                        find(x, preds, succs, null);
                        recordRetAndEnqueue(entry, threadId, true);
                        return true;
                    } else if (marked[0]) {
                        recordRetAndEnqueue(entry, threadId, false);
                        return false;
                    }
                }
            }
        }
    }

    public boolean contains(int threadId, T x) {
        int bottomLevel = 0;
        int key = x.hashCode();
        boolean[] marked = {false};
        Node<T> pred = head;
        Node<T> curr = null;
        Node<T> succ = null;

        Log.Entry entry = record(Log.Method.CONTAINS, x);

        for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
            curr = pred.next[level].getReference();
            recordTimeStamp(entry);
            while (true) {
                succ = curr.next[level].get(marked);
                while (marked[0]) {
                    curr = succ;
                    recordTimeStamp(entry);
                    succ = curr.next[level].get(marked);
                }
                if (curr.value != null && x.compareTo(curr.value) < 0) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
        }

        boolean ret = curr.value != null && x.compareTo(curr.value) == 0;
        recordRetAndEnqueue(entry, threadId, ret);
        return ret;
    }

    private boolean find(T x, Node<T>[] preds, Node<T>[] succs, Log.Entry entry) {
        int bottomLevel = 0;
        boolean[] marked = {false};
        boolean snip;
        Node<T> pred = null;
        Node<T> curr = null;
        Node<T> succ = null;
        retry:
        while (true) {
            pred = head;
            for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
                if (level == bottomLevel) {
                    curr = pred.next[level].getReference();
                    recordTimeStamp(entry);
                } else {
                    curr = pred.next[level].getReference();
                }
                while (true) {
                    succ = curr.next[level].get(marked);
                    while (marked[0]) {
                        snip = pred.next[level].compareAndSet(curr, succ, false, false);
                        if (!snip) continue retry;
                        if (level == bottomLevel) {
                            curr = pred.next[level].getReference();
                            recordTimeStamp(entry);
                        } else {
                            curr = pred.next[level].getReference();
                        }
                        succ = curr.next[level].get(marked);
                    }
                    if (curr.value != null && x.compareTo(curr.value) < 0) {
                        pred = curr;
                        curr = succ;
                    } else {
                        break;
                    }
                }

                preds[level] = pred;
                succs[level] = curr;
            }
            return curr.value != null && x.compareTo(curr.value) == 0;
        }
    }

    private Log.Entry record(Log.Method method, T x) {
        int arg = (x == null ? 0 : x.hashCode());
        return new Log.Entry(method, arg, false, 0);
    }

    private void recordTimeStamp(Log.Entry entry) {
        if (entry != null) {
            entry.timestamp = System.nanoTime();
        }
    }

    private void recordRetAndEnqueue(Log.Entry entry, int threadId, boolean ret) {
        if (entry != null) {
            entry.ret = ret;
            map.get(threadId).add(entry);
        }
    }


    public Log.Entry[] getLog() {
        ArrayList<Log.Entry> entries = new ArrayList<>();
        map.values().forEach(entries::addAll);
        return entries.toArray(new Log.Entry[0]);
    }

    public void reset() {
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference<Task4.Node<T>>(tail, false);
        }
        for (int i = 0; i < MAX_THREADS; i++) {
            map.get(i).clear();
        }
    }
}

