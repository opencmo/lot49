package com.enremmeta.rtb.caches;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The future.
 * 
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public final class KnownFuture<T> implements Future<T> {

    public static final KnownFuture KNOWN_NULL_FUTURE = new KnownFuture(null);
    public static final KnownFuture KNOWN_STALE_FUTURE = new KnownFuture(null).setNote("STALE");

    private final T obj;

    public KnownFuture(T obj) {
        this.obj = obj;
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public final boolean isCancelled() {
        return false;
    }

    @Override
    public final boolean isDone() {
        return true;
    }

    @Override
    public final T get() throws InterruptedException, ExecutionException {
        return obj;
    }

    @Override
    public final T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        return obj;
    }

    private String note = "";

    private KnownFuture<T> setNote(String note) {
        this.note = note;
        return this;
    }

    @Override
    public String toString() {

        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            String stringObj = (String) obj;
            stringObj = stringObj.trim();
            if (stringObj.trim().length() == 0) {
                return "";
            }
        }
        return "Future[" + this.obj + "]";
    }

}
