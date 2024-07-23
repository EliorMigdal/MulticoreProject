import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final int TESTS = 10000;

    public static void main(String[] args) {
        //OriginalLock lock = new OriginalLock();
        RevisedLock lock = new RevisedLock();
        List<Integer> AIndices = new ArrayList<>();
        List<Integer> BIndices = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            AIndices.add(i);
            BIndices.add(i);
        }

        while (AIndices.equals(BIndices)) {
            Collections.shuffle(AIndices);
            Collections.shuffle(BIndices);
        }

        Runnable task = () -> {
            ThreadID.assignID();
            int count = 0;
            double avg = 0D;

            while (count < TESTS) {
                long startTime = System.currentTimeMillis();

                if (ThreadID.get() % 2 == 0) {
                    lock.lock(AIndices);
                } else {
                    lock.lock(BIndices);
                }

                long lockAcquiredTime = System.currentTimeMillis();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    if (ThreadID.get() % 2 == 0) {
                        lock.unlock(AIndices);
                    } else {
                        lock.unlock(BIndices);
                    }

                    long lockReleasedTime = System.currentTimeMillis();
                    System.out.println("Thread " + ThreadID.get() + " lock wait time: " + (lockAcquiredTime - startTime) + " ms");
                    System.out.println("Thread " + ThreadID.get() + " critical section time: " + (lockReleasedTime - lockAcquiredTime) + " ms");
                    count++;

                    avg += (double) (lockAcquiredTime - startTime) / TESTS;
                }
            }

            System.out.println("Thread " + ThreadID.get() + " average waiting time: " + avg + "ms");
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
    }
}