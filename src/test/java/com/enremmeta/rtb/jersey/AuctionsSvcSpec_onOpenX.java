package com.enremmeta.rtb.jersey;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.util.LinkedList;

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
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, LogUtils.class, OpenXAdapter.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onOpenX {
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    private OpenXAdapter opAdapterMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        opAdapterMock = PowerMockito.mock(OpenXAdapter.class);
        PowerMockito.when(opAdapterMock.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();

        PowerMockito.whenNew(OpenXAdapter.class).withNoArguments().thenReturn(opAdapterMock);

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

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldConfiureOpenXChangeAdapterAndCallOnBidRequestDelegate()
                    throws Throwable {

        OpenRtbRequest req = new OpenRtbRequest();
        req.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });

        svc.onOpenX(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        // verify adapter constructor call
        PowerMockito.verifyNew(OpenXAdapter.class).withNoArguments();

        // verify adapter call to convertRequest
        Mockito.verify(opAdapterMock, times(1)).convertRequest(any(OpenRtbRequest.class));

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


        svc.onOpenX(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), badReq, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        // verify adapter constructor call
        PowerMockito.verifyNew(OpenXAdapter.class).withNoArguments();

        // but onBidRequestDelegate shouldn't be called due exception
        PowerMockito.verifyStatic(Mockito.never());
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

        // verify exception
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing request from OpenX: null"),
                        Mockito.any(NullPointerException.class));
    }

}
