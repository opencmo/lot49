package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
import com.enremmeta.rtb.dao.JsonFuture;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class, RedisCacheableWebResponseDaoShortLivedMap.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisCacheableWebResponseDaoShortLivedMapSpec_remove {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisCacheableWebResponseDaoShortLivedMap redisCacheableWebResponseDaoShortLivedMap;

    private String key = "Key";
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisCacheableWebResponseDaoShortLivedMap = new RedisCacheableWebResponseDaoShortLivedMap(redisServiceMock);
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfCodeRunsSuccessfully() throws Exception {
        // TODO in RedisCacheableWebResponseDaoShortLivedMap.remove(): Utils.MAPPER should not serialize/deserialize property getResponse() of class CacheableWebResponse
        ObjectMapper newMapper = new ObjectMapper();
        newMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        newMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        CacheableWebResponse value = new CacheableWebResponse("Entity", "text/plain");
        String valStr = newMapper.writeValueAsString(value);
        
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(valStr);
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        ObjectMapper oldMapper = Utils.MAPPER;
        ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", newMapper);
        
        CacheableWebResponse result;
        try {
            result = redisCacheableWebResponseDaoShortLivedMap.remove(key); /// act
        } finally {
            ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", oldMapper);
        }

        Mockito.verify(redisAsyncConnectionMock).del(key);
        
        assertThat(newMapper.writeValueAsString(result), equalTo(valStr));
    }
    
    @Test
    public void negativeFlow_throwsLot49ExceptionIfCodeThrowsInterruptedException() throws Exception {
        try {
            testGet_negativeFlow(InterruptedException.class);
        } finally {
            Thread.interrupted(); /// to clear interrupted status
        }
    }

    @Test
    public void negativeFlow_throwsLot49ExceptionIfCodeThrowsExecutionException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        testGet_negativeFlow(ExecutionException.class);
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(ExecutionException.class));
    }

    @SuppressWarnings("unchecked")
    private void testGet_negativeFlow(Class<? extends Exception> exceptionClass) throws Exception {
        JsonFuture<CacheableWebResponse> futureMock = Mockito.mock(JsonFuture.class);
        Mockito.when(futureMock.get()).thenThrow(exceptionClass);
        PowerMockito.whenNew(JsonFuture.class).withAnyArguments().thenReturn(futureMock);

        CacheableWebResponse result = redisCacheableWebResponseDaoShortLivedMap.remove(key); /// act
        
        assertThat(result, equalTo(null));
    }
}
