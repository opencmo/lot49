package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisCacheableWebResponseDaoShortLivedMapSpec_PLAIN {
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

    @Test
    public void positiveFlow_getAsync() throws Lot49Exception {
        Future<CacheableWebResponse> result = redisCacheableWebResponseDaoShortLivedMap.getAsync(key); /// act
        
        assertThat(result, not(equalTo(null)));
        assertThat(result, instanceOf(RedisJsonFuture.class));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_put() {
        // TODO in RedisCacheableWebResponseDaoShortLivedMap.put(): method put() should call method putAsync() instead of to call itself
        RedisCacheableWebResponseDaoShortLivedMap redisCacheableWebResponseDaoShortLivedMapSpy = Mockito.spy(redisCacheableWebResponseDaoShortLivedMap);
        
        redisCacheableWebResponseDaoShortLivedMapSpy.put(key, value); /// act
        
        Mockito.verify(redisCacheableWebResponseDaoShortLivedMapSpy).putAsync(key, value);
    }
}
