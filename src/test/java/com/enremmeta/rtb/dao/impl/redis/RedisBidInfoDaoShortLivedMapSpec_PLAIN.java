package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.dao.JsonFuture;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisBidInfoDaoShortLivedMapSpec_PLAIN {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMap;

    private String key = "Key";
    private BidInFlightInfo value = null;
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisBidInfoDaoShortLivedMap = new RedisBidInfoDaoShortLivedMap(redisServiceMock);
    }

    @Test
    public void positiveFlow_getAsync() {
        Future<BidInFlightInfo> result = redisBidInfoDaoShortLivedMap.getAsync(key); /// act
        
        assertThat(result, not(equalTo(null)));
        assertThat(result, instanceOf(JsonFuture.class));
    }

    @Test
    public void positiveFlow_put() {
        RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMapSpy = Mockito.spy(redisBidInfoDaoShortLivedMap);
        PowerMockito.mockStatic(LogUtils.class);
        
        redisBidInfoDaoShortLivedMapSpy.put(key, value); /// act
        
        Mockito.verify(redisBidInfoDaoShortLivedMapSpy).putAsync(key, value);
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Time to put(" + key + "): "));
    }
}
