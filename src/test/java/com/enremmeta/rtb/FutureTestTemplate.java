package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FutureTestTemplate<T, I> {
    private ExecutorService executor;
    private long maxTimeWaitTermination = 10000;
    private long taskTimeout = 10000;
    
    private Future<T> testFuture;
    protected Future<T> getTestFuture() { return testFuture; };

    protected void set_up() {
        executor = Executors.newSingleThreadExecutor();
    }

    protected void tear_down() throws InterruptedException {
        try {
            executor.shutdown();
            executor.awaitTermination(maxTimeWaitTermination, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
    
    protected abstract Future<T> createTestFuture(Future<I> innerFuture);

    protected void test_cancel_ifTaskIsFinished() throws InterruptedException, ExecutionException {
        Future<I> innerFuture = executor.submit(() -> { return null; });
        testFuture = createTestFuture(innerFuture);
        
        innerFuture.get(); /// wait for finishing of task
        
        boolean cancelResult = testFuture.cancel(true); /// act
        
        assertThat(cancelResult, is(false));
        assertThat(testFuture.isCancelled(), is(false));
        assertThat(testFuture.isDone(), is(true));
    }

    protected void test_cancel_ifTaskIsRunning() {
        Future<I> innerFuture = executor.submit(() -> {
            TimeUnit.MILLISECONDS.sleep(taskTimeout);
            return null;
        });
        
        testFuture = createTestFuture(innerFuture);
        
        boolean cancelResult = testFuture.cancel(true); /// act
        
        assertThat(cancelResult, is(true));
        assertThat(testFuture.isCancelled(), is(true));
        assertThat(testFuture.isDone(), is(true));
    }

    protected T test_get(I innerFutureGetResult) throws InterruptedException, ExecutionException {
        Future<I> innerFuture = executor.submit(() -> { return innerFutureGetResult; });
        testFuture = createTestFuture(innerFuture);
        
        T result = testFuture.get(); /// act
        
        return result;
    }

    protected T test_getWithTimeout(I innerFutureGetResult) throws InterruptedException, ExecutionException, TimeoutException {
        long maxWaitTime = 10000;
        
        Future<I> innerFuture = executor.submit(() -> { return innerFutureGetResult; });
        testFuture = createTestFuture(innerFuture);
        
        T result = testFuture.get(maxWaitTime, TimeUnit.MILLISECONDS); /// act
        
        return result;
    }

    protected void test_getWithTimeout_ifWaitTimeElapsed(I innerFutureGetResult) throws InterruptedException, ExecutionException, TimeoutException {
        long maxWaitTime = 1;
        
        Future<I> innerFuture = executor.submit(() -> {
            TimeUnit.MILLISECONDS.sleep(taskTimeout);
            return innerFutureGetResult;
        });
        
        testFuture = createTestFuture(innerFuture);
        
        try {
            testFuture.get(maxWaitTime, TimeUnit.MILLISECONDS); /// act
        } catch (TimeoutException ex) {
            testFuture.cancel(true);
            throw ex;
        }
    }
}
