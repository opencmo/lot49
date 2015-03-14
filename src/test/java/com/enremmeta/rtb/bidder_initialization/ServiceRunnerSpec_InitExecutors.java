package com.enremmeta.rtb.bidder_initialization;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class, ServiceRunner.class})
@PowerMockIgnore("javax.management.*")
public class ServiceRunnerSpec_InitExecutors {
    // should create 2 executors and configure them
    private final static int EXECUTOR_THREAD_POOL_SIZE = 16;
    private ScheduledThreadPoolExecutor scheduledExecutor;
    private ThreadPoolExecutor executor;
    private ServiceRunner serviceRunnerSimpleMock;

    @Before
    public void beforeEach() throws Lot49Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        scheduledExecutor = new ScheduledThreadPoolExecutor(EXECUTOR_THREAD_POOL_SIZE);
        executor = new ThreadPoolExecutor(EXECUTOR_THREAD_POOL_SIZE, Integer.MAX_VALUE,
                        Integer.MAX_VALUE, NANOSECONDS, new LinkedBlockingQueue<Runnable>());

        Lot49Config configMock = Mockito.mock(Lot49Config.class);
        Mockito.when(configMock.getExecutorThreadPoolSize()).thenReturn(EXECUTOR_THREAD_POOL_SIZE);
        Whitebox.setInternalState(serviceRunnerSimpleMock, "config", configMock);
    }

    @Test
    public void shouldInitTheScheduledThreadPoolExecutorField() {

        assertNull(serviceRunnerSimpleMock.getScheduledExecutor());

        serviceRunnerSimpleMock.initExecutors();

        assertNotNull(serviceRunnerSimpleMock.getScheduledExecutor());
    }

    @Test
    public void theScheduledThreadPoolExecutorFieldShouldBeConstructedWithThreadPoolSize()
                    throws Exception {

        mockSheduledThreadPoolExecutorConstruction();

        serviceRunnerSimpleMock.initExecutors();

        PowerMockito.verifyNew(ScheduledThreadPoolExecutor.class)
                        .withArguments(EXECUTOR_THREAD_POOL_SIZE);
    }

    private void mockSheduledThreadPoolExecutorConstruction() throws Exception {
        PowerMockito.whenNew(ScheduledThreadPoolExecutor.class)
                        .withArguments(EXECUTOR_THREAD_POOL_SIZE).thenReturn(scheduledExecutor);
    }

    @Test
    public void theScheduledThreadPoolExecutorFieldShouldBeConstructedWithKeepAliveSettings()
                    throws Exception {

        mockSheduledThreadPoolExecutorConstruction();

        serviceRunnerSimpleMock.initExecutors();

        assertEquals(Long.MAX_VALUE, scheduledExecutor.getKeepAliveTime(TimeUnit.NANOSECONDS));
    }

    @Test
    public void theScheduledThreadPoolExecutorFieldShouldBeConstructedWithRejectedExecutionHandler()
                    throws Exception {


        mockSheduledThreadPoolExecutorConstruction();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(Mockito.any());

        serviceRunnerSimpleMock.initExecutors();

        try {
            scheduledExecutor.getRejectedExecutionHandler().rejectedExecution(new Runnable() {

                @Override
                public void run() {}

                @Override
                public String toString() {
                    return "fakeRunnableForServiceRunnerSpec";
                }

            }, scheduledExecutor);
            fail("My method didn't throw when I expected it to");
        } catch (RuntimeException expectedException) {
        }

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.any());

    }

    @Test
    public void shouldInitTheThreadPoolExecutorField() {

        assertNull(serviceRunnerSimpleMock.getExecutor());

        serviceRunnerSimpleMock.initExecutors();

        assertNotNull(serviceRunnerSimpleMock.getExecutor());
    }

    @Test
    public void theThreadPoolExecutorFieldShouldBeConstructedWithThreadPoolSize() throws Exception {

        mockThreadPoolExecutorConstruction();

        serviceRunnerSimpleMock.initExecutors();

        PowerMockito.verifyNew(ThreadPoolExecutor.class).withArguments(
                        Mockito.eq(EXECUTOR_THREAD_POOL_SIZE), Mockito.anyInt(), Mockito.anyLong(),
                        Mockito.any(), Mockito.any());
    }


    private void mockThreadPoolExecutorConstruction() throws Exception {
        PowerMockito.whenNew(ThreadPoolExecutor.class).withArguments(Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyLong(), Mockito.any(), Mockito.any())
                        .thenReturn(executor);
    }

    @Test
    public void theThreadPoolExecutorFieldShouldBeConstructedWithKeepAliveSettings()
                    throws Exception {

        mockThreadPoolExecutorConstruction();

        serviceRunnerSimpleMock.initExecutors();

        assertEquals(Long.MAX_VALUE, executor.getKeepAliveTime(TimeUnit.NANOSECONDS));

        // TODO:
        // refactoring idea:
        // KeepAliveSettings are set in two places: in constructor of ThreadPoolExecutor and in this
        // setter
        // tpe.setKeepAliveTime(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        //
        //
        // should be set in one place - in constructor. Also RejectedExecutionHandler can be set
        // there
    }

    @Test
    public void theThreadPoolExecutorFieldShouldBeConstructedWithRejectedExecutionHandler()
                    throws Exception {


        mockThreadPoolExecutorConstruction();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(Mockito.any());

        serviceRunnerSimpleMock.initExecutors();

        try {
            executor.getRejectedExecutionHandler().rejectedExecution(new Runnable() {

                @Override
                public void run() {}

                @Override
                public String toString() {
                    return "fakeRunnableForServiceRunnerSpec";
                }

            }, executor);
            fail("My method didn't throw when I expected it to");
        } catch (RuntimeException expectedException) {
        }

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.any());

    }

}
