import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RevisedLock {
    final int M = 3;
    private final int WAITING = -1;
    private final int FREE = 0;
    volatile List<AtomicInteger> registers = new ArrayList<>(M);

    public RevisedLock(){
        for(int i = 0; i < M; i++)
            registers.add(new AtomicInteger(FREE));
    }

    private int countOwnedRegisters() {
        int ownedRegisters = 0;

        for (AtomicInteger register : registers) {
            if (register.compareAndSet(ThreadID.get(), register.get())) {
                ownedRegisters++;
            }
        }

        return ownedRegisters;
    }

    private void prioritizeWaitingProcess(List<Integer> indices) {
        int myCounter = 0;

        while (myCounter < M && !tryToOwnRegister(indices.get(myCounter)))
            myCounter++;

        if (isOtherProcessWaiting(indices)) {
            if (myCounter < M)
                releaseRegister(indices.get(myCounter));

            while(isOtherProcessWaiting(indices));
        }
    }

    private boolean isOtherProcessWaiting(List<Integer> indices) {
        boolean isWaiting = false;

        for (int i = 0; i < M && !isWaiting; i++)
            if (isSignaledWaiting(indices.get(i)))
                isWaiting = true;

        return isWaiting;
    }

    private boolean canIEnterCS(boolean myGo) {
        return myGo || countOwnedRegisters() == 2;
    }

    private void tryOwningEnoughRegisters(List<Integer> indices) {
        int owned = countOwnedRegisters();
        for (int i = 0; i < M; i++) {
            if (owned < 2) {
                if (tryToOwnRegister(indices.get(i))) {
                    owned++;
                }
            }

            else break;
        }
    }

    private boolean isSignaledWaiting(int index) {
        return registers.get(index).get() == WAITING;
    }

    private void releaseRegister(int index) {
        registers.get(index).compareAndSet(ThreadID.get(), FREE);
    }

    private void releaseWaiting(int index) {
        registers.get(index).compareAndSet(WAITING, FREE);
    }

    private boolean isRegisterFree(int index) {
        return registers.get(index).get() == FREE;
    }

    private boolean tryToOwnRegister(int index) {
        return registers.get(index).compareAndSet(FREE, ThreadID.get());
    }

    private boolean didILose() {
        return countOwnedRegisters() < 2;
    }

    private boolean tryToSignalWaiting(int index) {
        return registers.get(index).compareAndSet(ThreadID.get(), WAITING);
    }

    private void handleLoss(List<Integer> indices) {
        setMyselfWaiting(indices);
        waitForCSToBeReleased(indices);
    }

    private void setMyselfWaiting(List<Integer> indices) {

        for (int i = 0; i < M; i++) {
            if(tryToSignalWaiting(indices.get(i)))
                break;
        }
    }

    private void waitForCSToBeReleased(List<Integer> indices) {
        boolean wasCSReleased = false;
        boolean foundOwnedRegister;

        while (!wasCSReleased) {
            foundOwnedRegister = false;

            for (int i = 0; i < M && !foundOwnedRegister; i++)
                if (!isRegisterFree(indices.get(i)) && !isSignaledWaiting(indices.get(i)))
                    foundOwnedRegister = true;

            if (!foundOwnedRegister)
                wasCSReleased = true;
        }
    }

    public void lock(List<Integer> indices) {
        ThreadID.assignID();
        boolean myGo = false;
        prioritizeWaitingProcess(indices);

        while (!canIEnterCS(myGo)) {
            tryOwningEnoughRegisters(indices);

            System.out.println("Thread " + ThreadID.get() + " owns " + countOwnedRegisters() + " registers.");
            System.out.println("Registers state: [" + registers.get(indices.get(0)) + ", "
                    + registers.get(indices.get(1)) + ", "
                    + registers.get(indices.get(2)) + "]");

            if (didILose())
                handleLoss(indices);

            myGo = true;
        }
    }

    public void unlock(List<Integer> indices) {
        if (countOwnedRegisters() < 2) {
            for (int i = 0; i < M; i++)
                releaseWaiting(indices.get(i));
        } else {
            for (int j = 0; j < M; j++)
                releaseRegister(indices.get(j));
        }
    }
}
