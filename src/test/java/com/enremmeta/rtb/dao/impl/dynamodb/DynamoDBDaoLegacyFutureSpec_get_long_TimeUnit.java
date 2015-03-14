package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore("DynamoDBDaoLegacyFuture was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoLegacyFutureSpec_get_long_TimeUnit {
    private ExecutorService executor;
    private long maxTimeWaitTermination = 10000;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
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
    public void positiveFlow_returnsExpectedValueIfFutureGetReturnsMapWithIt() throws Exception {
        long maxTimeWait = 10000;
        
        String expectedResult = "Expected result";
        Map<String, String> map = new HashMap<String, String>();
        //map.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, expectedResult );
        
        Future<Map<String, String>> future = executor.submit(() -> { return map; });
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //String result = dynamoDBDaoLegacyFuture.get(maxTimeWait, TimeUnit.MILLISECONDS); /// act
        
        //assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void negativeFlow_throwsTimeoutExceptionIfFutureGetWaitTimeElapsed() throws Exception {
        exceptionRule.expect(TimeoutException.class);
        
        long taskTimeout = 1000;
        long maxTimeWait = 10;
        
        Future<Map<String, String>> future = executor.submit(() -> {
            TimeUnit.MILLISECONDS.sleep(taskTimeout);
            return new HashMap<String, String>();
        });
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //dynamoDBDaoLegacyFuture.get(maxTimeWait, TimeUnit.MILLISECONDS); /// act
    }
}
