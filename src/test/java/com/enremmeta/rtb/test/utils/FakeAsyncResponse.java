package com.enremmeta.rtb.test.utils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;

/**
 * For testing.
 *
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class FakeAsyncResponse implements AsyncResponse {

    private Object payload;
    private boolean done = false;

    public Object getPayload() {
        return payload;
    }

    public FakeAsyncResponse() {
        super();
    }

    @Override
    public boolean resume(Object response) {
        this.payload = response;
        this.done = true;
        return true;
    }

    @Override
    public boolean resume(Throwable response) {
        this.payload = response;
        this.done = true;
        return true;
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public boolean cancel(int retryAfter) {
        return true;
    }

    @Override
    public boolean cancel(Date retryAfter) {
        return true;
    }

    @Override
    public boolean isSuspended() {

        return false;
    }

    @Override
    public boolean isCancelled() {

        return false;
    }

    @Override
    public boolean isDone() {

        return done;
    }

    @Override
    public boolean setTimeout(long time, TimeUnit unit) {
        return false;
    }

    @Override
    public void setTimeoutHandler(TimeoutHandler handler) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<Class<?>> register(Class<?> callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Class<?> callback, Class<?>... callbacks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Class<?>> register(Object callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Class<?>, Collection<Class<?>>> register(Object callback, Object... callbacks) {
        throw new UnsupportedOperationException();
    }

}
