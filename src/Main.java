import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final int TESTS = 20000000;

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Initial memory usage: " + initialMemory + " bytes");

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
            int count = 0;

            while (count < TESTS) {
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
                    count++;
                }
            }
        };

        Thread threadA = new Thread(task);
        Thread threadB = new Thread(task);

        try {
            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Final memory usage: " + finalMemory + " bytes");
        System.out.println("Memory used by task: " + (finalMemory - initialMemory) + " bytes");
    }
}
