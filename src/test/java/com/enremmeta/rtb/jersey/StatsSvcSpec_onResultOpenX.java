package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisBidInfoDaoShortLivedMap;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.proto.openx.AuctionResult;
import com.enremmeta.rtb.proto.openx.AuctionResultMessage;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class, LocalOrchestrator.class,
                BidInFlightInfo.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onResultOpenX {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;
    private AdCache adCacheMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        AdCacheConfig adCacheConfigMarker = new AdCacheConfig();
        adCacheConfigMarker.setPacing(new PacingServiceConfig());

        RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        redisServiceConfig.setHost("221.34.157.44");
        redisServiceConfig.setPort(3000);

        adCacheConfigMarker.getPacing().setRedis(redisServiceConfig);

        AdCache acMarker = new AdCache(adCacheConfigMarker);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(acMarker);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        adCacheMock = Mockito.mock(AdCache.class);

        RedisBidInfoDaoShortLivedMap mapMock = Mockito.mock(RedisBidInfoDaoShortLivedMap.class);
        Mockito.when(mapMock.getAsync(any())).thenReturn(Mockito.mock(Future.class));
        Mockito.when(mapMock.replace(any(), any()))
                        .thenReturn(PowerMockito.mock(BidInFlightInfo.class));

        Mockito.when(adCacheMock.getBidInFlightInfoMap()).thenReturn(mapMock);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adCacheMock);

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_NullInAuctionResults() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());


        Response response = (Response) svc.resultsOpenX(Mockito.mock(UriInfo.class), "Cookie",
                        new AuctionResultMessage(), "xff", Mockito.mock(HttpServletRequest.class),
                        "xrip", Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error"), Mockito.any(NullPointerException.class));

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

    @Test
    public void negativeFlow_AuctionResultsAreEmpty() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        AuctionResultMessage arm = new AuctionResultMessage();
        List<AuctionResult> results = new LinkedList<AuctionResult>();
        arm.setResults(results);

        Response response = (Response) svc.resultsOpenX(Mockito.mock(UriInfo.class), "Cookie", arm,
                        "xff", Mockito.mock(HttpServletRequest.class), "xrip",
                        Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(any());

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

    @Test
    public void negativeFlow_AuctionResultsHasIllegalFormattedPriceMicrosStr() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        AuctionResultMessage arm = new AuctionResultMessage();
        List<AuctionResult> results = new LinkedList<AuctionResult>() {
            {
                add(new AuctionResult() {
                    {
                        setStatus(AuctionResult.STATUS_WIN);
                    }
                });
            }
        };
        arm.setResults(results);

        Response response = (Response) svc.resultsOpenX(Mockito.mock(UriInfo.class), "Cookie", arm,
                        "xff", Mockito.mock(HttpServletRequest.class), "xrip",
                        Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(Mockito.eq("Error"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void negativeFlow_AdIsNull() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        AuctionResultMessage arm = new AuctionResultMessage();
        List<AuctionResult> results = new LinkedList<AuctionResult>() {
            {
                add(new AuctionResult() {
                    {
                        setStatus(AuctionResult.STATUS_WIN);
                        setClearingPriceMicros("1000000000");
                    }
                });
            }
        };
        arm.setResults(results);
        arm.setAuctionId("auctionId");

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http//for.rtb_test.com"));


        Response response = (Response) svc.resultsOpenX(uriInfoMock, "Cookie", arm, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip",
                        Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Unknown Ad ID: null"));

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

    @Test
    public void positiveFlow_AdNotNull() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), Mockito.any(HttpServletRequest.class), any(),
                        Mockito.anyLong(), any(), any(), Mockito.anyBoolean(), any(), any(), any(),
                        any(), any(), any(), Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        AuctionResultMessage arm = new AuctionResultMessage();
        List<AuctionResult> results = new LinkedList<AuctionResult>() {
            {
                add(new AuctionResult() {
                    {
                        setStatus(AuctionResult.STATUS_WIN);
                        setClearingPriceMicros("1000000000");
                    }
                });
            }
        };
        arm.setResults(results);
        arm.setAuctionId("auctionId");

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http//for.rtb_test.com"));

        Ad ad = new SharedSetUp.Ad_1001001_mock();
        Mockito.when(adCacheMock.getAd(any())).thenReturn(ad);

        Response response = (Response) svc.resultsOpenX(uriInfoMock, "Cookie", arm, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip",
                        Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), Mockito.any(HttpServletRequest.class), any(),
                        Mockito.anyLong(), any(), any(), Mockito.anyBoolean(), any(), any(), any(),
                        any(), any(), any(), Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

    @Test
    public void positiveFlow_AuctionResultSTATUSLOSS() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.logLost(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());

        AuctionResultMessage arm = new AuctionResultMessage();
        List<AuctionResult> results = new LinkedList<AuctionResult>() {
            {
                add(new AuctionResult() {
                    {
                        setStatus(AuctionResult.STATUS_LOSS);
                        setLossReason("lossReason");
                        setClearingPriceMicros("1000000000");
                    }
                });
            }
        };
        arm.setResults(results);
        arm.setAuctionId("auctionId");

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http//for.rtb_test.com"));

        Ad ad = new SharedSetUp.Ad_1001001_mock();
        Mockito.when(adCacheMock.getAd(any())).thenReturn(ad);

        Response response = (Response) svc.resultsOpenX(uriInfoMock, "Cookie", arm, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip",
                        Mockito.mock(HttpHeaders.class));


        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logLost(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());

        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

}
