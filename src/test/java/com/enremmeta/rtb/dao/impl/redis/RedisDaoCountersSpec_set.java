package com.enremmeta.rtb.dao.impl.redis;

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
public class RedisDaoCountersSpec_set {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisDaoCounters redisDaoCounters;

    private String key = "Key";
    private long value = 123;
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisDaoCounters = new RedisDaoCounters(redisServiceMock);
    }

    @Test
    public void positiveFlow_callsSet() throws Exception {
        redisDaoCounters.set(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).set(key, String.valueOf(value));
    }
}
