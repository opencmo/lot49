package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.io.IOException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
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
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisCacheableWebResponseDaoShortLivedMapSpec_replace {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisCacheableWebResponseDaoShortLivedMap redisCacheableWebResponseDaoShortLivedMap;

    private String key = "Key";
    private CacheableWebResponse value = new CacheableWebResponse("Entity", "text/plain");
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisCacheableWebResponseDaoShortLivedMap = new RedisCacheableWebResponseDaoShortLivedMap(redisServiceMock);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_returnsOldValueIfCodeRunsSuccessfully() throws Exception {
        // TODO in RedisCacheableWebResponseDaoShortLivedMap.replace(): assign variable 'retval' the result of Utils.MAPPER.readValue(json, CacheableWebResponse.class)
        // TODO in RedisCacheableWebResponseDaoShortLivedMap.replace(): Utils.MAPPER should not serialize/deserialize property getResponse() of class CacheableWebResponse
        ObjectMapper newMapper = new ObjectMapper();
        newMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        newMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        RedisCacheableWebResponseDaoShortLivedMap redisCacheableWebResponseDaoShortLivedMapSpy = Mockito.spy(redisCacheableWebResponseDaoShortLivedMap);
        String valStr = newMapper.writeValueAsString(value);
        CacheableWebResponse oldValue = new CacheableWebResponse("OldEntity", "text/plain");
        String oldValStr = newMapper.writeValueAsString(oldValue);
        
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(oldValStr);
        Mockito.when(redisAsyncConnectionMock.getset(key, valStr)).thenReturn(futureMock);
        
        ObjectMapper oldMapper = Utils.MAPPER;
        ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", newMapper);
        
        CacheableWebResponse result;
        try {
            result = redisCacheableWebResponseDaoShortLivedMapSpy.replace(key, value); /// act
        } finally {
            ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", oldMapper);
        }
        
        Mockito.verify(redisAsyncConnectionMock).getset(key, valStr);
        Mockito.verify(redisCacheableWebResponseDaoShortLivedMapSpy).getSafe(futureMock);
        Mockito.verify(redisCacheableWebResponseDaoShortLivedMapSpy).setExpiry(key);
        
        assertThat(newMapper.writeValueAsString(result), equalTo(oldValStr));
    }
    
    @Test
    public void negativeFlow_returnsNullIfCodeThrowsIOException() throws Exception {
        testReplace_negativeFlow(IOException.class);
    }
    
    @SuppressWarnings("unchecked")
    private void testReplace_negativeFlow(Class<? extends Exception> exceptionClass) throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenThrow(exceptionClass);
        Mockito.when(redisAsyncConnectionMock.getset(eq(key), any())).thenReturn(futureMock);
        
        CacheableWebResponse result = redisCacheableWebResponseDaoShortLivedMap.replace(key, value); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(exceptionClass));
        
        assertThat(result, equalTo(null));
    }
}
