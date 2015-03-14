package com.enremmeta.rtb;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.util.concurrent.Future;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.UserAttributes;
import com.enremmeta.rtb.api.UserSegments;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoReceived;

public class BidCandidateSpec_SHARED {
    
    public static enum StatusOfCheck { /// prefix "WAS_" means "check is completed", "WILL_" - "check is not completed"
        WAS_PASSED,
        WAS_FAILED,
        WILL_BE_PASSED,
        WILL_BE_FAILED,
        WILL_THROW_EXCEPTION
    }
    
    private BidCandidate bidCandidate;
    
    public BidCandidateSpec_SHARED(BidCandidate bidCandidate) {
        this.bidCandidate = bidCandidate;
    }
    
    public static Ad createAdMock(boolean needUserInfo, boolean needIntegralInfo, boolean needExperimentInfo, boolean needFrequencyCap) {
        Ad adMock = Mockito.mock(Ad.class);
        
        Mockito.doReturn(needUserInfo).when(adMock).needUserInfo();
        Mockito.doReturn(needIntegralInfo).when(adMock).needIntegralInfo();
        Mockito.doReturn(needExperimentInfo).when(adMock).needExperimentInfo();
        Mockito.doReturn(needFrequencyCap).when(adMock).needFrequencyCap();
        
        Mockito.doReturn(true).when(adMock).needCanBid2();
        
        return adMock;
    }
    
    public static OpenRtbRequest createBidRequest() {
        return new OpenRtbRequest();
    }
    
    public void configureTest(StatusOfCheck userInfoStatus, StatusOfCheck integralInfoStatus, StatusOfCheck experimentInfoStatus, StatusOfCheck frequencyCapInfoStatus) throws Exception {
        userInfoConfig(userInfoStatus);
        integralInfoConfig(integralInfoStatus);
        experimentInfoConfig(experimentInfoStatus);
        frequencyCapInfoConfig(frequencyCapInfoStatus);
    }
    
    private void userInfoConfig(StatusOfCheck status) throws Exception {
        switch (status) {
            case WAS_PASSED:
                bidCandidate.setUserInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "segmentsPassed", true);
                break;
            case WAS_FAILED:
                bidCandidate.setUserInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "segmentsPassed", false);
                break;
            case WILL_BE_PASSED:
            case WILL_BE_FAILED:
            case WILL_THROW_EXCEPTION:
                bidCandidate.setUserInfoCompleted(false);
                
                OpenRtbRequest bidRequest = createBidRequest();
                
                @SuppressWarnings("unchecked")
                Future<UserSegments> futureMock = Mockito.mock(Future.class);
                Mockito.when(futureMock.isDone()).thenReturn(true);
                
                BidCandidateManager bidCandidateManagerMock = bidCandidate.getBcMgr();
                Mockito.when(bidCandidateManagerMock.getBidRequest()).thenReturn(bidRequest);
                Mockito.when(bidCandidateManagerMock.getUserSegmentsFuture()).thenReturn(futureMock);
                
                Ad adMock = bidCandidate.getAd();
                
                if (status == StatusOfCheck.WILL_BE_PASSED) {
                    Mockito.when(adMock.checkSegments(eq(bidRequest), any())).thenReturn(true);
                } else if (status == StatusOfCheck.WILL_BE_FAILED) {
                    Mockito.when(adMock.checkSegments(eq(bidRequest), any())).thenReturn(false);
                } else if (status == StatusOfCheck.WILL_THROW_EXCEPTION) {
                    PowerMockito.mockStatic(LogUtils.class);
                    Mockito.when(futureMock.get()).thenThrow(new RuntimeException());
                }
                
