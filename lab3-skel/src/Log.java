import java.util.HashSet;
import java.util.Arrays;

public class Log {
    private Log() {
        // Do not implement
    }

    public static int validate(Log.Entry[] log) {
        // Implement this.
        // Should return the number of discrepancies in the log.
        Arrays.sort(log, (e1, e2) -> Long.compare(e1.timestamp, e2.timestamp));
        HashSet<Integer> set = new HashSet<>();
        int discrepancy = 0;
        for (Entry entry : log) {
            int arg = entry.arg;
            boolean ret = entry.ret;
            boolean set_ret = switch (entry.method) {
                case ADD -> set.add(arg);
                case REMOVE -> set.remove(arg);
                case CONTAINS -> set.contains(arg);
            };
            discrepancy += set_ret == ret ? 0 : 1;
        }
        return discrepancy;
    }

    // Log entry for linearization point.
    public static class Entry {
        public Method method;
        public int arg;
        public boolean ret;
        public long timestamp;
        public Entry(Method method, int arg, boolean ret, long timestamp) {
            this.method = method;
            this.arg = arg;
            this.ret = ret;
            this.timestamp = timestamp;
        }
    }

    public static enum Method {
        ADD, REMOVE, CONTAINS
    }
}
