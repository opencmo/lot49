package com.enremmeta.rtb.dao.impl.redis;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisDaoShortLivedMapSpec_PLAIN {
    RedisService redisServiceMock;
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisDaoShortLivedMap<?> redisDaoShortLivedMap;

    private String key = "Key";

    @Before
    public void setUp() throws Exception {
        redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisDaoShortLivedMap = Mockito.mock(RedisDaoShortLivedMap.class, Mockito.CALLS_REAL_METHODS); /// RedisDaoShortLivedMap is abstract class
        Mockito.doReturn(redisServiceMock).when(redisDaoShortLivedMap).getRedisService();
    }

    @Test
    public void positiveFlow_setExpiry_String() {
        long ttl = redisServiceMock.getConfig().getShortLivedMapTtlSeconds();
        Whitebox.setInternalState(redisDaoShortLivedMap, "ttl", ttl);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        redisDaoShortLivedMap.setExpiry(key); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug(redisDaoShortLivedMap.getClass().getName() + ":  setExpiry(" + key + ", " + ttl + ")");
        
        Mockito.verify(redisAsyncConnectionMock).expire(key, ttl);
    }

    @Test
    public void positiveFlow_setExpiry_String_long() {
        long specificTtl = 100;
        
        PowerMockito.mockStatic(LogUtils.class);
        
        redisDaoShortLivedMap.setExpiry(key, specificTtl); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug(redisDaoShortLivedMap.getClass().getName() + ":  setExpiry(" + key + ", " + specificTtl + ")");
        
        Mockito.verify(redisAsyncConnectionMock).pexpire(key, specificTtl);
    }
}
