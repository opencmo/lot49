package com.enremmeta.rtb.magic;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.enremmeta.rtb.LogUtils;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandOutput;

public class DelegatingCommand<K, V, T> extends Command<K, V, T> {
    private Command<K, V, T> delegate;

    public DelegatingCommand(Command<K, V, T> d) {
        super(null, null, null, false);
        this.delegate = d;
    }

    public boolean cancel(boolean ignored) {
        return delegate.cancel(ignored);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public T get() {
        try {
            Class klass = delegate.getClass();

            Field latchField = klass.getDeclaredField("latch");
            latchField.setAccessible(true);
            Object latchValue = latchField.get(delegate);

            LogUtils.debug("DelegatingCommand: In " + delegate + ": latch value is " + latchValue);
            CountDownLatch cdl = (CountDownLatch) latchValue;
            cdl.await();

            CommandOutput co = delegate.getOutput();
            LogUtils.debug("DelegatingCommand: In " + delegate + ": CommandOutput: " + co);

            Class coKlass = co.getClass();

            Field outputField = CommandOutput.class.getDeclaredField("output");
            outputField.setAccessible(true);
            Object outputValue = outputField.get(co);
            LogUtils.debug("DelegatingCommand: In " + delegate + ": CommandOutput.output="
                            + outputValue);

            Object o = co.get();
            LogUtils.debug("DelegatingCommand: In " + delegate + ": o=" + o);
            return (T) o;
        } catch (Throwable t) {
            LogUtils.error(t);
            return null;
        }
    }

    public T get(long timeout, TimeUnit unit) throws TimeoutException {
        return delegate.get(timeout, unit);
    }

    public boolean await(long timeout, TimeUnit unit) {
        return delegate.await(timeout, unit);
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public CommandOutput<K, V, T> getOutput() {
        return delegate.getOutput();
    }

    public void complete() {
        delegate.complete();
    }

    public String toString() {
        return delegate.toString();
    }
}
