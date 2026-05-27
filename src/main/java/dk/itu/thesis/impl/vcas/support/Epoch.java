package dk.itu.thesis.impl.vcas.support;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Epoch<Node extends Reclaimable> {
    public static final int ANNOUNCES_BEFORE_COLLECT = 512;
    public static final int PADDING = 64;
    public static final int ACTIVATING = -1;
    public static final int INACTIVE = -2;
    public static final int MAX_THREADS = ThreadID.MAX_THREADS;

    public final int[] announce;
    public final int[] prevRetireEpoch;
    public final int[] announceCount;
    public final ArrayList<Node>[][] retiredNodes;

    public volatile int epochNum;

    private static final AtomicInteger nextThreadId = new AtomicInteger(0);

    private static final AtomicIntegerFieldUpdater<Epoch> epochUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Epoch.class, "epochNum");

    private int threadIndex() {
        Integer tid = ThreadID.threadID.get();

        if (tid == null) {
            tid = nextThreadId.getAndIncrement();

            if (tid >= MAX_THREADS) {
                throw new IllegalStateException("Too many threads for Epoch: " + tid);
            }

            ThreadID.threadID.set(tid);
        }

        return tid * PADDING;
    }

    public Epoch() {
        epochNum = 0;
        announce = new int[MAX_THREADS * PADDING];
        prevRetireEpoch = new int[MAX_THREADS * PADDING];
        announceCount = new int[MAX_THREADS * PADDING];
        retiredNodes = new ArrayList[3][MAX_THREADS * PADDING];

        for (int i = 0; i < MAX_THREADS; i++) {
            announce[i * PADDING] = INACTIVE;
        }
    }

    public void announce() {
        int idx = threadIndex();
        int curEpoch = epochNum;
        announce[idx] = curEpoch;
        tryAdvanceEpoch(curEpoch);
    }

    public void tryAdvanceEpoch(int curEpoch) {
        int idx = threadIndex();
        int annCount = announceCount[idx];

        if (annCount == ANNOUNCES_BEFORE_COLLECT) {
            announceCount[idx] = 0;

            for (int i = 0; i < MAX_THREADS; i++) {
                int ann = announce[i * PADDING];
                if (ann != INACTIVE && ann != curEpoch) {
                    return;
                }
            }

            epochUpdater.compareAndSet(this, curEpoch, curEpoch + 1);
        } else {
            announceCount[idx] = annCount + 1;
        }
    }

    public void unannounce() {
        int idx = threadIndex();
        announce[idx] = INACTIVE;
    }

    public void retire(Node node) {
        int idx = threadIndex();
        int curEpoch = epochNum;

        int prevEpoch = prevRetireEpoch[idx];

        for (int i = prevEpoch - 1; i <= curEpoch - 2 && i <= prevEpoch; i++) {
            int rindex = (i + 3) % 3;

            if (retiredNodes[rindex][idx] != null) {
                for (int j = 0; j < retiredNodes[rindex][idx].size(); j++) {
                    retiredNodes[rindex][idx].get(j).reclaim();
                }

                retiredNodes[rindex][idx].clear();
            }
        }

        prevRetireEpoch[idx] = curEpoch;

        int bagIdx = curEpoch % 3;

        if (retiredNodes[bagIdx][idx] == null) {
            retiredNodes[bagIdx][idx] = new ArrayList<>(128);
        }

        retiredNodes[bagIdx][idx].add(node);

        tryAdvanceEpoch(curEpoch);
    }
}