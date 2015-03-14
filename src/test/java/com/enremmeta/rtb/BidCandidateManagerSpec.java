package com.enremmeta.rtb;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.container.AsyncResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.TargetingIntegral;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.dao.impl.collections.CollectionsShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.ExchangeAdapter.BidChoiceAlgorithm;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.rtb.proto.spotxchange.SpotXChangeAdapter;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;
import com.enremmeta.rtb.spi.providers.integral.result.IntegralValidationResult;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, BidCandidateManager.class,
                TargetingIntegral.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class BidCandidateManagerSpec {

    private ServiceRunner serviceRunnerSimpleMock;
    private BidCandidate bc;
    private BidCandidateManager bcmSpy;
    private OpenRtbRequest req;
    private OpenRtbResponse resp;
    private Ad adMock;

    @Before
    public void setUp() throws Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        ScheduledThreadPoolExecutor sExecutor = Mockito.mock(ScheduledThreadPoolExecutor.class);
        Mockito.when(sExecutor.scheduleWithFixedDelay(any(BidCandidateManager.class),
                        Mockito.anyLong(), Mockito.anyLong(), any(TimeUnit.class)))
                        .thenReturn(Mockito.mock(ScheduledFuture.class));

        Mockito.when(serviceRunnerSimpleMock.getScheduledExecutor()).thenReturn(sExecutor);

        Orchestrator orch = new LocalOrchestrator(new OrchestratorConfig());
        Whitebox.setInternalState(orch, "nodeId", "TEST_NODE_ID");
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(orch);

        AdCache adcMock = PowerMockito.mock(AdCache.class);
        Mockito.when(adcMock.getBidInFlightInfoMap())
                        .thenReturn(new CollectionsShortLivedMap<BidInFlightInfo>());
        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adcMock);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        req = new OpenRtbRequest();
        resp = new OpenRtbResponse();

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req.setImp(impList);

        resp.setSeatbid(new LinkedList<SeatBid>() {
            {
                add(new SeatBid() {
                    {
                        setBid(new LinkedList<Bid>() {
                            {
                                add(new Bid());
                            }
                        });
                    }
                });
            }
        });

        req.getLot49Ext().setAdapter(new AdaptvAdapter() {
            {
            }
        });

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);
        bcmSpy = PowerMockito.spy(bcm);

        adMock = new Ad_1001001_fake2();

        bc = new BidCandidate(bcmSpy, adMock, new Bid());
        Whitebox.setInternalState(bc, "doneAndPassed", false);
        Whitebox.setInternalState(bc, "doneAndFailed", false);

        ExecutorService execMock = Mockito.mock(ExecutorService.class);
        Mockito.doNothing().when(execMock).execute(Mockito.any());
        Mockito.when(serviceRunnerSimpleMock.getExecutor()).thenReturn(execMock);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void negativeFlow_minimalConditionsForRun() throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        OpenRtbRequest req2 = new OpenRtbRequest();

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        req2.getLot49Ext().setAdapter(new SpotXChangeAdapter());

        BidCandidateManager bcm = new BidCandidateManager(req2, new OpenRtbResponse(),
                        Mockito.mock(AsyncResponse.class), 1000L);

        final ScheduledFuture bcmFuture = Bidder.getInstance().getScheduledExecutor()
                        .scheduleWithFixedDelay(bcm, 10, 10, TimeUnit.MILLISECONDS);
        bcm.setFutureSelf(bcmFuture);

        bcm.run();

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("BidCandidateManager(null,null): No bid for null"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void negativeFlow_declaredButNoRealBids() throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        OpenRtbRequest req2 = new OpenRtbRequest();

        List<Impression> impList = new LinkedList<>();
        impList.add(new Impression());
        req2.setImp(impList);

        req2.getLot49Ext().setAdapter(new AdaptvAdapter());
        req2.getLot49Ext().setNoBid(false);

        req2.getLot49Ext().setLot49Test(true);
        Ad ad = new SharedSetUp.Ad_1001001_mock();
        ad.setBidsToMake(1);
        Ad[] ads = {ad};
        req2.getLot49Ext().setAll(ads);

        BidCandidateManager bcm = new BidCandidateManager(req2, new OpenRtbResponse(),
                        Mockito.mock(AsyncResponse.class), 1000L);

        final ScheduledFuture bcmFuture = Bidder.getInstance().getScheduledExecutor()
                        .scheduleWithFixedDelay(bcm, 10, 10, TimeUnit.MILLISECONDS);
        bcm.setFutureSelf(bcmFuture);

        bcm.run();

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains(
                        "BidCandidateManager(null,null): Run 1. Done: 0/0; passed: 0; failed: 0;"));
    }

    @Test
    public void negativeFlow_setBids_EmptySeatBid() {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.info(anyString());

        OpenRtbResponse resp3 = new OpenRtbResponse();
        BidCandidateManager bcm = new BidCandidateManager(req, resp3,
                        Mockito.mock(AsyncResponse.class), 1000L);

        bcm.setBids(req, resp3);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("Unexpected: Empty seatBid: null"));
    }

    @Test
    public void positiveFlow_setBids_AdaptvAdapter_0candidates() {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        bcm.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq(
                        "BidCandidateManager(null,null): Out of 0: 0 passed, 0 failed, 0 timed out, 1 bids."));
    }

    @Test
    public void positiveFlow_setBids_AdaptvAdapter_1candidates() throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        bcm.add(new BidCandidate(bcm, new SharedSetUp.Ad_1001001_fake(), new Bid()));

        bcm.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    private class Ad_1001001_fake2 extends SharedSetUp.Ad_1001001_fake {

        public Ad_1001001_fake2() throws Lot49Exception {
            super();
            // TODO Auto-generated constructor stub
        }

        public boolean needCanBid2() {
            return true;
        }

    }

    @Test
    public void negativeFlow_setBids_UserDataNotReceived()
                    throws Lot49Exception, InterruptedException, ExecutionException {

        Future<UserSegments> usFuture = Mockito.mock(Future.class);
        Mockito.when(usFuture.get()).thenReturn(new UserSegments());
        Mockito.when(bcmSpy.getUserSegmentsFuture()).thenReturn(usFuture);

        Whitebox.setInternalState(bc, "userSegmentsCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("User data not received"));

    }

    @Test
    public void positiveFlow_setBids_UserDataReceived()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        Future<UserSegments> usFuture = Mockito.mock(Future.class);
        Mockito.when(usFuture.get()).thenReturn(new UserSegments());
        Mockito.when(usFuture.isDone()).thenReturn(true);
        Mockito.when(bcmSpy.getUserSegmentsFuture()).thenReturn(usFuture);

        Whitebox.setInternalState(bc, "userSegmentsCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));

    }

    @Test
    public void negativeFlow_setBids_UserInfoFutureInterruptedException()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.debug(anyString());

        Future<UserSegments> usFuture = Mockito.mock(Future.class);
        Mockito.when(usFuture.get()).thenThrow(new InterruptedException());
        Mockito.when(usFuture.isDone()).thenReturn(true);
        Mockito.when(bcmSpy.getUserSegmentsFuture()).thenReturn(usFuture);

        Whitebox.setInternalState(bc, "userSegmentsCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.debug(Mockito.eq(
                        "Error getting User segments data for null, java.lang.InterruptedException"));


    }

    @Test
    public void negativeFlow_setBids_FcDataNotReceived()
                    throws Lot49Exception, InterruptedException, ExecutionException {

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "fcInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("FC data not received"));

    }

    @Test
    public void positiveFlow_setBids_FcDataReceived()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(uaFuture.isDone()).thenReturn(true);
        Mockito.when(uaFuture.get())
                        .thenReturn(new UserAttributes(
                                        new UserExperimentAttributes(new HashMap<String, String>()),
                                        new UserFrequencyCapAttributes(null, null)));
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "fcInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void negativeFlow_setBids_UserAttributesFutureException()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.debug(anyString());

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(uaFuture.isDone()).thenReturn(true);
        Mockito.when(uaFuture.get()).thenThrow(new InterruptedException());
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "fcInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.debug(Mockito.eq(
                        "Error getting Frequency Cap data for null, java.lang.InterruptedException"));

    }

    @Test
    public void negativeFlow_setBids_OptOut_someDataNotReceived()
                    throws Lot49Exception, InterruptedException, ExecutionException {

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "experimentInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("some data not received"));

    }

    @Test
    public void positiveFlow_setBids_experimentInfoCompleted()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(uaFuture.isDone()).thenReturn(true);
        Mockito.when(uaFuture.get())
                        .thenReturn(new UserAttributes(
                                        new UserExperimentAttributes(new HashMap<String, String>()),
                                        new UserFrequencyCapAttributes(null, null)));
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "fcInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void negativeFlow_setBids_experimentInfoCompleted_UserAttributesFutureException()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.debug(anyString());

        Future<UserAttributes> uaFuture = Mockito.mock(Future.class);
        Mockito.when(uaFuture.isDone()).thenReturn(true);
        Mockito.when(uaFuture.get()).thenThrow(new InterruptedException());
        Mockito.when(bcmSpy.getUserAttributesFuture()).thenReturn(uaFuture);

        Whitebox.setInternalState(bc, "experimentInfoCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.debug(Mockito.eq(
                        "Error getting Experiment data for null, java.lang.InterruptedException"));

    }

    @Test
    public void positiveFlow_setBids_integralCompleted()
                    throws Lot49Exception, InterruptedException, ExecutionException {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralNotNull() throws Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isBrandSafety()).thenReturn(true);
        PowerMockito.doReturn(new IntegralValidationResult(true, "TEST_VALIDITY")).when(tiMock,
                        "validateBrandSafety", any());
        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse() {
                    {

                    }
                });
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralNotNullButNotValid() throws Exception {

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isBrandSafety()).thenReturn(true);
        PowerMockito.doReturn(new IntegralValidationResult(false, "TEST_VALIDITY")).when(tiMock,
                        "validateBrandSafety", any());
        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse());
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("Brand Safety not passed = TEST_VALIDITY"));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralIsTraq() throws Exception {

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isTraq()).thenReturn(true);
        PowerMockito.doReturn(new IntegralValidationResult(true, "TEST_VALIDITY")).when(tiMock,
                        "validateBrandSafety", any());
        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse());
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("No TRAQ received but integral TRAQ specified."));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralIsTraq_traqScoreNotNull() throws Exception {

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isTraq()).thenReturn(true);
        PowerMockito.doReturn(new IntegralValidationResult(false, "TEST_TRAQ")).when(tiMock,
                        "validateTraq", Mockito.anyInt());
        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse() {
                    {
                        setTraq(10);
                    }
                });
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("TRAQ check not passed = TEST_TRAQ"));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralIsViewability() throws Exception {

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isViewability()).thenReturn(true);

        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse());
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("No viewability received but integral viewability specified."));
    }

    @Test
    public void positiveFlow_setBids_adTargetingIntegralIsViewability_viewabilityDtoNotNull()
                    throws Exception {

        TargetingIntegral tiMock = PowerMockito.mock(TargetingIntegral.class);
        Mockito.when(tiMock.isViewability()).thenReturn(true);
        PowerMockito.doReturn(new IntegralValidationResult(false, "TEST_VIEWABILITY")).when(tiMock,
                        "validateViewability", Mockito.any());
        Whitebox.setInternalState(adMock, "targetingIntegral", tiMock);

        Mockito.when(bcmSpy.getIntegralInfoReceived()).thenReturn(new IntegralInfoReceived() {
            {
                setCompleted(true);
                setIntegralAllResponse(new IntegralAllResponse() {
                    {
                        setUem(new ViewabilityDto());
                    }
                });
            }
        });

        Whitebox.setInternalState(bc, "integralCompleted", false);
        bcmSpy.add(bc);

        bcmSpy.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("viewability check not passed = TEST_VIEWABILITY"));
    }

    @Test
    public void positiveFlow_setBids_AdaptvAdapter_bidPriceLongBiggerThenMaxBidPrice_algoMAX()
                    throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        Ad adMock2 = new Ad_1001001_fake2();
        adMock2.setBidPrice(200);
        bcm.add(new BidCandidate(bcm, adMock2, new Bid()));

        bcm.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("MAX strategy used and 200>100"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_AdaptvAdapter_bidPriceLongLessThenMaxBidPrice_algoMAX()
                    throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        Ad adMock2 = new Ad_1001001_fake2();
        adMock2.setBidPrice(50);
        bcm.add(new BidCandidate(bcm, adMock2, new Bid()));

        bcm.setBids(req, resp);
        
        // TODO:
        // investigate the strange result 50>100 in the following assert:

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("MAX strategy used and 50>100"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.contains("BidCandidateManager(null,null): Time spent in decision:"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_1candidate_OpenxAdapter() throws Lot49Exception {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.debug(anyString());

        req.getLot49Ext().setAdapter(new OpenXAdapter());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        bcm.setBids(req, resp);

        //LRU case removed in SUT
        //PowerMockito.verifyStatic(Mockito.times(1));
        //LogUtils.debug(Mockito.eq("Chose LRU"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_2candidate() throws Lot49Exception {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        req.getLot49Ext().setAdapter(new OpenXAdapter());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        bcm.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId()).contains("MAX strategy used"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_2candidate_LastBidTimeReversed()
                    throws Lot49Exception {
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        req.getLot49Ext().setAdapter(new OpenXAdapter());

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setLastBidTime(1000);
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));
        Ad adMock2 = new Ad_1001001_fake2();
        adMock2.setLastBidTime(100);
        bcm.add(new BidCandidate(bcm, adMock2, new Bid()));

        bcm.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId()).contains("MAX strategy used"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_1candidate() throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        ExchangeAdapter ea = Mockito.mock(OpenXAdapter.class);
        Mockito.when(ea.getBidChoiceAlgorithm()).thenReturn(BidChoiceAlgorithm.RANDOM);
        Mockito.when(ea.getName()).thenReturn("AdapterMarker");
        req.getLot49Ext().setAdapter(ea);

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        Whitebox.setInternalState(adMock, "bidsByExchange", new TreeMap<String, AtomicLong>() {
            {
                put(ea.getName(), new AtomicLong(0));
            }
        });
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        bcm.setBids(req, resp);

        //RANDOM case removed in SUT
        //PowerMockito.verifyStatic(Mockito.times(1));
        //LogUtils.trace(Mockito.contains("Chose RANDOM"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

    @Test
    public void positiveFlow_setBids_2candidates() throws Lot49Exception {
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.trace(anyString());

        ExchangeAdapter ea = Mockito.mock(OpenXAdapter.class);
        Mockito.when(ea.getBidChoiceAlgorithm()).thenReturn(BidChoiceAlgorithm.RANDOM);
        Mockito.when(ea.getName()).thenReturn("AdapterMarker");
        req.getLot49Ext().setAdapter(ea);

        BidCandidateManager bcm = new BidCandidateManager(req, resp,
                        Mockito.mock(AsyncResponse.class), 1000L);

        adMock.setBidPrice(100);
        Whitebox.setInternalState(adMock, "bidsByExchange", new TreeMap<String, AtomicLong>() {
            {
                put(ea.getName(), new AtomicLong(0));
            }
        });
        Whitebox.setInternalState(adMock, "optoutsByExchange",
                        new TreeMap<String, SortedMap<String, AtomicLong>>() {
                            {
                                put(ea.getName(), new TreeMap<String, AtomicLong>());
                            }
                        });
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));
        bcm.add(new BidCandidate(bcm, adMock, new Bid()));

        bcm.setBids(req, resp);

        assertTrue(req.getLot49Ext().getOptoutReasons().get(adMock.getId())
                        .contains("MAX strategy used"));
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace(Mockito.eq("Ad 1001001: bids made: 1; remaining bids to make: -1."));
    }

}
