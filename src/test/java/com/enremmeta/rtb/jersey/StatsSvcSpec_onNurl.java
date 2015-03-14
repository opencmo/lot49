package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.net.URI;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.impl.redis.RedisBidInfoDaoShortLivedMap;
import com.enremmeta.rtb.dao.impl.redis.RedisStringDaoShortLivedMap;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.util.ServiceRunner;
import com.google.doubleclick.crypto.DoubleClickCrypto;;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class, AdXAdapter.class,
                LocalOrchestrator.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onNurl {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

    private long BID_CRETATED_TIMESTAMP = 1000L;
    private RedisStringDaoShortLivedMap mapMock2;

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

        AdCache adCacheMock = Mockito.mock(AdCache.class);

        RedisBidInfoDaoShortLivedMap mapMock = Mockito.mock(RedisBidInfoDaoShortLivedMap.class);
        Mockito.when(mapMock.getAsync(any())).thenReturn(Mockito.mock(Future.class));
        Mockito.when(adCacheMock.getBidInFlightInfoMap()).thenReturn(mapMock);

        mapMock2 = Mockito.mock(RedisStringDaoShortLivedMap.class);
        Mockito.when(mapMock2.get(KVKeysValues.NURL_PREFIX + "nurlId")).thenReturn("_TEST_TAG");
        Mockito.when(adCacheMock.getNurlMap()).thenReturn(mapMock2);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adCacheMock);

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_ErrorParsingMediaType() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.warn(any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Response response = (Response) svc.nurl(uriInfoMock, Lot49Constants.EXCHANGE_ADX, "ssp",
                        "wrongContentType", "50000", "cId", "crId", "bId", "iId", "brId", "10000",
                        BID_CRETATED_TIMESTAMP, "nurlId", "ref", "cookies", "ua", "custom", "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "ct", "cte", "myct",
                        "nodeId", Lot49Constants.NURL_STANDART);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn("handleWinLossError at http://test_site.com (RequestID: brId, Reason: null, WinPrice: 1000): \tAd null not found.\n\t"
                        + "No ad ID found in BidInFlightInfo(cancelLostAuctionTask() BidInFlightInfo for key bidRequest_brId not found)\n\t"
                        + "In-flight information for key bidRequest_brId contains bid request ID null, expected brId, refund may be lost.\n\t"
                        + "In-flight information for key bidRequest_brId contains bid ID null, expected bId, refund may be lost.");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(500, response.getStatus());
    }

    @Test
    public void positiveFlow_ResponseOKWithTag() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.warn(any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Response response = (Response) svc.nurl(uriInfoMock, Lot49Constants.EXCHANGE_ADX, "ssp",
                        "text/xml", "50000", "cId", "crId", "bId", "iId", "brId", "10000",
                        BID_CRETATED_TIMESTAMP, "nurlId", "ref", "cookies", "ua", "custom", "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "ct", "cte", "myct",
                        "nodeId", Lot49Constants.NURL_STANDART);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn("handleWinLossError at http://test_site.com (RequestID: brId, Reason: null, WinPrice: 1000): \tAd null not found.\n\t"
                        + "No ad ID found in BidInFlightInfo(cancelLostAuctionTask() BidInFlightInfo for key bidRequest_brId not found)\n\t"
                        + "In-flight information for key bidRequest_brId contains bid request ID null, expected brId, refund may be lost.\n\t"
                        + "In-flight information for key bidRequest_brId contains bid ID null, expected bId, refund may be lost.");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(200, response.getStatus());
        assertEquals("_TEST_TAG", response.getEntity());
    }

    @Test
    public void positiveFlow_TagIsNull() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.warn(any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Mockito.when(mapMock2.get(KVKeysValues.NURL_PREFIX + "nurlId")).thenReturn(null);

        Response response = (Response) svc.nurl(uriInfoMock, Lot49Constants.EXCHANGE_ADX, "ssp",
                        "text/xml", "500000", "cId", "crId", "bId", "iId", "brId", "10000",
                        BID_CRETATED_TIMESTAMP, "nurlId", "ref", "cookies", "ua", "custom", "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "ct", "cte", "myct",
                        "nodeId", Lot49Constants.NURL_STANDART);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn("handleWinLossError at http://test_site.com (RequestID: brId, Reason: null, WinPrice: 1000): \tAd null not found.\n\t"
                        + "No ad ID found in BidInFlightInfo(cancelLostAuctionTask() BidInFlightInfo for key bidRequest_brId not found)\n\t"
                        + "In-flight information for key bidRequest_brId contains bid request ID null, expected brId, refund may be lost.\n\t"
                        + "In-flight information for key bidRequest_brId contains bid ID null, expected bId, refund may be lost.");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{Mock for NewCookie=Mock for NewCookie=;Version=1}",
                        response.getCookies().toString());
    }

    @Test
    public void positiveFlow_MacrosInNurlIsTrue() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logResponse(any(), any(), any(), any(), any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        // Mockito.when(mapMock2.get(KVKeysValues.NURL_PREFIX + "nurlId")).thenReturn(null);

        Response response = (Response) svc.nurl(uriInfoMock, Lot49Constants.EXCHANGE_TEST1, "ssp",
                        "text/xml", "500000", "cId", "crId", "bId", "iId", "brId", "10000",
                        BID_CRETATED_TIMESTAMP, "nurlId", "ref", "cookies", "ua", "custom", "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip", "ct", "cte", "myct",
                        "nodeId", Lot49Constants.NURL_STANDART);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logResponse(any(), any(), any(), any(), Mockito.eq("_TEST_TAG"));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{Mock for NewCookie=Mock for NewCookie=;Version=1}",
                        response.getCookies().toString());
    }
}
