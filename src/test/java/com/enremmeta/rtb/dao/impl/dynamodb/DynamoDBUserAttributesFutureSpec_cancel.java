package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBUserAttributesFutureSpec_cancel {
    private ExecutorService executor;
    private long maxTimeWaitTermination = 10000;

    @Before
    public void setUp() throws Exception {
        executor = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() throws Exception {
        try {
            executor.shutdown();
            executor.awaitTermination(maxTimeWaitTermination, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void returnsExpectedResultsIfTaskIsFinished() throws Exception {
        Callable<GetItemResult> task = () -> { return null; };
        Future<GetItemResult> future = executor.submit(task);
        
        DynamoDBUserAttributesFuture dynamoDBUserAttributesFuture = new DynamoDBUserAttributesFuture(future);
        
        future.get(); /// wait for finishing of task
        
        boolean cancelResult = dynamoDBUserAttributesFuture.cancel(true); /// act
        
        assertThat(cancelResult, is(false));
        assertThat(dynamoDBUserAttributesFuture.isCancelled(), is(false));
        assertThat(dynamoDBUserAttributesFuture.isDone(), is(true));
    }

    @Test
    public void returnsExpectedResultsIfTaskIsRunning() throws Exception {
        long taskTimeout = 10000;
        
        Callable<GetItemResult> task = () -> {
            TimeUnit.MILLISECONDS.sleep(taskTimeout);
            return null;
        };
        Future<GetItemResult> future = executor.submit(task);
        
        DynamoDBUserAttributesFuture dynamoDBUserAttributesFuture = new DynamoDBUserAttributesFuture(future);
        
        boolean cancelResult = dynamoDBUserAttributesFuture.cancel(true); /// act
        
        assertThat(cancelResult, is(true));
        assertThat(dynamoDBUserAttributesFuture.isCancelled(), is(true));
        assertThat(dynamoDBUserAttributesFuture.isDone(), is(true));
    }
}
