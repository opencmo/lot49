package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisDaoCountersSpec_getAndSet {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    @SuppressWarnings("unchecked")
    private Future<String> futureMock = Mockito.mock(Future.class);
    private RedisDaoCounters redisDaoCounters;

    private String key = "Key";
    private long value = 123;
    private String valStr = String.valueOf(value);
    
    @Before
    public void setUp() throws Exception {
        Mockito.when(redisAsyncConnectionMock.getset(key, valStr)).thenReturn(futureMock);
        
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisDaoCounters = new RedisDaoCounters(redisServiceMock);
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfCodeRunsSuccessfully() throws Exception {
        String getReturn = "456";
        long expectedResult = Long.parseLong(getReturn);
        
        testGetAndSet_positiveFlow(getReturn, expectedResult);
    }

    @Test
    public void positiveFlow_returnsZeroIfFutureGetReturnsNull() throws Exception {
        String getReturn = null;
        long expectedResult = 0L;
        
        testGetAndSet_positiveFlow(getReturn, expectedResult);
    }
    
    private void testGetAndSet_positiveFlow(String getReturn, long expectedResult) throws Exception {
        Mockito.when(futureMock.get()).thenReturn(getReturn);
        
        long result = redisDaoCounters.getAndSet(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).getset(key, valStr);
        
        assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void negativeFlow_throwsLot49ExceptionIfCodeThrowsInterruptedException() throws Exception {
        try {
            testGetAndSet_negativeFlow(InterruptedException.class);
        } finally {
            Thread.interrupted(); /// to clear interrupted status
        }
    }

    @Test
    public void negativeFlow_throwsLot49ExceptionIfCodeThrowsExecutionException() throws Exception {
        testGetAndSet_negativeFlow(ExecutionException.class);
    }

    @SuppressWarnings("unchecked")
    private void testGetAndSet_negativeFlow(Class<? extends Exception> exceptionClass) throws Exception {
        Mockito.when(futureMock.get()).thenThrow(exceptionClass);

        try {
            redisDaoCounters.getAndSet(key, value); /// act
            fail("Expected test to throw an instance of com.enremmeta.rtb.Lot49Exception");
        } catch (Lot49Exception ex) {
            assertThat(ex.getCause(), instanceOf(exceptionClass));
        }
    }
}
