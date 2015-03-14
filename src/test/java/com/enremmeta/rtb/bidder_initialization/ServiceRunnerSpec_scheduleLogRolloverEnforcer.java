package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.StampedLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_scheduleLogRolloverEnforcer {
    // should schedule and run log rollover enforcer
    
    private int poolSize = 2;
    private ServiceRunner serviceRunnerMock;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);

        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void positiveFlow_expectedLogsIfRolloverEnforcerFinishedSuccessfully() throws Exception {
        long lastRolloverEnforcerRan = Whitebox.getInternalState(serviceRunnerMock, "lastRolloverEnforcerRan");
        assertThat(lastRolloverEnforcerRan, equalTo(0L));
        
        testScheduleLogRolloverEnforcer(false);
        
        PowerMockito.verifyStatic();
        LogUtils.logRequest(any(), eq(false), eq(0));
        
        PowerMockito.verifyStatic();
        LogUtils.logRequest(any(), eq(true), eq(0));
        
        PowerMockito.verifyStatic();
        LogUtils.logBid(any(), eq(0L), any(), eq("pushkin"), eq("pushkin"), any(), eq(0), eq(0), eq(null), anyString());
        
        PowerMockito.verifyStatic();
        LogUtils.logClick(eq(null), eq(null), any(), eq(null), eq(null), eq(null), eq(null),
                        eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                        any(), eq(null), eq(null), eq(null));
        
        lastRolloverEnforcerRan = Whitebox.getInternalState(serviceRunnerMock, "lastRolloverEnforcerRan");
        assertThat(lastRolloverEnforcerRan, not(equalTo(0L)));
    }
    
    @Test
    public void negativeFlow_errorLogsIfRolloverEnforcerThrowException() throws Exception {
        long lastRolloverEnforcerRan = Whitebox.getInternalState(serviceRunnerMock, "lastRolloverEnforcerRan");
        assertThat(lastRolloverEnforcerRan, equalTo(0L));
        
        testScheduleLogRolloverEnforcer(true);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Error in scheduleLogRolloverEnforcer()"), any());
        
        lastRolloverEnforcerRan = Whitebox.getInternalState(serviceRunnerMock, "lastRolloverEnforcerRan");
        assertThat(lastRolloverEnforcerRan, equalTo(0L));
    }
    
    private void testScheduleLogRolloverEnforcer(boolean throwException) throws Exception {
        long maxTimeWaitLock = 10000;
        long maxTimeWaitTermination = 10000;
        
        StampedLock lockShutdownExecutor = new StampedLock();
        long stamp = lockShutdownExecutor.writeLock(); /// deny to shutdown scheduledExecutor until the start of scheduled task
        
        PowerMockito.doAnswer((InvocationOnMock invocation) -> {
            lockShutdownExecutor.unlockWrite(stamp); /// allow to shutdown scheduledExecutor
            if (throwException) { throw new Exception("Exception in scheduleLogRolloverEnforcer()"); }
            return null;
        }).when(LogUtils.class, "init", contains("scheduleLogRolloverEnforcer() pushing empty line into logs"));
        
        ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(poolSize);
        Whitebox.setInternalState(serviceRunnerMock, "scheduledExecutor", scheduledExecutor);
        
        try {
            serviceRunnerMock.scheduleLogRolloverEnforcer();
    
            long stamp2 = lockShutdownExecutor.tryWriteLock(maxTimeWaitLock, TimeUnit.MILLISECONDS); /// wait for permission to shutdown scheduledExecutor
            if (stamp2 == 0) { throw new TimeoutException("Time to wait for the lock elapsed"); }
            lockShutdownExecutor.unlockWrite(stamp2);
        
            scheduledExecutor.shutdown();
            scheduledExecutor.awaitTermination(maxTimeWaitTermination, TimeUnit.MILLISECONDS); /// wait until scheduled task has completed execution
        } finally {
            scheduledExecutor.shutdownNow();
        }
    }
}
