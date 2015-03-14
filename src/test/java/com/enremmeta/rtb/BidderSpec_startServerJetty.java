package com.enremmeta.rtb;

import static org.mockito.Matchers.anyString;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractLifeCycle.class, ServiceRunner.class, Bidder.class, LogUtils.class})
@PowerMockIgnore({"javax.management.*", "javax.crypto.*"})
public class BidderSpec_startServerJetty {

    private Bidder bidderMock;
    private Lot49Config configMock;

    @Before
    public void setUp() throws Exception {

        bidderMock = PowerMockito.mock(Bidder.class);

        configMock = Mockito.mock(Lot49Config.class);
        
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);
        
        Mockito.when(configMock.getContainer()).thenReturn("jetty");
        Mockito.when(configMock.getJettyAcceptQueueSize()).thenReturn(65535);
        
        Mockito.when(bidderMock.getConfig()).thenReturn(configMock);
        Whitebox.setInternalState(bidderMock, "config", configMock);
        Whitebox.setInternalState(bidderMock, "shutdownHook", new ShutdownHook());

        //Mockito.when(bidderMock.getUserCache())
        //                .thenReturn((UserCache) Mockito.mock(UserCache.class));
        
        Whitebox.setInternalState(Bidder.class, "runner", bidderMock);
        
        Server serverMock  = PowerMockito.mock(Server.class);
        Mockito.doAnswer(new DoesNothing()).when(serverMock).start();
        Mockito.doNothing().when(serverMock).join();
        
        PowerMockito.whenNew(Server.class).withNoArguments()
            .thenReturn(serverMock);
        
        ServerConnector serverConnectorMock  = PowerMockito.mock(ServerConnector.class);
        Mockito.doAnswer(new DoesNothing()).when(serverConnectorMock).start();
        
        PowerMockito.whenNew(ServerConnector.class).withAnyArguments()
            .thenReturn(serverConnectorMock);
                       
    }

    @Test
    public void canStartServerJetty() throws Exception {

        PowerMockito.doCallRealMethod().when(bidderMock, "startServerJetty");

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());

        Whitebox.invokeMethod(Bidder.class, "runBidder");

        PowerMockito.verifyPrivate(bidderMock, Mockito.times(1)).invoke("startServerJetty");
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Lot49 is listening on"));
    }

}
