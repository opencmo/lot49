package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_rescheduleSelf {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private ServiceRunner serviceRunnerMock;
    private int poolSize = 2;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();
    }

    @Test
    public void negativeFlow_notSchedulingIfTtlMinutesIsNotPositive() throws Exception {
        int ttlMinutes = 0;
        
        adCacheConfig.setTtlMinutes(ttlMinutes);
        adCache = new AdCache(adCacheConfig);
        
        String returnValue = Whitebox.invokeMethod(adCache, "rescheduleSelf");
        
        assertThat(returnValue, equalTo("Not scheduling: TTL " + ttlMinutes));
    }

    @Test
    public void positiveFlow_SchedulingIfTtlMinutesIsPositive() throws Exception {
        int ttlMinutes = 1;
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize);
        
        try {
            PowerMockito.doReturn(executor).when(serviceRunnerMock).getScheduledExecutor();
    
            adCacheConfig.setTtlMinutes(ttlMinutes);
            adCache = new AdCache(adCacheConfig);
            Whitebox.setInternalState(adCache, "scheduledSelf", (ScheduledFuture<?>) null);
            
            String returnValue = Whitebox.invokeMethod(adCache, "rescheduleSelf");
            
            ScheduledFuture<?> scheduledSelf = (ScheduledFuture<?>) Whitebox.getInternalState(adCache, "scheduledSelf");
            assertThat(scheduledSelf, not(equalTo(null)));
            
            assertThat(returnValue, containsString("Scheduling another run in 1 MINUTES: " + scheduledSelf));
            
            scheduledSelf.cancel(true);
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
