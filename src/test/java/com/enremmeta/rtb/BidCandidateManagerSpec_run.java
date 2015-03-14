package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.testexchange.Test2ExchangeAdapter;
import com.enremmeta.util.ServiceRunner;

import io.netty.util.concurrent.ScheduledFuture;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, BidCandidate.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class BidCandidateManagerSpec_run {
    private OpenRtbRequest rtbRequest = new OpenRtbRequest();
    private OpenRtbResponse rtbResponse = new OpenRtbResponse();
    private AsyncResponse asyncResponseMock = Mockito.mock(AsyncResponse.class);
    private long timeout = 10000L;
    
    private ExchangeAdapter<?, ?> exchangeAdapter = new Test2ExchangeAdapter();
    private BidCandidateManager bidCandidateManager;
    
    @Before
    public void setUp() throws Exception {
        Bidder bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(Mockito.mock(AdCache.class)).when(bidderMock).getAdCache();
        
        rtbRequest.setImp(new ArrayList<Impression>());
        rtbRequest.getLot49Ext().setAdapter(exchangeAdapter);
        
        bidCandidateManager = new BidCandidateManager(rtbRequest, rtbResponse, asyncResponseMock, timeout);
        bidCandidateManager.setFutureSelf(Mockito.mock(ScheduledFuture.class));
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void negativeFlow_doesNotCallAsyncResponseResumeIfMethodIsAlreadyRunning() {
        AtomicBoolean running = Whitebox.getInternalState(bidCandidateManager, "running");
        running.set(true); /// method is already running
        
        bidCandidateManager.run(); /// act
        
        Mockito.verify(asyncResponseMock, never()).resume(any());

        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Already running, returning."));
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Done."));
    }
    
    @Test
    public void negativeFlow_callsAsyncResponseResumeWithStatusInternalServerErrorIfCodeThrowsException() {
        rtbRequest.getLot49Ext().setAdapter(null); /// this will cause exception
        
        bidCandidateManager.run(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.error(contains("Unexpected error"), any());
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("AsyncResp resumed with Error."));
        
        commonAssertions(Status.INTERNAL_SERVER_ERROR);
    }
    
    @Test
    public void positiveFlow_callsAsyncResponseResumeWithStatusNoContentIfThereAreNoBidsForRequest() {
        bidCandidateManager.run(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("No bid for " + rtbRequest.getId()));
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("AsyncResp resumed with no content."));
        
        commonAssertions(Status.NO_CONTENT);
    }
    
    @Test
    public void positiveFlow_callsAsyncResponseResumeWithStatusOkIfThereAreBidsForRequest() {
        BidCandidate bidCandidate1 = PowerMockito.mock(BidCandidate.class);
        PowerMockito.when(bidCandidate1.passed()).thenReturn(true);
        
        BidCandidate bidCandidate2 = PowerMockito.mock(BidCandidate.class);
        PowerMockito.when(bidCandidate2.failed()).thenReturn(true);
        
        bidCandidateManager.add(bidCandidate1);
        bidCandidateManager.add(bidCandidate2);
        
        rtbRequest.getLot49Ext().setNoBid(false); /// simulation of bids presence
        
        bidCandidateManager.run(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.logResponse(eq(rtbRequest), eq(rtbResponse), any());

        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Done: 2/2; passed: 1; failed: 1;"));
        
        PowerMockito.verifyStatic();
        LogUtils.trace(contains("AsyncResp resumed with response."));
        
        commonAssertions(Status.OK);
    }
    
    @Test
    public void negativeFlow_callsAsyncResponseResumeWithStatusInternalServerErrorIfThereAreBidsButErrorConvertingResponseOccurs() throws Throwable {
        ExchangeAdapter<?, ?> exchangeAdapterSpy = Mockito.spy(exchangeAdapter);
        Mockito.doThrow(Exception.class).when(exchangeAdapterSpy).convertResponse(rtbRequest, rtbResponse);
        rtbRequest.getLot49Ext().setAdapter(exchangeAdapterSpy);

        rtbRequest.getLot49Ext().setNoBid(false); /// simulation of bids presence
        
        bidCandidateManager.run(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.error(contains("Error converting response, this sucks."), any());

        PowerMockito.verifyStatic();
        LogUtils.trace(contains("AsyncResp resumed with Error."));
        
        commonAssertions(Status.INTERNAL_SERVER_ERROR);
    }
    
    private void commonAssertions(Status expectedResponseStatus) {
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        Mockito.verify(asyncResponseMock).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(expectedResponseStatus));
        
        PowerMockito.verifyStatic();
        LogUtils.logRequest(eq(rtbRequest), eq(false), anyInt());

        PowerMockito.verifyStatic();
        LogUtils.trace(contains("Done."));
    }
}
