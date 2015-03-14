package com.enremmeta.rtb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class})
@PowerMockIgnore("javax.management.*")
public class BidderSpec_runBidder {

    private Bidder bidderMock;
    private Lot49Config configMock;

    @Before
    public void setUp() {

        bidderMock = PowerMockito.mock(Bidder.class);

        configMock = Mockito.mock(Lot49Config.class);
        Mockito.when(configMock.getContainer()).thenReturn("none");

        Mockito.when(bidderMock.getConfig()).thenReturn(configMock);
        Whitebox.setInternalState(bidderMock, "shutdownHook", new ShutdownHook());

        //Mockito.when(bidderMock.getUserCache())
        //                .thenReturn((UserCache) Mockito.mock(UserCache.class));

        Whitebox.setInternalState(Bidder.class, "runner", bidderMock);
    }

    @Test
    public void shouldInitExecutors() throws Exception {

        Mockito.doNothing().when(bidderMock).initExecutors();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initExecutors();
    }

    @Test
    public void shouldInitOrchestrator() throws Exception {

        Mockito.doNothing().when(bidderMock).initOrchestrator();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initOrchestrator();
    }

    @Test
    public void shouldInitLogging() throws Exception {

        Mockito.doNothing().when(bidderMock).initLogging();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initLogging();
    }

    @Test
    public void shouldInitAdapters() throws Exception {

        Mockito.doNothing().when(bidderMock).initAdapters();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initAdapters();
    }

    @Test
    public void shouldScheduleLogRolloverEnforcer() throws Exception {

        Mockito.doNothing().when(bidderMock).scheduleLogRolloverEnforcer();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).scheduleLogRolloverEnforcer();
    }

    @Test
    public void shouldInitDbServices() throws Exception {

        Mockito.doNothing().when(bidderMock).initDbServices();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initDbServices();
    }

    @Test
    public void shouldInitSecurityManager() throws Exception {

        Mockito.doNothing().when(bidderMock).initSecurityManager();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initSecurityManager();
    }

    @Test
    public void shouldInitCaches() throws Exception {

        Mockito.doNothing().when(bidderMock).initCaches();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initCaches();
    }

    @Test
    public void shouldInitGeo() throws Exception {

        Mockito.doNothing().when(bidderMock).initGeo();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initGeo();
    }

    @Test
    public void shouldInitProviders() throws Exception {

        Mockito.doNothing().when(bidderMock).initProviders();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initProviders();
    }

    @Test
    public void shouldInitIntegral() throws Exception {

        Mockito.doNothing().when(bidderMock).initIntegral();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initIntegral();
    }

    @Test
    public void shouldInitCodecs() throws Exception {

        Mockito.doNothing().when(bidderMock).initCodecs();

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        Mockito.verify(bidderMock, Mockito.times(1)).initCodecs();
    }

    @Test
    public void canStartServerNetty() throws Exception {

        PowerMockito.doNothing().when(bidderMock, "startServerNetty");

        Mockito.when(configMock.getContainer()).thenReturn("netty");

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        PowerMockito.verifyPrivate(bidderMock, Mockito.times(1)).invoke("startServerNetty");
    }

    @Test
    public void canStartServerJetty() throws Exception {

        PowerMockito.doNothing().when(bidderMock, "startServerJetty");

        Mockito.when(configMock.getContainer()).thenReturn("jetty");

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        PowerMockito.verifyPrivate(bidderMock, Mockito.times(1)).invoke("startServerJetty");
    }

    @Test
    public void rejectUncnownContainer() throws Exception {

        Mockito.when(configMock.getContainer()).thenReturn("some wrong container");

        try {
            Whitebox.invokeMethod(Bidder.class, "runBidder");
            fail("should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown container: some wrong container", e.getMessage());
        }

    }

}
