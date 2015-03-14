package com.enremmeta.rtb.dao.impl.redis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.RedisFetchOp;
import com.enremmeta.rtb.caches.RedisFetchOpUser;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, RedisL2Cache.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RedisL2CacheSpec_ALL {

    @Before
    public void setUp() throws Exception { }

    private void createMocksForRefreshPool() throws Exception {
        Bidder bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(Mockito.mock(ScheduledExecutorService.class)).when(bidderMock).getScheduledExecutor();
        
        RedisClient redisClientMock = Mockito.mock(RedisClient.class);
        Mockito.when(redisClientMock.connectAsync(any())).thenAnswer((i) -> { return Mockito.mock(RedisAsyncConnection.class); });
        PowerMockito.whenNew(RedisClient.class).withAnyArguments().thenReturn(redisClientMock);
    }

    private RedisL2Cache<Set<String>> createRedisL2Cache() throws Exception {
        createMocksForRefreshPool();
        
        RedisServiceConfig redisServiceConfig = SharedSetUp.createRedisServiceConfig();
        RedisFetchOp<Set<String>> redisFetchOp = new RedisFetchOpUser();
        
        return new RedisL2Cache<Set<String>>(redisServiceConfig, redisFetchOp);
    }

    @SuppressWarnings("unchecked")
    private RedisL2Cache<Set<String>> createRedisL2CacheMock(boolean fillVarzas) throws Exception {
        RedisServiceConfig redisServiceConfig = SharedSetUp.createRedisServiceConfig();
        RedisFetchOp<Set<String>> redisFetchOp = new RedisFetchOpUser();
        
        RedisL2Cache<Set<String>> redisL2CacheMock = Mockito.mock(RedisL2Cache.class, Mockito.CALLS_REAL_METHODS);
        
        Whitebox.setInternalState(redisL2CacheMock, "fetchOp", redisFetchOp);
        Whitebox.setInternalState(redisL2CacheMock, "redisConfig", redisServiceConfig);
        Whitebox.setInternalState(redisL2CacheMock, "poolSize", redisServiceConfig.getPoolSize());
        Whitebox.setInternalState(redisL2CacheMock, "poolTtlMinutes", redisServiceConfig.getPoolTtlMinutes());
        
        RedisAsyncConnection<String, String>[] varzas = new RedisAsyncConnection[redisServiceConfig.getPoolSize()];
        if (fillVarzas) {
            for (int i = 0; i < varzas.length; i++) {
                varzas[i] = Mockito.mock(RedisAsyncConnection.class);
            }
        }
        Whitebox.setInternalState(redisL2CacheMock, "varzas", varzas);
        
        return redisL2CacheMock;
    }

    @Test
    public void positiveFlow_constructor_initializesFieldsAndCallsRefreshPool() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        RedisL2Cache<Set<String>> redisL2Cache = createRedisL2Cache(); /// act
        
        int poolSize = Whitebox.getInternalState(redisL2Cache, "poolSize");
        assertThat(poolSize, not(equalTo(0)));
        
        RedisAsyncConnection<String, String>[] varzas = Whitebox.getInternalState(redisL2Cache, "varzas");
        assertThat(varzas, not(equalTo(null)));
        assertThat(varzas.length, equalTo(poolSize));
        
        /// constructor should call refreshPool()
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Refreshing connection pool for "));
        
        PowerMockito.verifyStatic();
        LogUtils.info("Closed 0 connections.");
    }

    @Test
    public void positiveFlow_get_returnsExpectedResultAndChangesCurrentConnection() throws Exception {
        String key = "Key";
        @SuppressWarnings("unchecked")
        Future<Set<String>> futureMock = Mockito.mock(Future.class);
        
        RedisL2Cache<Set<String>> redisL2Cache = createRedisL2Cache();
        
        int curVarzaPtr = redisL2Cache.getCurVarzaPtr();
        assertThat(curVarzaPtr, equalTo(0));
        
        int poolSize = Whitebox.getInternalState(redisL2Cache, "poolSize");
        int expectedCurVarzaPtr = (curVarzaPtr < poolSize - 1) ? curVarzaPtr + 1 : 0;

        RedisAsyncConnection<String, String>[] varzas = Whitebox.getInternalState(redisL2Cache, "varzas");
        RedisAsyncConnection<String, String> expectedConnection = varzas[expectedCurVarzaPtr];
        Mockito.when(expectedConnection.smembers(key)).thenReturn(futureMock);

        Future<Set<String>> result = redisL2Cache.get(key); /// act
        
        assertThat(result, equalTo(futureMock));
        
        assertThat(redisL2Cache.getCurVarzaPtr(), equalTo(expectedCurVarzaPtr));
    }

    @Test
    public void positiveFlow_refreshPool_createsConnectionsAndSchedulesExecutionOfRedisL2Cache() throws Exception {
        RedisL2Cache<Set<String>> redisL2Cache = createRedisL2CacheMock(true); /// create mock for RedisL2Cache to avoid call of refreshPool() from constructor
        createMocksForRefreshPool();
        
        PowerMockito.mockStatic(LogUtils.class);
        
        redisL2Cache.refreshPool(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Refreshing connection pool for "));
        
        RedisAsyncConnection<String, String>[] varzas = Whitebox.getInternalState(redisL2Cache, "varzas");
        for (RedisAsyncConnection<String, String> connection : varzas) {
            assertThat(connection, not(equalTo(null)));
        }
        
        PowerMockito.verifyStatic();
        LogUtils.info("Closed " + varzas.length + " connections.");
        
        ScheduledExecutorService scheduledExecutorServiceMock = Bidder.getInstance().getScheduledExecutor();
        Mockito.verify(scheduledExecutorServiceMock).schedule(eq(redisL2Cache), any(Long.class), any(TimeUnit.class));
    }

    @Test
    public void positiveFlow_close_closesAllConnectionsInPool() throws Exception {
        RedisL2Cache<Set<String>> redisL2Cache = createRedisL2Cache();
        
        redisL2Cache.close(); /// act
        
        RedisAsyncConnection<String, String>[] varzas = Whitebox.getInternalState(redisL2Cache, "varzas");
        for (RedisAsyncConnection<String, String> connection : varzas) {
            Mockito.verify(connection).close();
        }
    }
}
