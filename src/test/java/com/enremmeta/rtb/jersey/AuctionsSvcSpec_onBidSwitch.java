package com.enremmeta.rtb.jersey;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.bidswitch.BidSwitchAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, LogUtils.class, BidSwitchAdapter.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onBidSwitch {
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    private BidSwitchAdapter bsAdapterMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        bsAdapterMock = PowerMockito.mock(BidSwitchAdapter.class);
        PowerMockito.when(bsAdapterMock.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();

        PowerMockito.whenNew(BidSwitchAdapter.class).withNoArguments().thenReturn(bsAdapterMock);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        svc = new AuctionsSvc();

        PowerMockito.mockStatic(AuctionsSvc.class);
        PowerMockito.doNothing().when(AuctionsSvc.class);
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());
    }

    @Test
    public void positiveFlow_shouldConfiureBidSwitchAdapterAndCallOnBidRequestDelegate()
                    throws Throwable {


        svc.onBidSwitch(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class),
                        new OpenRtbRequest(), "xff", Mockito.mock(HttpServletRequest.class), "xrip",
                        "debug");

        // verify adapter constructor call
        PowerMockito.verifyNew(BidSwitchAdapter.class).withNoArguments();

        // verify adapter call to convertRequest
        Mockito.verify(bsAdapterMock, times(1)).convertRequest(any(OpenRtbRequest.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

    }

    @Test
    public void negativeFlow_shouldLogBrockenRequest() throws Exception {

        PowerMockito.mockStatic(AuctionsSvc.class);

        OpenRtbRequest badReq = null; // BidRequest is broken now

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());


        svc.onBidSwitch(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), badReq,
                        "xff", Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        // verify adapter constructor call
        PowerMockito.verifyNew(BidSwitchAdapter.class).withNoArguments();

        // but onBidRequestDelegate shouldn't be called due exception
        PowerMockito.verifyStatic(Mockito.never());
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

        // verify exception
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing request from Bidswitch: null"),
                        Mockito.any(NullPointerException.class));
    }

}
