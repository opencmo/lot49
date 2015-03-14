package com.enremmeta.rtb.dao;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.Utils;

/**
 * A {@link Future} that, when finished, will need to parse the result of JSON into the
 * <code>T</code> object.
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public class JsonFuture<T> implements Future<T> {

    protected Future<String> getUnderlyingFuture() {
        return f;
    }

    protected Class<T> getType() {
        return type;
    }

    public JsonFuture(Future<String> f, Class<T> type) {
        super();
        this.f = f;
        this.type = type;
    }

    private final Class<T> type;

    private final Future<String> f;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return f.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return f.isCancelled();
    }

    @Override
    public boolean isDone() {
        return f.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        String s = f.get();
        T retval;
        try {
            LogUtils.debug("Dao: " + getClass().getName() + ":  get(): " + s);
            if (s == null) {
                return null;
            }
            retval = Utils.MAPPER.readValue(s, type);
            LogUtils.debug("Dao: " + getClass().getName() + ":  " + s + " -> " + retval);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        return retval;
    }

    @Override
    public T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        String s = f.get(timeout, unit);
        LogUtils.debug("Dao: " + getClass().getName() + ":  get(" + timeout + "," + unit + "): "
                        + s);
        T retval;
        try {
            retval = Utils.MAPPER.readValue(s, type);
            LogUtils.debug("Dao: " + getClass().getName() + ":  " + s + " -> " + retval);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        return retval;
    }
}
