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
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.util.ServiceRunner;
import com.google.protos.adx.NetworkBid;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onAdx {
    // should process request from Ad Exchange
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    AdXAdapter adxAdapterMock;

    @Before
    public void setUp() throws Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        adxAdapterMock = PowerMockito.mock(AdXAdapter.class);
        Mockito.when(adxAdapterMock.convertRequest(any(NetworkBid.BidRequest.class)))
                        .thenCallRealMethod();

        PowerMockito.whenNew(AdXAdapter.class).withNoArguments().thenReturn(adxAdapterMock);

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
    public void positiveFlow_shouldConfiureAdXAdapterAndCallOnBidRequestDelegate()
                    throws Exception {


        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();


        svc.onAdx(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), req, "xff",
                        "debug", Mockito.mock(HttpServletRequest.class), "xrip");

        // verify ADX adapter constructor call
        PowerMockito.verifyNew(AdXAdapter.class).withNoArguments();

        // verify ADX adapter call to convertRequest
        Mockito.verify(adxAdapterMock, times(1)).convertRequest(any(NetworkBid.BidRequest.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

    }

    @Test
    public void negativeFlow_shouldLogBrockenRequest() throws Exception {

        PowerMockito.mockStatic(AuctionsSvc.class);

        NetworkBid.BidRequest req = null; // BidRequest is broken now

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());


        svc.onAdx(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), req, "xff",
                        "debug", Mockito.mock(HttpServletRequest.class), "xrip");

        // verify ADX adapter constructor call
        PowerMockito.verifyNew(AdXAdapter.class).withNoArguments();

        // but onBidRequestDelegate shouldn't be called due exception
        PowerMockito.verifyStatic(Mockito.never());
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

        // verify exception
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing request from AdX: null"),
                        Mockito.any(NullPointerException.class));
    }

}
