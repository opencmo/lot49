package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.times;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_run {
    private AdCacheConfig adCacheConfig;
    private RedisClient redisClientMock;
    private AdCache adCache;
    private ServiceRunner serviceRunnerMock;
    private int poolSize = 2;
    private final int ttlMinutes = 1000; // set big value to avoid ad refresh if it is not first run

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        redisClientMock = SharedSetUp.createRedisClientMock();
        
        adCacheConfig.setTtlMinutes(ttlMinutes);
        adCacheConfig.setDir(tempFolder.getRoot().getAbsolutePath());
        
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        PowerMockito.doReturn(new ScheduledThreadPoolExecutor(poolSize)).when(serviceRunnerMock).getScheduledExecutor();
        
        adCache = new AdCache(adCacheConfig);
        Whitebox.setInternalState(adCache, "client", redisClientMock);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void negativeFlow_logsErrorIfTtlMinutesIsZero() throws Exception {
        Whitebox.setInternalState(adCache, "ttlMinutes", 0);

        adCache.run();
        
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Entering AdCache.doRun(true)"));
        
        PowerMockito.verifyStatic();
        LogUtils.error(any(ArithmeticException.class));
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.info(contains("Scheduling another run in 1 MINUTES: "));
        
        boolean first = (boolean) Whitebox.getInternalState(adCache, "first");
        assertThat(first, is(false));
    }

    @Test
    public void positiveFlow_expectedLogsIfRefreshAds() throws Exception {
        testRun_positiveFlow(true);
    }

    @Test
    public void positiveFlow_expectedLogsIfNotRefreshAds() throws Exception {
        testRun_positiveFlow(false);
    }

    private void testRun_positiveFlow(boolean refreshAds) throws Exception {
        ReflectionUtils.setPrivateStatic(AdCache.class, "runNumber", refreshAds ? 0 : 1);
        
        if (refreshAds) {
            tempFolder.newFile("wrote.txt");
        }
        
        adCache.run();
        
        assertThat(Thread.currentThread().getPriority(), is(Thread.MAX_PRIORITY));
        
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Entering AdCache.doRun(true)"));
        
        PowerMockito.verifyStatic(times(refreshAds ? 1 : 0));
        LogUtils.debug(contains("Entering refresh()"));
        
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Scheduling another run in 1 MINUTES: "));
        
        boolean first = (boolean) Whitebox.getInternalState(adCache, "first");
        assertThat(first, is(false));
    }

    @Test
    public void concurrentFlow_secondTaskBreaksIfFirstTaskIsRunning() throws Exception {
        long delayTask1 = 0;
        long delayTask2 = 0;
        long maxTimeWaitLock = 10000;
        
        tempFolder.newFile("wrote.txt");
        
        /// locks are needed to guarantee that second task (task2) will start during execution first task (task1)
        ReentrantLock lockTask1 = new ReentrantLock();
        lockTask1.lock(); /// pause task1 after its start
        
        StampedLock lockTask2 = new StampedLock();
        long stamp = lockTask2.writeLock(); /// deny to start task2 until the start of task1
        
        PowerMockito.doAnswer((InvocationOnMock invocation) -> {
            lockTask2.unlockWrite(stamp); /// allow to start task2
            boolean lockAcquired = lockTask1.tryLock(maxTimeWaitLock, TimeUnit.MILLISECONDS); /// wait for permission to continue task1
            if (!lockAcquired) { throw new TimeoutException("Time to wait for the lock elapsed"); }
            return PowerMockito.mock(RedisConnection.class);
        }).when(redisClientMock).connect();
        
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize);
        
        try {
            ScheduledFuture<?> futureTask1 = executor.schedule(adCache, delayTask1, TimeUnit.MILLISECONDS);
            
            long stamp2 = lockTask2.tryWriteLock(maxTimeWaitLock, TimeUnit.MILLISECONDS); /// wait for permission to start task2
            if (stamp2 == 0) { throw new TimeoutException("Time to wait for the lock elapsed"); }
            lockTask2.unlockWrite(stamp2);
        
            PowerMockito.mockStatic(LogUtils.class); /// reset the spying for LogUtils.class
            
            ScheduledFuture<?> futureTask2 = executor.schedule(adCache, delayTask2, TimeUnit.MILLISECONDS);
            futureTask2.get(); /// wait for ending of task2
            
            PowerMockito.verifyStatic(times(0));
            LogUtils.debug(contains("Exiting AdCache.run(), spent "));
            
            PowerMockito.verifyStatic(times(1));
            LogUtils.info(contains("Already running, will exit this time."));
            
            PowerMockito.mockStatic(LogUtils.class); /// reset the spying for LogUtils.class
            
            lockTask1.unlock(); /// allow to continue task1
            futureTask1.get(); /// wait for ending of task1
            
            PowerMockito.verifyStatic(times(1));
            LogUtils.debug(contains("Exiting AdCache.run(), spent "));
            
            PowerMockito.verifyStatic(times(0));
            LogUtils.info(contains("Already running, will exit this time."));
            
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
