public class ThreadID {
    private static final ThreadLocal<Integer> threadID = new ThreadLocal<>();
    private static int nextID = 1;

    public static synchronized void assignID() {
        if (threadID.get() == null) {
            threadID.set(nextID++);
        }
    }

    public static int get() {
        return threadID.get();
    }
}