package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.UserFrequencyCapAttributes;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, LostAuctionTask.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class BidCandidateManagerSpec_postProcessBid {
    
    private static enum CampaignAbTesting {
        NONE,
        NOT_EXPERIMENT_FOR_CAMPAIGN,
        CONTROL_FOR_CAMPAIGN,
        TEST_FOR_CAMPAIGN
    }
    
    private static enum TargetingStrategyAbTesting {
        NONE,
        CONTROL_FOR_TARGETING_STRATEGY,
        TEST_FOR_TARGETING_STRATEGY
    }
    
    private OpenRtbRequest rtbRequest = new OpenRtbRequest();
    private OpenRtbResponse rtbResponse = new OpenRtbResponse();
    private AsyncResponse asyncResponseMock = Mockito.mock(AsyncResponse.class);
    private long timeout = 10000L;
    
    private ExchangeAdapter<?, ?> exchangeAdapter = new Test1ExchangeAdapter();
    private BidCandidateManager bidCandidateManager;
    
    private Ad adMock;
    private Bid bid = new Bid();
    private boolean multiple = true;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        adMock = createAdMock();
        
        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getNurlMap()).thenReturn(Mockito.mock(DaoShortLivedMap.class));
        
        ExecutorService executorServiceMock = Mockito.mock(ExecutorService.class);
        Mockito.doAnswer(invocation -> {
            Runnable runnable = (Runnable) invocation.getArgumentAt(0, Runnable.class);
            runnable.run();
            return null;
        }).when(executorServiceMock).execute(any());
        
        Bidder bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(adCacheMock).when(bidderMock).getAdCache();
        PowerMockito.doReturn(executorServiceMock).when(bidderMock).getExecutor();
        PowerMockito.doReturn(Mockito.mock(Orchestrator.class)).when(bidderMock).getOrchestrator();
        
        rtbRequest.setImp(new ArrayList<Impression>());
        rtbRequest.getLot49Ext().setAdapter(exchangeAdapter);
        
        bidCandidateManager = new BidCandidateManager(rtbRequest, rtbResponse, asyncResponseMock, timeout);
        
        UserAttributes userAttributes = new UserAttributes(
                        new UserExperimentAttributes(new HashMap<String, String>()), 
                        new UserFrequencyCapAttributes(new HashMap<String, Set<String>>(), new HashMap<String, Set<String>>()));
        bidCandidateManager.setUserAttributes(userAttributes);
        
        PowerMockito.mockStatic(LostAuctionTask.class);
        PowerMockito.mockStatic(LogUtils.class);
    }

    private Ad createAdMock() throws Exception {
        Ad adMock = Mockito.mock(Ad.class);
        
        Mockito.when(adMock.getId()).thenReturn("Ad_ID");
        Mockito.when(adMock.getCampaignId()).thenReturn("Campaign_ID");

        Mockito.when(adMock.isCampaignFrequencyCap()).thenReturn(true);
        Mockito.when(adMock.isStrategyFrequencyCap()).thenReturn(true);
        Mockito.when(adMock.checkMeasurementSegments(any())).thenReturn(true);
        
        configureAdmock(adMock, CampaignAbTesting.NONE, TargetingStrategyAbTesting.NONE);
        
        return adMock;
    }
    
    private void configureAdmock(Ad adMock, CampaignAbTesting campaignAbTesting, TargetingStrategyAbTesting targetingStrategyAbTesting) throws Exception {
        switch (campaignAbTesting) {
            case NONE:
                Mockito.when(adMock.doCampaignAbTesting()).thenReturn(false);
                break;
            case NOT_EXPERIMENT_FOR_CAMPAIGN:
                Mockito.when(adMock.doCampaignAbTesting()).thenReturn(true);
                Mockito.when(adMock.getCampaignAbTestingShare()).thenReturn(Double.MIN_VALUE);
                break;
            case CONTROL_FOR_CAMPAIGN:
                Mockito.when(adMock.doCampaignAbTesting()).thenReturn(true);
                Mockito.when(adMock.getCampaignAbTestingShare()).thenReturn(Double.MAX_VALUE);
                Mockito.when(adMock.getCampaignAbTestingControlShare()).thenReturn(Double.MAX_VALUE);
                break;
            case TEST_FOR_CAMPAIGN:
                Mockito.when(adMock.doCampaignAbTesting()).thenReturn(true);
                Mockito.when(adMock.getCampaignAbTestingShare()).thenReturn(Double.MAX_VALUE);
                Mockito.when(adMock.getCampaignAbTestingControlShare()).thenReturn(Double.MIN_VALUE);
                break;
            default:
                throw new Exception("Unknown value of enum CampaignAbTesting");
        }
        
        switch (targetingStrategyAbTesting) {
            case NONE:
                Mockito.when(adMock.doTargetingStrategyAbTesting()).thenReturn(false);
                break;
            case CONTROL_FOR_TARGETING_STRATEGY:
                Mockito.when(adMock.doTargetingStrategyAbTesting()).thenReturn(true);
                Mockito.when(adMock.getAbTestingControlShare()).thenReturn(Double.MAX_VALUE);
                break;
            case TEST_FOR_TARGETING_STRATEGY:
                Mockito.when(adMock.doTargetingStrategyAbTesting()).thenReturn(true);
                Mockito.when(adMock.getAbTestingControlShare()).thenReturn(Double.MIN_VALUE);
                break;
            default:
                throw new Exception("Unknown value of enum TargetingStrategyAbTesting");
        }
        
        Mockito.when(adMock.doAbTesting())
            .thenReturn(campaignAbTesting != CampaignAbTesting.NONE || targetingStrategyAbTesting != TargetingStrategyAbTesting.NONE);
    }
    
    @Test
    public void returnsTrueIfIsNotUnderExperiment() throws Exception {
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        Map<String, Set<String>> bidsHistory = bidCandidateManager.getUserAttributes().getUserFrequencyCap().getBidsHistory();
        assertThat(bidsHistory.size(), equalTo(2));
        assertThat(bidsHistory.get(UserFrequencyCapAttributes.CAMPAIGN_PREFIX + adMock.getCampaignId()), not(equalTo(null)));
        assertThat(bidsHistory.get(UserFrequencyCapAttributes.TARGETING_STRATEGY_PREFIX + adMock.getId()), not(equalTo(null)));
        
        commonAssertions(result, true);
    }

    @Test
    public void returnsTrueIfIsNotUnderExperimentAndScheduleLostAuctionTaskThrowsException() throws Exception {
        multiple = false;
        
        PowerMockito.doThrow(new Lot49Exception()).when(LostAuctionTask.class);
        LostAuctionTask.scheduleLostAuctionTask(any(), any(), anyLong());
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.error(isA(Lot49Exception.class));
        
        commonAssertions(result, true);
    }

    @Test
    public void returnsTrueIfIsUnderExperimentAndNotExperimentForCampaign() throws Exception {
        bidCandidateManager.putUnderExperiment();
        configureAdmock(adMock, CampaignAbTesting.NOT_EXPERIMENT_FOR_CAMPAIGN, TargetingStrategyAbTesting.NONE);
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        UserExperimentAttributes experimentData = bidCandidateManager.getUserAttributes().getUserExperimentData();
        assertThat(experimentData.getStatusForCampaign(adMock), equalTo(UserExperimentAttributes.NOT_EXPERIMENT));
        
        commonAssertions(result, true);
    }

    @Test
    public void returnsFalseIfIsUnderExperimentAndControlForCampaign() throws Exception {
        bidCandidateManager.putUnderExperiment();
        configureAdmock(adMock, CampaignAbTesting.CONTROL_FOR_CAMPAIGN, TargetingStrategyAbTesting.NONE);
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        UserExperimentAttributes experimentData = bidCandidateManager.getUserAttributes().getUserExperimentData();
        assertThat(experimentData.getStatusForCampaign(adMock), equalTo(UserExperimentAttributes.CONTROL));
        
        commonAssertions(result, false);
    }

    @Test
    public void returnsTrueIfIsUnderExperimentAndTestForCampaign() throws Exception {
        bidCandidateManager.putUnderExperiment();
        configureAdmock(adMock, CampaignAbTesting.TEST_FOR_CAMPAIGN, TargetingStrategyAbTesting.NONE);
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        UserExperimentAttributes experimentData = bidCandidateManager.getUserAttributes().getUserExperimentData();
        assertThat(experimentData.getStatusForCampaign(adMock), equalTo(UserExperimentAttributes.TEST));
        
        commonAssertions(result, true);
    }

    @Test
    public void returnsFalseIfIsUnderExperimentAndControlForTargetingStrategy() throws Exception {
        bidCandidateManager.putUnderExperiment();
        configureAdmock(adMock, CampaignAbTesting.NONE, TargetingStrategyAbTesting.CONTROL_FOR_TARGETING_STRATEGY);
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        UserExperimentAttributes experimentData = bidCandidateManager.getUserAttributes().getUserExperimentData();
        assertThat(experimentData.getStatusForCampaign(adMock), equalTo(UserExperimentAttributes.NOT_EXPERIMENT));
        assertThat(experimentData.getStatusForTargetingStrategy(adMock), equalTo(UserExperimentAttributes.CONTROL));
        
        commonAssertions(result, false);
    }

    @Test
    public void returnsTrueIfIsUnderExperimentAndTestForTargetingStrategy() throws Exception {
        bidCandidateManager.putUnderExperiment();
        configureAdmock(adMock, CampaignAbTesting.NONE, TargetingStrategyAbTesting.TEST_FOR_TARGETING_STRATEGY);
        
        boolean result = Whitebox.invokeMethod(bidCandidateManager, "postProcessBid", rtbRequest, adMock, bid, multiple); /// act
        
        UserExperimentAttributes experimentData = bidCandidateManager.getUserAttributes().getUserExperimentData();
        assertThat(experimentData.getStatusForCampaign(adMock), equalTo(UserExperimentAttributes.NOT_EXPERIMENT));
        assertThat(experimentData.getStatusForTargetingStrategy(adMock), equalTo(UserExperimentAttributes.TEST));
        
        commonAssertions(result, true);
    }

    private void commonAssertions(boolean result, boolean expectedResult) throws Lot49Exception {
        PowerMockito.verifyStatic(times(expectedResult ? 1 : 0));
        LostAuctionTask.scheduleLostAuctionTask(any(), any(), anyLong());
        
        PowerMockito.verifyStatic(times(expectedResult ? 1 : 0));
        LogUtils.trace(contains("Time to postprocess bid:"));
        
        assertThat(result, equalTo(expectedResult));
    }
}
