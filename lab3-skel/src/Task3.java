import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Task3<T extends Comparable<T>> implements LockFreeSet<T> {
    /* Number of levels */
    private static final int MAX_LEVEL = 16;

    private final Log.Entry[] logArr = new Log.Entry[800000];
    private int logIndex = 0;
    private final ReentrantLock tsLock = new ReentrantLock();

    private final Node<T> head = new Node<T>();
    private final Node<T> tail = new Node<T>();

    public Task3() {
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference<Task3.Node<T>>(tail, false);
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

        tsLock.lock();
        int index = logIndex;
        try {
            logIndex++;
            record(index, Log.Method.ADD, x);
        } finally {
            tsLock.unlock();
        }
        while (true) {
            boolean found = find(x, preds, succs, index);
            if (found) {
                recordRet(index, false);
                return false;
            } else {
                Node<T> newNode = new Node(x, topLevel);
                for (int level = bottomLevel; level <= topLevel; level++) {
                    Node<T> succ = succs[level];
                    newNode.next[level].set(succ, false);
                }
                Node<T> pred = preds[bottomLevel];
                Node<T> succ = succs[bottomLevel];

                tsLock.lock();
                boolean ret = pred.next[bottomLevel].compareAndSet(succ, newNode, false, false);
                try {
                    recordTimeStamp(index);
                } finally {
                    tsLock.unlock();
                }

                if (!ret) {
                    continue;
                }
                for (int level = bottomLevel + 1; level <= topLevel; level++) {
                    while (true) {
                        pred = preds[level];
                        succ = succs[level];
                        if (pred.next[level].compareAndSet(succ, newNode, false, false))
                            break;
                        find(x, preds, succs, -1);
                    }
                }
                recordRet(index, true);
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

        tsLock.lock();
        int index = logIndex;
        try {
            logIndex++;
            record(index, Log.Method.REMOVE, x);
        } finally {
            tsLock.unlock();
        }

        while (true) {
            boolean found = find(x, preds, succs, index);
            if (!found) {
                recordRet(index, false);
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
                    tsLock.lock();
                    boolean iMarkedIt;
                    try {
                        iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                        recordTimeStamp(index);
                    } finally {
                        tsLock.unlock();
                    }
                    succ = succs[bottomLevel].next[bottomLevel].get(marked);
                    if (iMarkedIt) {
                        find(x, preds, succs, -1);
                        recordRet(index, true);
                        return true;
                    } else if (marked[0]) {
                        recordRet(index, false);
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

        tsLock.lock();
        int index = logIndex;
        try {
            logIndex++;
            record(index, Log.Method.CONTAINS, x);
        } finally {
            tsLock.unlock();
        }

        for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
            tsLock.lock();
            try {
                curr = pred.next[level].getReference();
                recordTimeStamp(index);
            } finally {
                tsLock.unlock();
            }
            while (true) {
                succ = curr.next[level].get(marked);
                while (marked[0]) {
                    tsLock.lock();
                    try {
                        curr = succ;
                        recordTimeStamp(index);
                    } finally {
                        tsLock.unlock();
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
        }

        boolean ret = curr.value != null && x.compareTo(curr.value) == 0;
        recordRet(index, ret);
        return ret;
    }

    private boolean find(T x, Node<T>[] preds, Node<T>[] succs, int index) {
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
                if (level == bottomLevel && index >= 0) {
                    tsLock.lock();
                    try {
                        curr = pred.next[level].getReference();
                        recordTimeStamp(index);
                    } finally {
                        tsLock.unlock();
                    }
                } else {
                    curr = pred.next[level].getReference();
                }
                while (true) {
                    succ = curr.next[level].get(marked);
                    while (marked[0]) {
                        snip = pred.next[level].compareAndSet(curr, succ, false, false);
                        if (!snip) continue retry;
                        if (level == bottomLevel && index >= 0) {
                            tsLock.lock();
                            try {
                                curr = pred.next[level].getReference();
                                recordTimeStamp(index);
                            } finally {
                                tsLock.unlock();
                            }
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
            boolean ret= curr.value != null && x.compareTo(curr.value) == 0;;
            if (index >= 0) {
                recordRet(index, ret);
            }
            return ret;
        }
    }

    private void record(int index, Log.Method method, T x) {
        tsLock.lock();
        try {
            long ts = System.nanoTime();
            Log.Entry entry = logArr[index];
            if (entry != null) {
                entry.timestamp = ts;
            } else {
                int arg = (x == null ? 0 : x.hashCode());
                logArr[index] = new Log.Entry(method, arg, false, ts);
            }
        } finally {
            tsLock.unlock();
        }
    }

    private void recordTimeStamp(int index) {
        tsLock.lock();
        try {
            long ts = System.nanoTime();
            Log.Entry entry = logArr[index];
            if (entry != null) {
                entry.timestamp = ts;
            }
        } finally {
            tsLock.unlock();
        }
    }

    private void recordRet(int index, boolean ret) {
        tsLock.lock();
        try {
            Log.Entry entry = logArr[index];
            if (entry != null) {
                entry.ret = ret;
            }
        } finally {
            tsLock.unlock();
        }
    }


    public Log.Entry[] getLog() {
        Log.Entry[] result = new Log.Entry[logIndex];
        System.arraycopy(logArr, 0, result, 0, logIndex);
        return result;
    }

    public void reset() {
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference<Task3.Node<T>>(tail, false);
        }
        for (int i = 0; i < logIndex; i++) {
            logArr[i] = null;
        }
        logIndex = 0;
    }
}

