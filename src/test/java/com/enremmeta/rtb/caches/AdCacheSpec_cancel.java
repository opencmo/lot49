package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_cancel {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private int poolSize = 2;

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();

        adCache = new AdCache(adCacheConfig);
    }

    @Test
    public void negativeFlow_returnsFalseIfTaskIsRunning() {
        Whitebox.setInternalState(adCache, "isRunning", new AtomicBoolean(true));
        
        boolean returnValue = adCache.cancel();
        
        assertThat(returnValue, is(false));
    }

    @Test
    public void negativeFlow_returnsTrueIfTaskIsNotRunningAndIsNotScheduled() {
        boolean returnValue = adCache.cancel();
        
        assertThat(returnValue, is(true));
    }

    @Test
    public void positiveFlow_returnsTrueIfTaskIsNotRunningButIsScheduledAndIsNotStartedYet() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize);
        ScheduledFuture<?> scheduled = executor.schedule(adCache, 10, TimeUnit.SECONDS);
                        
        Whitebox.setInternalState(adCache, "scheduledSelf", scheduled);
        
        boolean returnValue = adCache.cancel();
        
        assertThat(returnValue, is(true));
        assertThat(scheduled.isDone(), is(true));
    }

    @Test
    public void positiveFlow_returnsFalseIfTaskIsNotRunningButWasScheduledAndThenWasCanceled() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize);
        ScheduledFuture<?> scheduled = executor.schedule(adCache, 10, TimeUnit.SECONDS);
        scheduled.cancel(true);
                        
        Whitebox.setInternalState(adCache, "scheduledSelf", scheduled);
        
        boolean returnValue = adCache.cancel();
        
        assertThat(returnValue, is(false));
        assertThat(scheduled.isDone(), is(true));
    }
}
