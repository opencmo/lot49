package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.BidCandidateSpec_SHARED.StatusOfCheck;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BidCandidateManager.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class BidCandidateSpec_passed {
    private BidCandidateManager bidCandidateManagerMock = PowerMockito.mock(BidCandidateManager.class);
    private Ad adMock;
    private Bid bidMock = Mockito.mock(Bid.class);
    private BidCandidate bidCandidate;
    private BidCandidateSpec_SHARED shared;
    
    @Before
    public void setUp() throws Exception {
        Mockito.when(bidCandidateManagerMock.getBidRequest()).thenReturn(
                        new OpenRtbRequest());
        adMock = BidCandidateSpec_SHARED.createAdMock(false, false, false, false); /// all checks are completed
        bidCandidate = new BidCandidate(bidCandidateManagerMock, adMock, bidMock);
        shared = new BidCandidateSpec_SHARED(bidCandidate);
    }
    
    @Test
    public void returnsTrueIfDoneAndPassedIsTrue() {
        Whitebox.setInternalState(bidCandidate, "doneAndPassed", true);
        
        boolean result = bidCandidate.passed(); /// act
        
        commonAssertions(result, true);
    }
    
    @Test
    public void returnsFalseIfDoneAndFailedIsTrue() {
        Whitebox.setInternalState(bidCandidate, "doneAndFailed", true);
        
        boolean result = bidCandidate.passed(); /// act
        
        commonAssertions(result, false);
    }
    
    @Test
    public void returnsTrueIfNeedCanBid2ReturnsFalse() {
        Ad adMock = bidCandidate.getAd();
        Mockito.when(adMock.needCanBid2()).thenReturn(false);

        boolean result = bidCandidate.passed(); /// act
        
        commonAssertions(result, true);
    }
    
    @Test
    public void returnsTrueIfAllChecksWerePassed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        commonAssertions(result, true);
    }
    
    // Check of user info. All other checks were passed.
    
    @Test
    public void returnsFalseIfUserInfoCheckWasFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_FAILED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(result, is(false));
    }
    
    @Test
    public void returnsTrueIfUserInfoCheckWillBePassed() throws Exception {
        shared.configureTest(StatusOfCheck.WILL_BE_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isUserInfoCompleted(), is(true));
        commonAssertions(result, true);
    }
    
    @Test
    public void returnsFalseIfUserInfoCheckWillBeFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WILL_BE_FAILED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isUserInfoCompleted(), is(true));
        commonAssertions(result, false);
    }
    
    @Test
    public void returnsFalseIfUserInfoCheckWillThrowException() throws Exception {
        shared.configureTest(StatusOfCheck.WILL_THROW_EXCEPTION, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Error getting User segments data for "));

        assertThat(bidCandidate.isUserInfoCompleted(), is(false));
        commonAssertions(result, false);
    }
    
    // Check of integral info. All other checks were passed.
    
    @Test
    public void returnsFalseIfIntegralInfoCheckWasFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_FAILED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(result, is(false));
    }
    
    @Test
    public void returnsTrueIfIntegralInfoCheckWillBePassed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isIntegralCompleted(), is(true));
        commonAssertions(result, true);
    }
    
    @Test
    public void returnsFalseIfIntegralInfoCheckWillBeFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_FAILED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isIntegralCompleted(), is(true));
        commonAssertions(result, false);
    }
    
    // Check of experiment info. All other checks were passed.
    
    @Test
    public void returnsFalseIfExperimentInfoCheckWasFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_FAILED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(result, is(false));
    }
    
    @Test
    public void returnsTrueIfExperimentInfoCheckWillBePassed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_PASSED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isExperimentInfoCompleted(), is(true));
        commonAssertions(result, true);
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void returnsFalseIfExperimentInfoCheckWillBeFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_FAILED, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        // TODO in BidCandidate.passed(): assertions below are never true simultaneously (corresponding code is absent)
        assertThat(Whitebox.getInternalState(bidCandidate, "experimentPassed"), is(false));
        assertThat(bidCandidate.isExperimentInfoCompleted(), is(true));
        assertThat(result, is(false));
    }
    
    @Test
    public void returnsFalseIfExperimentInfoCheckWillThrowException() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_THROW_EXCEPTION, StatusOfCheck.WAS_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Error getting Experiment data for "));

        assertThat(bidCandidate.isExperimentInfoCompleted(), is(false));
        commonAssertions(result, false);
    }
    
    // Check of frequency cap info. All other checks were passed.
    
    @Test
    public void returnsFalseIfFrequencyCapInfoCheckWasFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_FAILED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(result, is(false));
    }
    
    @Test
    public void returnsTrueIfFrequencyCapInfoCheckWillBePassed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_PASSED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isFcInfoCompleted(), is(true));
        commonAssertions(result, true);
    }
    
    @Test
    public void returnsFalseIfFrequencyCapInfoCheckWillBeFailed() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_BE_FAILED);
        
        boolean result = bidCandidate.passed(); /// act
        
        assertThat(bidCandidate.isFcInfoCompleted(), is(true));
        commonAssertions(result, false);
    }
    
    @Test
    public void returnsFalseIfFrequencyCapInfoCheckWillThrowException() throws Exception {
        shared.configureTest(StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WAS_PASSED, StatusOfCheck.WILL_THROW_EXCEPTION);
        
        boolean result = bidCandidate.passed(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.debug(contains("Error getting Frequency Cap data for "));

        assertThat(bidCandidate.isFcInfoCompleted(), is(false));
        commonAssertions(result, false);
    }

    private void commonAssertions(boolean result, boolean expectedResult) {
        assertThat(bidCandidate.isDone(), is(true));
        assertThat(bidCandidate.isDoneAndPassed(), is(expectedResult));
        assertThat(bidCandidate.isDoneAndFailed(), is(!expectedResult));
        assertThat(result, is(expectedResult));
    }
}
