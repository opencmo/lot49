package com.enremmeta.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.enremmeta.rtb.LogUtils;

public class FutureWaiter implements Runnable {
    private String desc;

    public FutureWaiter(Future f, String desc) {
        this.future = f;
        this.desc = desc;
    }

    private final Future future;

    @Override
    public void run() {
        if (future.isDone()) {
            try {
                LogUtils.trace("Future " + future + " ( " + desc + ") is done: " + future.get());
            } catch (InterruptedException | ExecutionException e) {
                LogUtils.trace("Future " + future + " ( " + desc + ") is errored", e);
            }
        }
    }
}
