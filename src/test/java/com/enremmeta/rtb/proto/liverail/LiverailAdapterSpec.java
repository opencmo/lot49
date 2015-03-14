package com.enremmeta.rtb.proto.liverail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.jersey.AuctionsSvc;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class LiverailAdapterSpec {
    private ServiceRunner serviceRunnerSimpleMock;
    private ExchangesConfig exchangesConfigMock;
    private LiverailConfig lCfg;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        lCfg = new LiverailConfig();

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getLiverail()).thenReturn(lCfg);
                return exchangesConfigMock;
            }
        });

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
    }

    @Test
    public void constructorNegativeFlow_seatIdWasNotProvided() {

        try {
            new LiverailAdapter();
            fail("Exception expected but not thrown!");
        } catch (Lot49RuntimeException e) {
            assertEquals("seatId is null", e.getMessage());
        }

    }

    @Test
    public void constructorPositiveFlow_shouldSetLiverailConfig() {

        lCfg.setSeatId("SEAT_TEST_ID");

        LiverailAdapter adapter = new LiverailAdapter();

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getLiverail();

        assertEquals("SEAT_TEST_ID", Whitebox.getInternalState(adapter, "seatId"));

    }

    @Test
    public void convertRequestPositiveFlow_shouldSetLiverailAdapter() throws Throwable {

        lCfg.setSeatId("SEAT_TEST_ID");

        LiverailAdapter adapter = new LiverailAdapter();

        OpenRtbRequest req = new OpenRtbRequest();

        OpenRtbRequest req2 = adapter.convertRequest(req);

        assertEquals(adapter, req2.getLot49Ext().getAdapter());

    }

}
