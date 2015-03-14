package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
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

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, AdaptvAdapter.class, UriInfo.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onAdaptv {
    // should process request from Ad Exchange
    private ServiceRunner serviceRunnerSimpleMock;
    private AuctionsSvc svc;
    AdaptvAdapter adaptvAdapterMock;

    @Before
    public void setUp() throws Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

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
    public void positiveFlow_shouldConfiureAdapterAndCallOnBidRequestDelegate() throws Exception {

        OpenRtbRequest req = new OpenRtbRequest();
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        Mockito.when(adaptvAdapterMock.convertRequest(any(OpenRtbRequest.class)))
                        .thenReturn(new OpenRtbRequest());

        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterMock);

        svc.onAdaptv(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");


        // verify adapter constructor call
        PowerMockito.verifyNew(AdaptvAdapter.class).withNoArguments();

        // verify ADX adapter call to convertRequest
        Mockito.verify(adaptvAdapterMock, times(1)).convertRequest(any(OpenRtbRequest.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

    }

    @Test
    public void negativeFlow_canNotSetOpenRtbRequestFromEmptyQuery() throws Exception {
        OpenRtbRequest req = new OpenRtbRequest();
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterMock);

        svc.onAdaptv(Mockito.mock(AsyncResponse.class), PowerMockito.mock(UriInfo.class), req,
                        "xff", Mockito.mock(HttpServletRequest.class), "xrip", "debug");


        // verify adapter constructor call
        PowerMockito.verifyNew(AdaptvAdapter.class).withNoArguments();

        // onBidRequestDelegate called even on null query?
        PowerMockito.verifyStatic(Mockito.times(1));

        AuctionsSvc.onBidRequestDelegate(any(JerseySvc.class), anyString(),
                        any(ExchangeAdapter.class), any(AsyncResponse.class),
                        any(OpenRtbRequest.class), anyString(), any(HttpServletRequest.class),
                        anyString());

        // TODO:
        // refactoring idea: add exception throw for adaptvAdapter.convertRequest
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestAuctionId() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);
        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals("AUCTION_ID", openRtbRequestMarker.getId());

    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestDevice() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), Mockito.mock(UriInfo.class), req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals(SharedSetUp.IP_MARKER, openRtbRequestMarker.getDevice().getIp());
        assertEquals("TEST_UA_CHROME", openRtbRequestMarker.getDevice().getUa());
        assertEquals("TEST_LANG_ES", openRtbRequestMarker.getDevice().getLanguage());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestSite() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals("TEST_DOMAIN", openRtbRequestMarker.getSite().getDomain());
        assertEquals("TEST_URL", openRtbRequestMarker.getSite().getRef());
        assertEquals("TEST_URL", openRtbRequestMarker.getSite().getPage());
        assertEquals("TEST_PLACEMENT_NAME", openRtbRequestMarker.getSite().getName());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestApp() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals("TEST_APP_NAME", openRtbRequestMarker.getApp().getName());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestImpression() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals("TEST_IMP_ID", openRtbRequestMarker.getImp().get(0).getId());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestVideoFormats() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals("video/x-flv",
                        openRtbRequestMarker.getImp().get(0).getVideo().getMimes().get(0));
        assertEquals("video/mp4",
                        openRtbRequestMarker.getImp().get(0).getVideo().getMimes().get(1));
        assertEquals("video/ogg",
                        openRtbRequestMarker.getImp().get(0).getVideo().getMimes().get(2));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestPlayackMethod() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals(2, openRtbRequestMarker.getImp().get(0).getVideo().getPlaybackmethod().get(0)
                        .intValue());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_setOpenRtbRequestOtherVideoSettings() throws Exception {
        adaptvAdapterMock = PowerMockito.mock(AdaptvAdapter.class);
        AdaptvAdapter adaptvAdapterSpy = PowerMockito.spy(adaptvAdapterMock);
        PowerMockito.whenNew(AdaptvAdapter.class).withNoArguments().thenReturn(adaptvAdapterSpy);
        PowerMockito.when(adaptvAdapterSpy.convertRequest(any(OpenRtbRequest.class)))
                        .thenCallRealMethod();


        OpenRtbRequest openRtbRequestMarker = new OpenRtbRequest();

        PowerMockito.whenNew(OpenRtbRequest.class).withNoArguments()
                        .thenReturn(openRtbRequestMarker);

        UriInfo uriInfoMock = PowerMockito.mock(UriInfo.class);

        OpenRtbRequest req = new OpenRtbRequest();
        svc.onAdaptv(Mockito.mock(AsyncResponse.class), uriInfoMock, req, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "debug");

        assertEquals(1, openRtbRequestMarker.getImp().get(0).getVideo().getMinduration());
        assertEquals(10, openRtbRequestMarker.getImp().get(0).getVideo().getMaxduration());
        assertEquals(350, openRtbRequestMarker.getImp().get(0).getVideo().getH());
        assertEquals(350, openRtbRequestMarker.getImp().get(0).getVideo().getW());
        assertEquals(RtbConstants.API_VPAID_1,
                        openRtbRequestMarker.getImp().get(0).getVideo().getApi().get(0).intValue());
        assertEquals(RtbConstants.API_VPAID_2,
                        openRtbRequestMarker.getImp().get(0).getVideo().getApi().get(1).intValue());
    }
}
