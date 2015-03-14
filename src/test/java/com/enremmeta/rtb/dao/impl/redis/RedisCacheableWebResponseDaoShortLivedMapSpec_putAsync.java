package com.enremmeta.rtb.dao.impl.redis;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisCacheableWebResponseDaoShortLivedMapSpec_putAsync {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisCacheableWebResponseDaoShortLivedMap redisCacheableWebResponseDaoShortLivedMapSpy;

    private String key = "Key";
    private CacheableWebResponse value = new CacheableWebResponse("Entity", "text/plain");
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisCacheableWebResponseDaoShortLivedMapSpy = Mockito.spy(new RedisCacheableWebResponseDaoShortLivedMap(redisServiceMock));
    }

    @Test
    public void positiveFlow_callsSetIfJsonSerializationIsSuccessful() throws Exception {
        String valStr = Utils.MAPPER.writeValueAsString(value);
        
        redisCacheableWebResponseDaoShortLivedMapSpy.putAsync(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).set(key, valStr);
        Mockito.verify(redisCacheableWebResponseDaoShortLivedMapSpy).setExpiry(key);
    }

    @Test
    public void negativeFlow_doesNotCallSetIfJsonSerializationThrowsException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(CacheableWebResponse.class, new SharedSetUp.ThrowExceptionSerializer<CacheableWebResponse>());
        
        ObjectMapper newMapper = new ObjectMapper();
        newMapper.registerModule(module);
        
        ObjectMapper oldMapper = Utils.MAPPER;
        ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", newMapper);
        
        try {
            redisCacheableWebResponseDaoShortLivedMapSpy.putAsync(key, value); /// act
        } finally {
            ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", oldMapper);
        }
        
        Mockito.verify(redisAsyncConnectionMock, never()).set(any(), any());
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(JsonProcessingException.class));
    }
}
