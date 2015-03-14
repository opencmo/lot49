package com.enremmeta.rtb.dao.impl.redis;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
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

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lambdaworks.redis.RedisAsyncConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisBidInfoDaoShortLivedMapSpec_putAsync {
    @SuppressWarnings("unchecked")
    private RedisAsyncConnection<String, String> redisAsyncConnectionMock = Mockito.mock(RedisAsyncConnection.class);
    private RedisBidInfoDaoShortLivedMap redisBidInfoDaoShortLivedMapSpy;

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
        
        redisBidInfoDaoShortLivedMapSpy = Mockito.spy(new RedisBidInfoDaoShortLivedMap(redisServiceMock));
    }

    @Test
    public void positiveFlow_callsSetIfJsonSerializationIsSuccessful() throws Exception {
        String valStr = Utils.MAPPER.writeValueAsString(value);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        redisBidInfoDaoShortLivedMapSpy.putAsync(key, value); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug("Dao: " + redisBidInfoDaoShortLivedMapSpy.getClass().getName() + ":  putAsync(" + key + ", " + valStr + ")");
        
        Mockito.verify(redisAsyncConnectionMock).set(key, valStr);
        Mockito.verify(redisBidInfoDaoShortLivedMapSpy).setExpiry(key);
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Time to putAsync(" + key + "): "));
    }

    @Test
    public void negativeFlow_doesNotCallSetIfJsonSerializationThrowsException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(BidInFlightInfo.class, new SharedSetUp.ThrowExceptionSerializer<BidInFlightInfo>());
        
        ObjectMapper newMapper = new ObjectMapper();
        newMapper.registerModule(module);
        
        ObjectMapper oldMapper = Utils.MAPPER;
        ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", newMapper);
        
        try {
            redisBidInfoDaoShortLivedMapSpy.putAsync(key, value); /// act
        } finally {
            ReflectionUtils.setFinalStatic(Utils.class, "MAPPER", oldMapper);
        }
        
        Mockito.verify(redisAsyncConnectionMock, never()).set(any(), any());
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(JsonProcessingException.class));
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Time to putAsync(" + key + "): "));
    }
}
