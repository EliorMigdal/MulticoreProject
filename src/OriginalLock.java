import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OriginalLock implements Lock {
    private final int M = 7;
    private final int WAITING = -1;
    volatile Integer[] myView = new Integer[M];
    volatile List<Integer> registers = Arrays.asList(myView);
    private int myCounter = 0;
    private boolean myGo = false;

    public OriginalLock() {
        for (Integer i : myView)
            i = 0;
    }

    @Override
    public void lock() {
        Collections.shuffle(registers);
        int ownedRegisters = 0;

        for (Integer register : registers) {
            if (register == 1) { //TODO: assume thread id is 1, FIX LATER.
                ownedRegisters++;
            }
        }

        while (!myGo || ownedRegisters < M - 2) {
            Integer entry = registers.get(0);

            while (entry != 0) {
                myCounter++;
                entry = registers.get(myCounter);
            }

            entry = 1; //TODO: Assume thread id is 1, FIX LATER.

            for (Integer item : registers) {
                if (item == WAITING) { //TODO: assume 'waiting' is -1, EXAMINE LATER.
                    entry = 0;
                    break;
                }
            }

            if (entry == 0) {
                boolean isOtherThreadWaiting = true;

                while (isOtherThreadWaiting) {
                    for (Integer listitem : registers) {
                        if (listitem == -1)
                            break;

                        isOtherThreadWaiting = false;
                    }
                }
            }

            for (Integer item : registers) {
                if (item == 0) {
                    int owned = 0;

                    for (Integer listitem : registers) {
                        if (listitem == 1) { //TODO: assume thread id is 1, FIX LATER.
                            owned++;
                        }
                    }

                    if (owned < M - 2) {
                        item = 1; //TODO: assume thread id is 1, FIX LATER.
                    }
                }
            }

            ownedRegisters = 0;

            for (Integer register : registers) {
                if (register == 1) { //TODO: assume thread id is 1, FIX LATER.
                    ownedRegisters++;
                }
            }

            if (ownedRegisters < M / 2) { //TODO: handle upper value of m/2.
                myCounter = 0;

                for (Integer register : registers) {
                    if (register == 1 && myCounter == 2) {
                        register = 0;
                    } else {
                        register = WAITING; //TODO: assume 'waiting' is -1, EXAMINE LATER.
                        myCounter++;
                    }
                }
            }

            boolean isCSFree = false;

            while (!isCSFree) {
                for (Integer register : registers) {
                    if (register != 0 && register != WAITING)
                        break;
                }

                myGo = true;
                isCSFree = true;
            }
        }
    }

    @Override
    public void unlock() {
        if (myGo) {
            for (Integer register : registers) {
                if (register == WAITING || register == 1) { //TODO: assume thread id is 1, and waiting is -1. FIX LATER.
                    register = 0;
                }
            }

            myGo = false;
            myCounter = 0;
        }
    }
}