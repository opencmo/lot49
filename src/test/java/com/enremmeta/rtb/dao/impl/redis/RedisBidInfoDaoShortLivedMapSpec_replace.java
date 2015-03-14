package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.io.IOException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisBidInfoDaoShortLivedMapSpec_replace {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMap;

    private String key = "Key";
    private BidInFlightInfo value;
    
    @Before
    public void setUp() throws Exception {
        RedisService redisServiceMock = SharedSetUp.createRedisServiceMock();
        Mockito.when(redisServiceMock.getConnection()).thenReturn(redisAsyncConnectionMock);
        
        Orchestrator orchestratorMock = Mockito.mock(Orchestrator.class);
        Bidder bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(orchestratorMock).when(bidderMock).getOrchestrator();
        
        value = new BidInFlightInfo();
        
        redisBidInfoDaoShortLivedMap = new RedisBidInfoDaoShortLivedMap(redisServiceMock);
    }

    @Test
    public void positiveFlow_returnsOldValueIfCodeRunsSuccessfully() throws Exception {
        RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMapSpy = Mockito.spy(redisBidInfoDaoShortLivedMap);
        String valStr = Utils.MAPPER.writeValueAsString(value);
        BidInFlightInfo oldValue = new BidInFlightInfo("OldValueReason");
        String oldValStr = Utils.MAPPER.writeValueAsString(oldValue);
        
        @SuppressWarnings("unchecked")
        Future<String> futureMock = Mockito.mock(Future.class);
        Mockito.when(futureMock.get()).thenReturn(oldValStr);
        Mockito.when(redisAsyncConnectionMock.getset(key, valStr)).thenReturn(futureMock);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        BidInFlightInfo result = redisBidInfoDaoShortLivedMapSpy.replace(key, value); /// act
        
        Mockito.verify(redisAsyncConnectionMock).getset(key, valStr);
        Mockito.verify(futureMock).get();
        Mockito.verify(redisBidInfoDaoShortLivedMapSpy).setExpiry(key);
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + redisBidInfoDaoShortLivedMapSpy.getClass().getName() + ":  replace(" + key + ", " + valStr + "): " + oldValStr);
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Time to replace(" + key + "): "));
        
        assertThat(Utils.MAPPER.writeValueAsString(result), equalTo(oldValStr));
    }
    
    @Test
    public void negativeFlow_returnsNullIfCodeThrowsInterruptedException() throws Exception {
        try {
            testReplace_negativeFlow(InterruptedException.class);
        } finally {
            Thread.interrupted(); /// to clear interrupted status
        }
    }

    @Test
    public void negativeFlow_returnsNullIfCodeThrowsExecutionException() throws Exception {
        testReplace_negativeFlow(ExecutionException.class);
    }

    @Test
    public void negativeFlow_returnsNullIfCodeThrowsJsonProcessingException() throws Exception {
        testReplace_negativeFlow(JsonProcessingException.class);
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
        
        BidInFlightInfo result = redisBidInfoDaoShortLivedMap.replace(key, value); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(exceptionClass));
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Time to replace(" + key + "): "));
        
        assertThat(result, equalTo(null));
    }
}