                break;
            default:
                throw new UnknownStatusException("Unknown status of check for user info");
        }
    }
    
    private void integralInfoConfig(StatusOfCheck status) throws Exception {
        switch (status) {
            case WAS_PASSED:
                bidCandidate.setIntegralCompleted(true);
                Whitebox.setInternalState(bidCandidate, "integralPassed", true);
                break;
            case WAS_FAILED:
                bidCandidate.setIntegralCompleted(true);
                Whitebox.setInternalState(bidCandidate, "integralPassed", false);
                break;
            case WILL_BE_PASSED:
            case WILL_BE_FAILED:
                bidCandidate.setIntegralCompleted(false);
                
                OpenRtbRequest bidRequest = createBidRequest();
                IntegralInfoReceived integralInfoReceived = new IntegralInfoReceived();
                integralInfoReceived.setCompleted(true);
                
                BidCandidateManager bidCandidateManagerMock = bidCandidate.getBcMgr();
                Mockito.when(bidCandidateManagerMock.getBidRequest()).thenReturn(bidRequest);
                Mockito.when(bidCandidateManagerMock.getIntegralInfoReceived()).thenReturn(integralInfoReceived);
                
                Ad adMock = bidCandidate.getAd();
                
                if (status == StatusOfCheck.WILL_BE_PASSED) {
                    Mockito.when(adMock.checkIntegralTargeting(eq(bidRequest), any(), any())).thenReturn(true);
                } else if (status == StatusOfCheck.WILL_BE_FAILED) {
                    Mockito.when(adMock.checkIntegralTargeting(eq(bidRequest), any(), any())).thenReturn(false);
                }
                
                break;
            default:
                throw new UnknownStatusException("Unknown status of check for integral info");
        }
    }
    
    private void experimentInfoConfig(StatusOfCheck status) throws Exception {
        switch (status) {
            case WAS_PASSED:
                bidCandidate.setExperimentInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "experimentPassed", true);
                break;
            case WAS_FAILED:
                bidCandidate.setExperimentInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "experimentPassed", false);
                break;
            case WILL_BE_PASSED:
            case WILL_BE_FAILED:
            case WILL_THROW_EXCEPTION:
                bidCandidate.setExperimentInfoCompleted(false);
                
                OpenRtbRequest bidRequest = createBidRequest();
                
                @SuppressWarnings("unchecked")
                Future<UserAttributes> futureMock = Mockito.mock(Future.class);
                
                BidCandidateManager bidCandidateManagerMock = bidCandidate.getBcMgr();
                Mockito.when(bidCandidateManagerMock.getBidRequest()).thenReturn(bidRequest);
                Mockito.when(bidCandidateManagerMock.getUserAttributesFuture()).thenReturn(futureMock);
                
                if (status == StatusOfCheck.WILL_BE_PASSED) {
                    Mockito.when(futureMock.isDone()).thenReturn(true);
                    Mockito.when(futureMock.get()).thenReturn(Mockito.mock(UserAttributes.class));
                } else if (status == StatusOfCheck.WILL_BE_FAILED) {
                    Mockito.when(futureMock.isDone()).thenReturn(false);
                } else if (status == StatusOfCheck.WILL_THROW_EXCEPTION) {
                    PowerMockito.mockStatic(LogUtils.class);
                    Mockito.when(futureMock.isDone()).thenReturn(true);
                    Mockito.when(futureMock.get()).thenThrow(new RuntimeException());
                }
                
                break;
            default:
                throw new UnknownStatusException("Unknown status of check for experiment info");
        }
    }
    
    private void frequencyCapInfoConfig(StatusOfCheck status) throws Exception {
        switch (status) {
            case WAS_PASSED:
                bidCandidate.setFcInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "fcPassed", true);
                break;
            case WAS_FAILED:
                bidCandidate.setFcInfoCompleted(true);
                Whitebox.setInternalState(bidCandidate, "fcPassed", false);
                break;
            case WILL_BE_PASSED:
            case WILL_BE_FAILED:
            case WILL_THROW_EXCEPTION:
                bidCandidate.setFcInfoCompleted(false);
                
                OpenRtbRequest bidRequest = createBidRequest();
                
                @SuppressWarnings("unchecked")
                Future<UserAttributes> futureMock = Mockito.mock(Future.class);
                Mockito.when(futureMock.isDone()).thenReturn(true);
                Mockito.when(futureMock.get()).thenReturn(Mockito.mock(UserAttributes.class));
                
                BidCandidateManager bidCandidateManagerMock = bidCandidate.getBcMgr();
                Mockito.when(bidCandidateManagerMock.getBidRequest()).thenReturn(bidRequest);
                Mockito.when(bidCandidateManagerMock.getUserAttributesFuture()).thenReturn(futureMock);
                
                Ad adMock = bidCandidate.getAd();
                
                if (status == StatusOfCheck.WILL_BE_PASSED) {
                    Mockito.when(adMock.checkFrequencyCap(eq(bidRequest), any())).thenReturn(true);
                } else if (status == StatusOfCheck.WILL_BE_FAILED) {
                    Mockito.when(adMock.checkFrequencyCap(eq(bidRequest), any())).thenReturn(false);
                } else if (status == StatusOfCheck.WILL_THROW_EXCEPTION) {
                    PowerMockito.mockStatic(LogUtils.class);
                    Mockito.when(futureMock.get()).thenThrow(new RuntimeException());
                }
                
                break;
            default:
                throw new UnknownStatusException("Unknown status of check for frequency cap info");
        }
    }

    @SuppressWarnings("serial")
    public static class UnknownStatusException extends Exception {
        public UnknownStatusException() {}
        public UnknownStatusException(String msg) { super(msg); }
    }
}
