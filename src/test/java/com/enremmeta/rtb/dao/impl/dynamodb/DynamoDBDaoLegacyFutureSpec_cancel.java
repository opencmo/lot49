package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore("DynamoDBDaoLegacyFuture was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoLegacyFutureSpec_cancel {
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
        Callable<Map<String, String>> task = () -> { return null; };
        Future<Map<String, String>> future = executor.submit(task);
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        future.get(); /// wait for finishing of task
        
        //boolean cancelResult = dynamoDBDaoLegacyFuture.cancel(true); /// act
        
        //assertThat(cancelResult, is(false));
        //assertThat(dynamoDBDaoLegacyFuture.isCancelled(), is(false));
        //assertThat(dynamoDBDaoLegacyFuture.isDone(), is(true));
    }

    @Test
    public void returnsExpectedResultsIfTaskIsRunning() throws Exception {
        long taskTimeout = 10000;
        
        Callable<Map<String, String>> task = () -> {
            TimeUnit.MILLISECONDS.sleep(taskTimeout);
            return null;
        };
        Future<Map<String, String>> future = executor.submit(task);
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //boolean cancelResult = dynamoDBDaoLegacyFuture.cancel(true); /// act
        
        //assertThat(cancelResult, is(true));
        //assertThat(dynamoDBDaoLegacyFuture.isCancelled(), is(true));
        //assertThat(dynamoDBDaoLegacyFuture.isDone(), is(true));
    }
}
