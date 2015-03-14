package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_init {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private ServiceRunner serviceRunnerMock;
    private DbService dbServiceMock;
    private DaoCounters daoCountersMock;
    private int poolSize = 2;
    
    @Before
    public void setUp() throws Exception {
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();
        
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        dbServiceMock = Mockito.mock(DbService.class); 
        daoCountersMock = Mockito.mock(DaoCounters.class);
        Mockito.when(dbServiceMock.getDaoCounters()).thenReturn(daoCountersMock);
        PowerMockito.doReturn(dbServiceMock).when(serviceRunnerMock).getDbServiceByName(anyString());
        PowerMockito.doReturn(new ScheduledThreadPoolExecutor(poolSize)).when(serviceRunnerMock).getScheduledExecutor();

        adCache = new AdCache(adCacheConfig);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void positiveFlow_initializesNecessaryFieldsAndSchedulesTask() throws Lot49Exception, Exception {
        Whitebox.setInternalState(adCache, "scheduledSelf", null);
        
        adCache.init();
        
        assertThat(Whitebox.getInternalState(adCache, "shortTermDb"), equalTo(dbServiceMock));
        assertThat(Whitebox.getInternalState(adCache, "winRateCounters"), equalTo(daoCountersMock));
        
        ScheduledFuture<?> scheduledSelf = (ScheduledFuture<?>) Whitebox.getInternalState(adCache, "scheduledSelf");
        assertThat(scheduledSelf, not(equalTo(null)));
        
        PowerMockito.verifyStatic();
        LogUtils.info(contains("Scheduling first run in 1 second: " + scheduledSelf));
        
        scheduledSelf.cancel(true); /// cancel scheduled task 
    }

}
