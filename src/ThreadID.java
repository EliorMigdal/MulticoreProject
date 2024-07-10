import java.util.concurrent.atomic.AtomicInteger;

public class ThreadID {
    private static AtomicInteger counter = new AtomicInteger(1);

    public static int getID() {
        return counter.getAndIncrement();
    }
}