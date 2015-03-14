package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BidCandidateManager.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class BidCandidateSpec_PLAIN {
    private BidCandidateManager bidCandidateManagerMock = PowerMockito.mock(BidCandidateManager.class);
    private Ad adMock;
    private Bid bidMock = Mockito.mock(Bid.class);
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void constructor_initializesFields() {
        boolean needUserInfo = false;
        boolean needIntegralInfo = false;
        boolean needExperimentInfo = false;
        boolean needFrequencyCap = false;
        
        adMock = BidCandidateSpec_SHARED.createAdMock(needUserInfo, needIntegralInfo, needExperimentInfo, needFrequencyCap);
        
        Mockito.when(bidCandidateManagerMock.getBidRequest())
            .thenReturn(new OpenRtbRequest());
        
        BidCandidate bidCandidate = new BidCandidate(bidCandidateManagerMock, adMock, bidMock); /// act
        
        assertThat(bidCandidate.getBcMgr(), equalTo(bidCandidateManagerMock));
        assertThat(bidCandidate.getAd(), equalTo(adMock));
        assertThat(bidCandidate.getBid(), equalTo(bidMock));
        
        assertThat(bidCandidate.isUserInfoCompleted(), equalTo(!needUserInfo));
        assertThat(bidCandidate.isIntegralCompleted(), equalTo(!needIntegralInfo));
        assertThat(bidCandidate.isExperimentInfoCompleted(), equalTo(!needExperimentInfo));
        assertThat(bidCandidate.isFcInfoCompleted(), equalTo(!needFrequencyCap));
    }
    
    @Test
    public void toString_returnsExpectedResult() {
        adMock = BidCandidateSpec_SHARED.createAdMock(false, false, false, false);
        
        Mockito.when(bidCandidateManagerMock.getBidRequest())
            .thenReturn(new OpenRtbRequest());
        
        BidCandidate bidCandidate = new BidCandidate(bidCandidateManagerMock, adMock, bidMock);
        
        bidCandidate.setUserInfoCompleted(false); /// NOT DONE
        bidCandidate.setExperimentInfoCompleted(true); /// DONE
        
        bidCandidate.setIntegralCompleted(true);
        Whitebox.setInternalState(bidCandidate, "integralPassed", true); /// PASSED
        
        bidCandidate.setFcInfoCompleted(true);
        Whitebox.setInternalState(bidCandidate, "fcPassed", false); /// FAILED
        
        String result = bidCandidate.toString(); /// act
        
        String expectedResult = "BidCandidate<Ad: " + adMock + "; bid: " + bidMock + 
                        "; userSegments: NOT DONE; Integral: PASSED; FC: FAILED; Experiment: DONE>";
        
        assertThat(result, equalTo(expectedResult));
    }
}
