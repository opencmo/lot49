package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisException;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_doRun {
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
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        redisClientMock = SharedSetUp.createRedisClientMock();
        
        adCacheConfig.setTtlMinutes(ttlMinutes);
        adCacheConfig.setDir(tempFolder.getRoot().getAbsolutePath());
        
        adCache = new AdCache(adCacheConfig);
        Whitebox.setInternalState(adCache, "client", redisClientMock);
    }

    @Test
    public void negativeFlow_breaksIfAlreadyRunning() throws Exception {
        boolean scheduled = true;
        Whitebox.setInternalState(adCache, "isRunning", new AtomicBoolean(true));

        String returnValue = adCache.doRun(scheduled);
        
        assertThat(returnValue.contains("Entering AdCache.doRun(" + scheduled + ")"), is(true));
        assertThat(returnValue.contains("Already running, will exit this time."), is(true));
        assertThat(returnValue.contains("Scheduling another run in 1 MINUTES: "), is(false));
    }

    @Test
    public void negativeFlow_breaksIfRedisErrorOccurs() throws Exception {
        boolean scheduled = true;
        
        if (scheduled) {
            PowerMockito.doReturn(new ScheduledThreadPoolExecutor(poolSize)).when(serviceRunnerMock).getScheduledExecutor();
        }
        
        RuntimeException exception = new RuntimeException(new RedisException("Redis error"));
        PowerMockito.doThrow(exception).when(redisClientMock).connect();

        String returnValue = adCache.doRun(scheduled);
        
        assertThat(returnValue.contains("Entering AdCache.doRun(" + scheduled + ")"), is(true));
        assertThat(returnValue.contains("AdCache.run(): Unexpected error: " + exception), is(true));
        assertThat(returnValue.contains("Exiting AdCache.run(), spent "), is(true));
        assertThat(returnValue.contains("Scheduling another run in 1 MINUTES: "), is(scheduled));
    }

    @Test
    public void positiveFlow_returnsExpectedResultIfNotScheduled() throws Exception {
        testDoRun_positiveFlow(false, false);
    }

    @Test
    public void positiveFlow_returnsExpectedResultIfScheduledAndNotRefreshAds() throws Exception {
        testDoRun_positiveFlow(true, false);
    }

    @Test
    public void positiveFlow_returnsExpectedResultIfScheduledAndRefreshAds() throws Exception {
        testDoRun_positiveFlow(true, true);
    }

    private void testDoRun_positiveFlow(boolean scheduled, boolean refreshAdsIfScheduled) throws Exception {
        boolean refreshAds = !scheduled || refreshAdsIfScheduled;
        long runNumber = (long) ReflectionUtils.getPrivateStatic(AdCache.class, "runNumber");
        
        if (scheduled) {
            PowerMockito.doReturn(new ScheduledThreadPoolExecutor(poolSize)).when(serviceRunnerMock).getScheduledExecutor();
            
            runNumber = refreshAdsIfScheduled ? 0 : 1;
            ReflectionUtils.setPrivateStatic(AdCache.class, "runNumber", runNumber);
        }
        
        if (refreshAds) {
            tempFolder.newFile("wrote.txt");
        }
        
        String returnValue = adCache.doRun(scheduled);
        
        Mockito.verify(redisClientMock.connect()).close();
        
        AtomicBoolean isRunning = (AtomicBoolean) Whitebox.getInternalState(adCache, "isRunning");
        assertThat(isRunning.get(), equalTo(false));
        
        assertThat(returnValue.contains("Entering AdCache.doRun(" + scheduled + ")"), is(true));
        assertThat(returnValue.contains("This is run number " + (runNumber + 1) + "; ttlMinutes: " + ttlMinutes + "; refreshAds: " + refreshAds), is(true));
        assertThat(returnValue.contains("Entering refresh()"), is(refreshAds));
        assertThat(returnValue.contains("Exiting AdCache.run(), spent "), is(true));
        assertThat(returnValue.contains("Scheduling another run in 1 MINUTES: "), is(scheduled));
    }

    @Test
    public void concurrentFlow_secondTaskBreaksIfFirstTaskIsRunning() throws Exception {
        long delayTask1 = 0;
        long delayTask2 = 0;
        long maxTimeWaitLock = 10000;
        long maxTimeWaitTermination = 10000;

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
            lockTask1.unlock();
            return PowerMockito.mock(RedisConnection.class);
        }).when(redisClientMock).connect();
        
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize);
        
        try {
            ScheduledFuture<String> futureTask1 = executor.schedule(() -> adCache.doRun(false), delayTask1, TimeUnit.MILLISECONDS);
            
            long stamp2 = lockTask2.tryWriteLock(maxTimeWaitLock, TimeUnit.MILLISECONDS); /// wait for permission to start task2
            if (stamp2 == 0) { throw new TimeoutException("Time to wait for the lock elapsed"); }
            lockTask2.unlockWrite(stamp2);
        
            ScheduledFuture<String> futureTask2 = executor.schedule(() -> adCache.doRun(false), delayTask2, TimeUnit.MILLISECONDS);
            String returnValue2 = futureTask2.get(); /// wait for ending of task2
            
            if (futureTask1.isDone() || futureTask1.isCancelled()) {
                throw new RuntimeException("The first task was completed earlier than expected");
            }
            
            lockTask1.unlock(); /// allow to continue task1
            String returnValue1 = futureTask1.get(); /// wait for ending of task1
            
            assertThat(returnValue1.contains("Entering AdCache.doRun(false)"), is(true));
            assertThat(returnValue1.contains("Exiting AdCache.run(), spent "), is(true));
            assertThat(returnValue1.contains("Already running, will exit this time."), is(false));

            assertThat(returnValue2.contains("Entering AdCache.doRun(false)"), is(true));
            assertThat(returnValue2.contains("Exiting AdCache.run(), spent "), is(false));
            assertThat(returnValue2.contains("Already running, will exit this time."), is(true));

            executor.shutdown();
            executor.awaitTermination(maxTimeWaitTermination, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
            if (lockTask1.isHeldByCurrentThread()) { lockTask1.unlock(); }
            if (lockTask2.isWriteLocked()) { lockTask2.tryUnlockWrite(); }
        }
    }
}
