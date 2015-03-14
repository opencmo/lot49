package com.enremmeta.rtb.caches;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

public class DynamoDBFuture<T> implements Future<T> {

    private final Future<GetItemResult> dynFuture;

    private final DynamoDBDecoder<T> decoder;

    public DynamoDBFuture(Future<GetItemResult> dynFuture, DynamoDBDecoder<T> decoder) {
        super();
        this.dynFuture = dynFuture;
        this.decoder = decoder;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return dynFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return dynFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return dynFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        final GetItemResult res = dynFuture.get();
        final Map<String, AttributeValue> map = res.getItem();
        final T result = decoder.decode(map);
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        GetItemResult res = dynFuture.get(timeout, unit);
        final Map<String, AttributeValue> map = res.getItem();
        final T result = decoder.decode(map);
        return result;
    }
}
