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

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisBidInfoDaoShortLivedMapSpec_get {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMap;

    private String key = "Key";
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        redisBidInfoDaoShortLivedMap = new RedisBidInfoDaoShortLivedMap(redisServiceMock);
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfCodeRunsSuccessfully() throws Exception {
        Orchestrator orchestratorMock = Mockito.mock(Orchestrator.class);
        Bidder bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(orchestratorMock).when(bidderMock).getOrchestrator();
        
        BidInFlightInfo value = new BidInFlightInfo();
        String valStr = Utils.MAPPER.writeValueAsString(value);
        
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(valStr);
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMapSpy = Mockito.spy(redisBidInfoDaoShortLivedMap);
        
        BidInFlightInfo result = redisBidInfoDaoShortLivedMapSpy.get(key); /// act
        
        Mockito.verify(redisBidInfoDaoShortLivedMapSpy).getAsync(key);
        
        assertThat(Utils.MAPPER.writeValueAsString(result), equalTo(valStr));
    }
    
    @Test
    public void negativeFlow_returnsNullIfCodeThrowsInterruptedException() throws Exception {
        try {
            testGet_negativeFlow(InterruptedException.class);
        } finally {
            Thread.interrupted(); /// to clear interrupted status
        }
    }

    @Test
    public void negativeFlow_returnsNullIfCodeThrowsExecutionException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        testGet_negativeFlow(ExecutionException.class);
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(ExecutionException.class));
    }

    @SuppressWarnings("unchecked")
    private void testGet_negativeFlow(Class<? extends Exception> exceptionClass) throws Exception {
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenThrow(exceptionClass);
        Mockito.when(redisAsyncConnectionMock.get(key)).thenReturn(futureMock);
        
        BidInFlightInfo result = redisBidInfoDaoShortLivedMap.get(key); /// act
        
        assertThat(result, equalTo(null));
    }
}
