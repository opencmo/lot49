package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.BidCandidateManager;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.Orchestrator;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.UserCacheConfig;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindFacade;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookInfoReceived;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookProvider;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;
import com.google.protos.adx.NetworkBid;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class, Utils.class, BidCandidateManager.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionsSvcSpec_onBidRequestDelegate {


    private ServiceRunner serviceRunnerSimpleMock;
    private AdXAdapter adxAdapterMock;
    private ScheduledThreadPoolExecutor sExecutor;
    private NetworkBid.BidRequest req;
    private OpenRtbRequest req2;

    @SuppressWarnings({"serial", "rawtypes", "unchecked"})
    @Before
    public void setUp() throws Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        adxAdapterMock = PowerMockito.mock(AdXAdapter.class);
        Mockito.when(adxAdapterMock.convertRequest(any(NetworkBid.BidRequest.class)))
                        .thenCallRealMethod();
        Mockito.when(adxAdapterMock.getName()).thenCallRealMethod();
        Mockito.when(adxAdapterMock.getOptoutBuilder(Mockito.any(OpenRtbRequest.class)))
                        .thenCallRealMethod();

        PowerMockito.whenNew(AdXAdapter.class).withNoArguments().thenReturn(adxAdapterMock);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        Mockito.when(configMock.getUserCache()).thenReturn(new UserCacheConfig());

        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        MaxMindFacade maxMindFacade = Mockito.mock(MaxMindFacade.class);
        Mockito.when(maxMindFacade.getGeo(SharedSetUp.IP_MARKER))
                        .thenReturn(SharedSetUp.GEO_MARKER);

        Orchestrator orch = new LocalOrchestrator(new OrchestratorConfig());

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        Mockito.when(serviceRunnerSimpleMock.getMaxMind()).thenReturn(maxMindFacade);

        Mockito.when(serviceRunnerSimpleMock.getNextId()).thenReturn("NEXT_TEST_BID_ID");

        // TODO:
        // refactoring ideas:
        // Parameterize SkyhookProvider constructor
        Provider skyHook = new SkyhookProvider(serviceRunnerSimpleMock, new HashMap() {
            {
                put("enabled", true);
            }
        });
        Provider skyHookSpy = Mockito.spy(skyHook);
        Mockito.doReturn(new SkyhookInfoReceived(new HashSet<Integer>() {
            {
                add(1);
            }
        }, false, false)).when(skyHookSpy).getProviderInfo(any(OpenRtbRequest.class));
        Map<String, Provider> providers = new HashMap<String, Provider>() {
            {
                put("skyhook", skyHookSpy);
            }
        };
        Mockito.when(serviceRunnerSimpleMock.getProviders()).thenReturn(providers);
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(orch);
        Mockito.when(serviceRunnerSimpleMock.getAdCache())
                        .thenReturn(PowerMockito.mock(AdCache.class));

        sExecutor = Mockito.mock(ScheduledThreadPoolExecutor.class);
        Mockito.when(sExecutor.scheduleWithFixedDelay(any(BidCandidateManager.class),
                        Mockito.anyLong(), Mockito.anyLong(), any(TimeUnit.class)))
                        .thenReturn(Mockito.mock(ScheduledFuture.class));

        Mockito.when(serviceRunnerSimpleMock.getScheduledExecutor()).thenReturn(sExecutor);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.addOptoutHeaders(any(Lot49Ext.class), any(ResponseBuilder.class)))
                        .thenCallRealMethod();

        PowerMockito.spy(AuctionsSvc.class);
        PowerMockito.doReturn(new Bid()).when(AuctionsSvc.class, "getBid", any(Ad.class),
                        any(OpenRtbRequest.class));

        req = NetworkBid.BidRequest.getDefaultInstance();
        req2 = adxAdapterMock.convertRequest(req);
    }


    @Test
    public void negativeFlow_optOutNoOneEligible() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad[] ads = {new SharedSetUp.Ad_1001001_fake()};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.times(1)).getOptoutBuilder(req2);

        final ArgumentCaptor<ResponseBuilder> responseBuilderCaptor =
                        ArgumentCaptor.forClass(ResponseBuilder.class);
        PowerMockito.verifyStatic();
        Utils.addOptoutHeaders(Mockito.any(Lot49Ext.class), responseBuilderCaptor.capture());

        assertEquals("{x-lot49-optout=[No-one-eligible]}",
                        responseBuilderCaptor.getValue().build().getHeaders().toString());
    }

    @Test
    public void negativeFlow_noImpressions() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad[] ads = {new SharedSetUp.Ad_1001001_fake()};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        // right, just empty list at this time...
        req2.setImp(impList);

        AsyncResponse aResponse = SharedSetUp.getFakeAsynkResponseObject();

        AsyncResponse aResponseSpy = Mockito.spy(aResponse);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, aResponseSpy, req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.times(1)).getOptoutBuilder(req2);

        final ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        Mockito.verify(aResponseSpy).resume(responseCaptor.capture());

        assertEquals("{x-lot49-optout=[No impressions]}",
                        responseCaptor.getValue().getHeaders().toString());
    }

    @Test
    public void negativeFlow_noAds() throws Exception {

        req2.getLot49Ext().setLot49Test(true);

        // right, just empty array of ads at this time...
        Ad[] ads = {};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AsyncResponse aResponse = SharedSetUp.getFakeAsynkResponseObject();

        AsyncResponse aResponseSpy = Mockito.spy(aResponse);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, aResponseSpy, req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.times(1)).getOptoutBuilder(req2);

        Mockito.verify(aResponseSpy, Mockito.times(1)).resume(any(Response.class));

        assertEquals("No ads here -- refreshing ad cache.",
                        req2.getLot49Ext().getOptoutReasons().get("ALL"));
    }

    @Test
    public void positiveFlow_noOptOut() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.never()).getOptoutBuilder(req2);

        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(
                        any(BidCandidateManager.class), Mockito.anyLong(), Mockito.anyLong(),
                        any(TimeUnit.class));
    }

    @Test
    public void positiveFlow_extractingIPDataFromRequest() throws Exception {

        Device testDevice = new Device();
        testDevice.setIp(SharedSetUp.IP_MARKER);
        req2.setDevice(testDevice);

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(
                        any(BidCandidateManager.class), Mockito.anyLong(), Mockito.anyLong(),
                        any(TimeUnit.class));

        assertEquals(SharedSetUp.GEO_MARKER, req2.getLot49Ext().getGeo());
    }

    @Test
    public void positiveFlow_extractingRemoteHostPortAndAddr() throws Exception {

        Device testDevice = new Device();
        testDevice.setUa(SharedSetUp.USER_AGENT);
        req2.setDevice(testDevice);

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        HttpServletRequest httpSRMock = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpSRMock.getRemoteHost()).thenReturn("TEST_REMOTE_HOST");
        Mockito.when(httpSRMock.getRemotePort()).thenReturn(8888);
        Mockito.when(httpSRMock.getRemoteAddr()).thenReturn("TEST_REMOTE_ADDR");

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff", httpSRMock,
                        "xrip");

        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(
                        any(BidCandidateManager.class), Mockito.anyLong(), Mockito.anyLong(),
                        any(TimeUnit.class));

        assertEquals("TEST_REMOTE_HOST", req2.getLot49Ext().getRemoteHost());
        assertEquals(8888, req2.getLot49Ext().getRemotePort());
        assertEquals("TEST_REMOTE_ADDR", req2.getLot49Ext().getRemoteAddr());
    }

    @Test
    public void positiveFlow_settingProvidersData() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(
                        any(BidCandidateManager.class), Mockito.anyLong(), Mockito.anyLong(),
                        any(TimeUnit.class));

        assertEquals("{skyhook={ \"skyhook\" : { \"resident\" : false,  \"businesss\" : false, \"cats\" : [1]}}",
                        req2.getLot49Ext().getProviderInfo().toString());
    }

    @Test
    public void positiveFlow_settingXFFAndXRIPData() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), SharedSetUp.IP_MARKER);

        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(
                        any(BidCandidateManager.class), Mockito.anyLong(), Mockito.anyLong(),
                        any(TimeUnit.class));

        assertEquals("xff", req2.getLot49Ext().getxForwardedFor());
        assertEquals(SharedSetUp.IP_MARKER, req2.getLot49Ext().getRealIpHeader());
    }

    @Test
    public void negativeFlow_needAdCacheOutsideLot49Test_noAdsSituation() throws Exception {

        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertFalse(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        AsyncResponse aResponse = SharedSetUp.getFakeAsynkResponseObject();

        AsyncResponse aResponseSpy = Mockito.spy(aResponse);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, aResponseSpy, req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.times(1)).getOptoutBuilder(req2);

        Mockito.verify(aResponseSpy, Mockito.times(1)).resume(any(Response.class));

        assertEquals("No ads here -- refreshing ad cache.",
                        req2.getLot49Ext().getOptoutReasons().get("ALL"));
    }

    @Test
    public void positiveFlow_constructsBidCandidateManager() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        PowerMockito.whenNew(BidCandidateManager.class)
                        .withArguments(any(OpenRtbRequest.class), any(OpenRtbResponse.class),
                                        any(AsyncResponse.class), Mockito.anyLong())
                        .thenReturn(PowerMockito.mock(BidCandidateManager.class));

        AsyncResponse asyncResponseMock = Mockito.mock(AsyncResponse.class);

        req2.setTmax(10);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, asyncResponseMock, req2, "xff",
                        Mockito.mock(HttpServletRequest.class), SharedSetUp.IP_MARKER);

        PowerMockito.verifyNew(BidCandidateManager.class).withArguments(Mockito.eq(req2),
                        any(OpenRtbResponse.class), // openRtbResponseCaptor.capture(),
                        Mockito.eq(asyncResponseMock), Mockito.eq(10L));
    }

    @Test
    public void positiveFlow_feedingOpenRTBResponse() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        OpenRtbResponse openRtbResponseMarker = new OpenRtbResponse();
        PowerMockito.whenNew(OpenRtbResponse.class).withNoArguments()
                        .thenReturn(openRtbResponseMarker);

        req2.setId("TEST_ID");

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, Mockito.mock(AsyncResponse.class), req2, "xff",
                        Mockito.mock(HttpServletRequest.class), SharedSetUp.IP_MARKER);


        assertEquals("TEST_ID", openRtbResponseMarker.getId());
        assertEquals("NEXT_TEST_BID_ID", openRtbResponseMarker.getBidid());
        assertEquals("USD", openRtbResponseMarker.getCur());
        assertEquals(1, openRtbResponseMarker.getSeatbid().size());

    }

    @Test
    public void positiveFlow_checkBidCandidateManagerConstruction() throws Exception {

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        assertTrue(req2.getLot49Ext().isLot49Test());

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        OpenRtbResponse openRtbResponseMarker = new OpenRtbResponse();
        PowerMockito.whenNew(OpenRtbResponse.class).withNoArguments()
                        .thenReturn(openRtbResponseMarker);

        AsyncResponse arMarker = Mockito.mock(AsyncResponse.class);

        AuctionsSvc.onBidRequestDelegate(Mockito.mock(JerseySvc.class), "true", // debug mode
                        adxAdapterMock, arMarker, req2, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        Mockito.verify(adxAdapterMock, Mockito.never()).getOptoutBuilder(req2);

        final ArgumentCaptor<BidCandidateManager> bcmCaptor =
                        ArgumentCaptor.forClass(BidCandidateManager.class);
        Mockito.verify(sExecutor, Mockito.times(1)).scheduleWithFixedDelay(bcmCaptor.capture(),
                        Mockito.anyLong(), Mockito.anyLong(), any(TimeUnit.class));

        assertTrue((long) (Whitebox.getInternalState(bcmCaptor.getValue(), "startTime")) > 0);
        assertEquals(req2, Whitebox.getInternalState(bcmCaptor.getValue(), "bReq"));
        assertEquals(openRtbResponseMarker,
                        Whitebox.getInternalState(bcmCaptor.getValue(), "bResp"));
        assertEquals(arMarker, Whitebox.getInternalState(bcmCaptor.getValue(), "asyncResp"));
        assertEquals(new Long(0), Whitebox.getInternalState(bcmCaptor.getValue(), "timeout"));
        assertNotNull(Whitebox.getInternalState(bcmCaptor.getValue(), "integralInfoReceived"));
    }
}
