package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisDaoCountersSpec_addAndGet {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisDaoCounters redisDaoCounters;

    private String key = "Key";
    private long value = 123;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisDaoCounters = new RedisDaoCounters(redisServiceMock);
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfCodeRunsSuccessfully() throws Exception {
        long expectedResult = 456;
        
        @SuppressWarnings("unchecked")
        Future<Long> futureMock = Mockito.mock(Future.class);
        Mockito.when(redisAsyncConnectionMock.incrby(key, value)).thenReturn(futureMock);
        Mockito.when(futureMock.get()).thenReturn(expectedResult);
        
        RedisDaoCounters redisDaoCountersSpy = Mockito.spy(redisDaoCounters);
        
        long result = redisDaoCountersSpy.addAndGet(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).incrby(key, value);
        Mockito.verify(redisDaoCountersSpy).getSafe(futureMock);
        
        assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void positiveFlow_returnsZeroIfIncrbyReturnsNull() throws Exception {
        Mockito.when(redisAsyncConnectionMock.incrby(key, value)).thenReturn(null);

        long result = redisDaoCounters.addAndGet(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).incrby(key, value);
        
        assertThat(result, equalTo(0L));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_throwsExecutionExceptionIfIncrbyThrowsExecutionException() throws Exception {
        exceptionRule.expect(ExecutionException.class);
        
        Mockito.when(redisAsyncConnectionMock.incrby(key, value)).thenThrow(ExecutionException.class);
        
        redisDaoCounters.addAndGet(key, value); /// act
    }

    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_throwsLot49ExceptionIfFutureGetThrowsExecutionException() throws Exception {
        exceptionRule.expect(Lot49Exception.class);
        
        Future<Long> futureMock = Mockito.mock(Future.class);
        Mockito.when(redisAsyncConnectionMock.incrby(key, value)).thenReturn(futureMock);
        Mockito.when(futureMock.get()).thenThrow(ExecutionException.class);

        redisDaoCounters.addAndGet(key, value); /// act
    }
}
