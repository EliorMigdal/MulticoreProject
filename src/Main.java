import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final int TESTS = 1000;

    public static void main(String[] args) {
        OriginalLock lock = new OriginalLock();
        List<Integer> AIndices = new ArrayList<>();
        List<Integer> BIndices = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            AIndices.add(i);
            BIndices.add(i);
        }

        while (AIndices.equals(BIndices)) {
            Collections.shuffle(AIndices);
            Collections.shuffle(BIndices);
        }

        Runnable task = () -> {
            long startTime = System.currentTimeMillis();
            ThreadID.assignID();

            while(true) {
                if (ThreadID.get() % 2 == 0)
                    lock.lock(AIndices);

                else
                    lock.lock(BIndices);

                long lockAcquiredTime = System.currentTimeMillis();
                try {
                    System.out.println("Thread " + ThreadID.get() + " is in the critical section");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (ThreadID.get() % 2 == 0)
                        lock.unlock(AIndices);

                    else
                        lock.unlock(BIndices);

                    long lockReleasedTime = System.currentTimeMillis();
                    System.out.println("Thread " + ThreadID.get() + " has left the critical section");
                    System.out.println("Thread " + ThreadID.get() + " lock wait time: " + (lockAcquiredTime - startTime) + " ms");
                    System.out.println("Thread " + ThreadID.get() + " critical section time: " + (lockReleasedTime - lockAcquiredTime) + " ms");
                }
            }
        };

        new Thread(task).start();
        new Thread(task).start();
    }
}