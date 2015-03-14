package com.enremmeta.rtb.dao.impl.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.BidCandidateManager;
import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.config.HazelcastServiceConfig;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;


@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({RedisService.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisServiceSpec {
    private ServiceRunner serviceRunnerMock;
    private ScheduledThreadPoolExecutor sExecutor;
    private RedisClient rcMock;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        sExecutor = Mockito.mock(ScheduledThreadPoolExecutor.class);
        Mockito.when(sExecutor.scheduleWithFixedDelay(any(BidCandidateManager.class),
                        Mockito.anyLong(), Mockito.anyLong(), any(TimeUnit.class)))
                        .thenReturn(Mockito.mock(ScheduledFuture.class));

        Mockito.when(serviceRunnerMock.getScheduledExecutor()).thenReturn(sExecutor);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerMock);
        
        rcMock = PowerMockito.mock(RedisClient.class);
        PowerMockito.when(rcMock.connectAsync(Mockito.any()))
            .thenReturn(Mockito.mock(RedisAsyncConnection.class));
        
        PowerMockito.whenNew(RedisClient.class).withAnyArguments()
            .thenReturn(rcMock);
            
    }


    @Test
    public void positiveFlow_init_shouldSetRedisServiceConfig() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        rs.init(dbc);
                
        assertEquals(dbc, rs.getConfig());
    }
    
    @Test
    public void positiveFlow_init_shouldSetOtherParams() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        rs.init(dbc);
                
        assertNotNull(Whitebox.getInternalState(rs, "client"));
        assertEquals(128, (int)Whitebox.getInternalState(rs, "poolSize"));
        assertEquals(5, (int)Whitebox.getInternalState(rs, "poolTtlMinutes"));
        assertEquals(128, 
            ((RedisAsyncConnection[])Whitebox.getInternalState(rs, "varzas")).length);
    }
    
    @Test
    public void positiveFlow_init_shouldCallrefreshPool() throws Lot49Exception {
        RedisService rs = Mockito.mock(RedisService.class, Mockito.CALLS_REAL_METHODS);
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        rs.init(dbc);
                
        Mockito.verify(rs, times(1))
            .refreshPool();
    }
    
    @Test
    public void negativeFlow_init_shouldNotSetWrongServiceConfig() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new HazelcastServiceConfig();
       
        try{
            rs.init(dbc);
            fail("shoulld throw exception");
        }catch(Lot49Exception e){
            assertEquals("Expected RedisServiceConfig class, got class com.enremmeta.rtb.config.HazelcastServiceConfig",
                            e.getMessage());
        }
        
        assertNull(rs.getConfig());
    }
    
    @Test
    public void negativeFlow_refreshPool_canNotConnect() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        
        PowerMockito.when(rcMock.connectAsync(Mockito.any()))
            .thenCallRealMethod();
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());
        
        rs.init(dbc);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error refreshing connection 0 to TEST_HOST:9999"),
                        Mockito.any(com.lambdaworks.redis.RedisException.class));
        
    }
    
    @Test
    public void negativeFlow_run_canNotConnect() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        
        Whitebox.setInternalState(rs, "config", dbc);
        Whitebox.setInternalState(rs, "client", rcMock);
        Whitebox.setInternalState(rs, "poolSize", 128);
        
        PowerMockito.when(rcMock.connectAsync(Mockito.any()))
            .thenCallRealMethod();
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());
        
        rs.run();
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error refreshing connection 0 to TEST_HOST:9999"),
                        Mockito.any(com.lambdaworks.redis.RedisException.class));
        
    }

    @Test
    public void positiveFlow_run() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        
        Whitebox.setInternalState(rs, "config", dbc);
        Whitebox.setInternalState(rs, "client", rcMock);
        Whitebox.setInternalState(rs, "poolSize", 128);
        Whitebox.setInternalState(rs, "varzas", new RedisAsyncConnection[128]);
                
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());
        
        rs.run();
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.eq(
            "Refreshing connection pool for TEST_HOST:9999 (pool TTL: 0 minutes; poolSize: 128 connections; timeout: 1000 milliseconds)."));
        
    }
    
    @Test
    public void negativeFlow_getConnection() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        
        Whitebox.setInternalState(rs, "config", dbc);
        Whitebox.setInternalState(rs, "client", rcMock);
        Whitebox.setInternalState(rs, "poolSize", 128);
        Whitebox.setInternalState(rs, "varzas", new RedisAsyncConnection[128]);
                
        try{
            rs.getConnection();
            fail("should throw exception");
        }catch(Lot49RuntimeException e){
            assertEquals("Could not get connection to TEST_HOST:9999", e.getMessage());
        }
        
    }
    
    @Test
    public void positiveFlow_getConnection() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        
        Whitebox.setInternalState(rs, "config", dbc);
        Whitebox.setInternalState(rs, "client", rcMock);
        Whitebox.setInternalState(rs, "poolSize", 128);
        RedisAsyncConnection<String, String>[] testVarzas = 
                        new RedisAsyncConnection[128];
        testVarzas[1] = Mockito.mock(RedisAsyncConnection.class);
        Whitebox.setInternalState(rs, "varzas", testVarzas);
                
        assertNotNull(rs.getConnection());
        
    }
    
    @Test
    public void positiveFlow_getDaoShortLivedMap() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        rs.init(dbc);    
        
        assertEquals("com.enremmeta.rtb.dao.impl.redis.RedisStringDaoShortLivedMap", 
                        rs.getDaoShortLivedMap(String.class).getClass().getCanonicalName());
        assertEquals("com.enremmeta.rtb.dao.impl.redis.RedisBidInfoDaoShortLivedMap", 
                        rs.getDaoShortLivedMap(BidInFlightInfo.class).getClass().getCanonicalName());
        assertEquals("com.enremmeta.rtb.dao.impl.redis.RedisCacheableWebResponseDaoShortLivedMap", 
                        rs.getDaoShortLivedMap(CacheableWebResponse.class).getClass().getCanonicalName());
        try{
            rs.getDaoShortLivedMap(Integer.class);
        fail("should throw exception");
        }catch(UnsupportedOperationException e){
            assertEquals("Cannot deal with java.lang.Integer", 
                            e.getMessage());
        }
    }
    
    @Test
    public void positiveFlow_close() throws Lot49Exception {
        RedisService rs = new RedisService();
        DbConfig dbc = new RedisServiceConfig();
        ((RedisServiceConfig)dbc).setHost("TEST_HOST");
        ((RedisServiceConfig)dbc).setPort(9999);
        rs.init(dbc);    
        
        Whitebox.setInternalState(rs, "poolSize", 1);
        RedisAsyncConnection<String, String>[] testVarzas = 
                        new RedisAsyncConnection[1];
        RedisAsyncConnection racMock = Mockito.mock(RedisAsyncConnection.class);
        testVarzas[0] = racMock;
        Whitebox.setInternalState(rs, "varzas", testVarzas);
        
        rs.close();
        
        Mockito.verify(racMock, times(1))
            .close();
    }
}
