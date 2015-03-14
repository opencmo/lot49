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
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, LogUtils.class,
                Test1ExchangeAdapter.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onTest {
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    private Test1ExchangeAdapter tstAdapterMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        tstAdapterMock = PowerMockito.mock(Test1ExchangeAdapter.class);
        PowerMockito.when(tstAdapterMock.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();

        PowerMockito.whenNew(Test1ExchangeAdapter.class).withNoArguments()
                        .thenReturn(tstAdapterMock);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        AdCache adCacheMock = PowerMockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getBid2()).thenReturn(new Ad[0]);
        Mockito.when(adCacheMock.getAll()).thenReturn(new Ad[0]);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adCacheMock);

        svc = new AuctionsSvc();

        PowerMockito.mockStatic(AuctionsSvc.class);
        PowerMockito.doNothing().when(AuctionsSvc.class);
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());
    }

    @Test
    public void positiveFlow_shouldConfiureTest1XChangeAdapterAndCallOnBidRequestDelegate()
                    throws Throwable {


        svc.onTest(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class),
                        new OpenRtbRequest(), "xff", Mockito.mock(HttpServletRequest.class),
                        "xrip");

        // verify adapter constructor call
        PowerMockito.verifyNew(Test1ExchangeAdapter.class).withNoArguments();

        // verify adapter call to convertRequest
        Mockito.verify(tstAdapterMock, times(1)).convertRequest(any(OpenRtbRequest.class));

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
        LogUtils.error(any());


        svc.onTest(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), badReq, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        // verify adapter constructor call
        PowerMockito.verifyNew(Test1ExchangeAdapter.class).withNoArguments();

        // but onBidRequestDelegate shouldn't be called due exception
        PowerMockito.verifyStatic(Mockito.never());
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

        // verify exception
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.any(NullPointerException.class));
    }

}
