package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.HashMap;
import java.util.Map;
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
public class DynamoDBDaoLegacyFutureSpec_get {
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
    public void negativeFlow_returnsNullIfFutureGetReturnsNull() throws Exception {
        Future<Map<String, String>> future = executor.submit(() -> { return null; });
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //String result = dynamoDBDaoLegacyFuture.get(); /// act
        
        //assertThat(result, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfFutureGetReturnsMapWithIt() throws Exception {
        String expectedResult = "Expected result";
        Map<String, String> map = new HashMap<String, String>();
        //map.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, expectedResult );
        
        Future<Map<String, String>> future = executor.submit(() -> { return map; });
        
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //String result = dynamoDBDaoLegacyFuture.get(); /// act
        
        //assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfFutureGetReturnsNotMap() throws Exception {
        String expectedResult = "Expected result";
        
        @SuppressWarnings("rawtypes")
        Future future = executor.submit(() -> { return expectedResult; });
        
        //@SuppressWarnings("unchecked")
        //DynamoDBDaoLegacyFuture<String> dynamoDBDaoLegacyFuture = new DynamoDBDaoLegacyFuture<String>(future);
        
        //String result = dynamoDBDaoLegacyFuture.get(); /// act
        
        //assertThat(result, equalTo(expectedResult));
    }
}
