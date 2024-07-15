import java.util.Arrays;
import java.util.List;

public class OriginalLock implements Lock {
    private final int M = 7;
    private final int WAITING = -1;
    private final int FREE = 0;
    volatile Integer[] myView = new Integer[M];
    volatile List<Integer> registers = Arrays.asList(myView);

    public OriginalLock() {
        initialize();
    }

    private void initialize() {
        Arrays.fill(myView, FREE);
    }

    private int countOwnedRegisters() {
        int ownedRegisters = 0;

        for (Integer register : registers) {
            if (register == ThreadID.get()) {
                ownedRegisters++;
            }
        }

        return ownedRegisters;
    }

    private void prioritizeWaitingProcess(List<Integer> indices) {
        int myCounter = 0;

        while (myCounter < M && !isRegisterFree(indices.get(myCounter)))
            myCounter++;

        if (myCounter < M)
            ownRegister(indices.get(myCounter), ThreadID.get());

        if (isOtherProcessWaiting(indices)) {
            if (myCounter < M && doesProcessOwnRegister(indices.get(myCounter), ThreadID.get()))
                releaseRegister(myCounter);

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
        return myGo || countOwnedRegisters() == M - 2;
    }

    private void tryOwningEnoughRegisters(List<Integer> indices) {
        for (int i = 0; i < M; i++) {
            if (isRegisterFree(indices.get(i)) && countOwnedRegisters() < M - 2)
                ownRegister(indices.get(i), ThreadID.get());

            else if (countOwnedRegisters() == M - 2)
                break;
        }
    }

    private boolean isSignaledWaiting(int index) {
        return registers.get(index) == WAITING;
    }

    private boolean doesProcessOwnRegister(int index, int processID) {
        return registers.get(index) == processID;
    }

    private void releaseRegister(int index) {
        registers.set(index, FREE);
    }

    private boolean isRegisterFree(int index) {
        return registers.get(index) == FREE;
    }

    private void ownRegister(int index, int processID) {
        registers.set(index, processID);
    }

    private boolean didILose() {
        return countOwnedRegisters() < Math.ceil((double) M / 2);
    }

    private void signalWaiting(int index) {
        registers.set(index, WAITING);
    }

    private void handleLoss(List<Integer> indices) {
        keepOnlyTwoRegisters(indices);
        waitForCSToBeReleased(indices);
    }

    private void keepOnlyTwoRegisters(List<Integer> indices) {
        int myCounter = 0;

        for (int i = 0; i < M; i++) {
            if (doesProcessOwnRegister(indices.get(i), ThreadID.get())) {
                if (myCounter == 2)
                    releaseRegister(indices.get(i));

                else {
                    signalWaiting(indices.get(i));
                    myCounter++;
                }
            }
        }
    }

    private void waitForCSToBeReleased(List<Integer> indices) {
        boolean wasCSReleased = false;

        while (!wasCSReleased) {
            for (int i = 0; i < M; i++)
                if (!isRegisterFree(indices.get(i)) && !isSignaledWaiting(indices.get(i)))
                    break;

            wasCSReleased = true;
        }
    }

    @Override
    public void lock(List<Integer> indices) {
        ThreadID.assignID();
        boolean myGo = false;
        prioritizeWaitingProcess(indices);

        while (!canIEnterCS(myGo)) {
            tryOwningEnoughRegisters(indices);

            System.out.println("Thread " + ThreadID.get() + " owns " + countOwnedRegisters() + " registers.");
            System.out.println("Registers state: [" + registers.get(indices.get(0)) + ", "
                    + registers.get(indices.get(1)) + ", "
                    + registers.get(indices.get(2)) + ", "
                    + registers.get(indices.get(3)) + ", "
                    + registers.get(indices.get(4)) + ", "
                    + registers.get(indices.get(5)) + ", "
                    + registers.get(indices.get(6)) + "]");

            if (didILose())
                handleLoss(indices);

            myGo = true;
        }
    }

    @Override
    public void unlock(List<Integer> indices) {
        for (int i = 0; i < M; i++)
            if (isSignaledWaiting(indices.get(i)) || doesProcessOwnRegister(indices.get(i), ThreadID.get()))
                releaseRegister(indices.get(i));
    }
}