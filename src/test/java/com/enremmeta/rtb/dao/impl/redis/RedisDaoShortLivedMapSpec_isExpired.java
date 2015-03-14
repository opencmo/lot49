package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisDaoShortLivedMapSpec_isExpired {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisDaoShortLivedMap<?> redisDaoShortLivedMap;

    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisDaoShortLivedMap = Mockito.mock(RedisDaoShortLivedMap.class, Mockito.CALLS_REAL_METHODS); /// RedisDaoShortLivedMap is abstract class
        Mockito.doReturn(redisServiceMock).when(redisDaoShortLivedMap).getRedisService();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnsFalseIfTimeLeftIsPositive() throws Exception {
        Long timeLeft = 60 * 1000L; /// milliseconds
        Future<Long> pttlFutureMock = Mockito.mock(Future.class);
        Mockito.when(pttlFutureMock.get()).thenReturn(timeLeft);
        
        testIsExpired(pttlFutureMock, false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnsTrueIfPttlGetThrowsInterruptedException() throws Exception {
        Future<Long> pttlFutureMock = Mockito.mock(Future.class);
        Mockito.when(pttlFutureMock.get()).thenThrow(InterruptedException.class);
        
        testIsExpired(pttlFutureMock, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnsTrueIfPttlGetThrowsExecutionException() throws Exception {
        Future<Long> pttlFutureMock = Mockito.mock(Future.class);
        Mockito.when(pttlFutureMock.get()).thenThrow(ExecutionException.class);
        
        testIsExpired(pttlFutureMock, true);
    }

    private void testIsExpired(Future<Long> pttlFuture, Boolean expectedResult) throws InterruptedException, ExecutionException {
        String key = "Key";
        
        Mockito.when(redisAsyncConnectionMock.pttl(key)).thenReturn(pttlFuture);
        
        Boolean result = redisDaoShortLivedMap.isExpired(key); /// act
        
        Mockito.verify(redisAsyncConnectionMock).pttl(key);
        Mockito.verify(pttlFuture).get();
        
        assertThat(result, equalTo(expectedResult));
    }
}
