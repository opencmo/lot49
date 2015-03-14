package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.net.URI;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
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
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.impl.redis.RedisBidInfoDaoShortLivedMap;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.util.ServiceRunner;
import com.google.doubleclick.crypto.DoubleClickCrypto;;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class, AdXAdapter.class,
                LocalOrchestrator.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onImpression {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

    private long BID_CRETATED_TIMESTAMP = 1000L;

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

        // RedisService rService = new RedisService();
        // RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        // redisServiceConfig.setHost("221.34.157.44");
        // redisServiceConfig.setPort(3000);
        // Whitebox.setInternalState(rService, "config", redisServiceConfig);

        RedisBidInfoDaoShortLivedMap mapMock = Mockito.mock(RedisBidInfoDaoShortLivedMap.class);
        Mockito.when(mapMock.getAsync(any())).thenReturn(Mockito.mock(Future.class));

        Mockito.when(adCacheMock.getBidInFlightInfoMap()).thenReturn(mapMock);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adCacheMock);

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_badRequestURL() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(null);

        int nurl = 0;

        Response response =
                        (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_SPOTXCHANGE,

                                        "ssp", "wp", "cId", "crId", "bId", "iId", "brid", "bp",
                                        BID_CRETATED_TIMESTAMP, "redir", nurl, "forceCookieReset",
                                        "forceCookieResync", "referer", "Cookie", "user-agent",
                                        "custom", "x-forwarded-for",
                                        Mockito.mock(HttpServletRequest.class), "x-real-ip",
                                        Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing null"), Mockito.any(NullPointerException.class));

        assertEquals(500, response.getStatus());

    }

    @Test
    public void negativeFlow_badBidPrice() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));

        int nurl = 0;

        Response response =
                        (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_SPOTXCHANGE,

                                        "ssp", "wp", "cId", "crId", "bId", "iId", "brid", "bp",
                                        BID_CRETATED_TIMESTAMP, "redir", nurl, "forceCookieReset",
                                        "forceCookieResync", "referer", "Cookie", "user-agent",
                                        "custom", "x-forwarded-for",
                                        Mockito.mock(HttpServletRequest.class), "x-real-ip",
                                        Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing bid price from request: bp"),
                        Mockito.any(NumberFormatException.class));

        assertEquals(400, response.getStatus());

    }

    @Test
    public void negativeFlow_badWinningPrice() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), any());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));

        int nurl = 0;


        Response response =
                        (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_SPOTXCHANGE,

                                        "ssp", "wp", "cId", "crId", "bId", "iId", "brid", "10000",
                                        BID_CRETATED_TIMESTAMP, "redir", nurl, "forceCookieReset",
                                        "forceCookieResync", "referer", "Cookie", "user-agent",
                                        "custom", "x-forwarded-for",
                                        Mockito.mock(HttpServletRequest.class), "x-real-ip",
                                        Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Error parsing winning price from wp"),
                        Mockito.any(NumberFormatException.class));

        assertEquals(400, response.getStatus());

    }

    @Test
    public void positiveFlow_WinningAndRedir() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());



        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));

        int nurl = 0;

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));
        Mockito.when(serviceRunnerSimpleMock.getUserAttributesCacheService())
                        .thenReturn(Mockito.mock(DaoMapOfUserAttributes.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Response response = (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_ADX,

                        "ssp", "50000000000000000000000000000000000000", "cId", "crId", "bId",
                        "iId", "brid", "10000", BID_CRETATED_TIMESTAMP, "redir", nurl,
                        "forceCookieReset", "forceCookieResync", "referer", "Cookie", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());


        assertEquals(303, response.getStatus());
        assertEquals("redir", response.getLocation().toString());
    }

    @Test
    public void positiveFlow_WinningNoRedirInParameters() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());


        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        int nurl = 0;

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));
        Mockito.when(serviceRunnerSimpleMock.getUserAttributesCacheService())
                        .thenReturn(Mockito.mock(DaoMapOfUserAttributes.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);
        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());

        Response response = (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_ADX,

                        "ssp", "50000000000000000000000000000000000000", "cId", "crId", "bId",
                        "iId", "brid", "10000", BID_CRETATED_TIMESTAMP, null, nurl,
                        "forceCookieReset", "forceCookieResync", "referer", "Cookie", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        assertEquals(303, response.getStatus());
        assertEquals("https://test_site.comstats/proust?lot49version=v16&base=http%3A%2F%2Ftest_site.com&xch=adx&ssp=ssp&cid=cId&crid=crId&bid=bId&iid=iId&brid=brid&fcr=1&custom=custom&phase=rest",
                        response.getLocation().toString());
    }

    @Test
    public void positiveFlow_NoWinning() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logAccess(any());


        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));
        Mockito.when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://test_site.com"));

        int nurl = 1;

        DoubleClickCrypto.Price dccpMock = PowerMockito.mock(DoubleClickCrypto.Price.class);
        PowerMockito.when(dccpMock.decodePriceMicros(any())).thenReturn(1000L);
        PowerMockito.whenNew(DoubleClickCrypto.Price.class).withAnyArguments().thenReturn(dccpMock);

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(Mockito.mock(LocalOrchestrator.class));

        Mockito.when(serviceRunnerSimpleMock.getUserAttributesCacheService())
                        .thenReturn(Mockito.mock(DaoMapOfUserAttributes.class));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);
        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());

        Response response = (Response) svc.impression(uriInfoMock, Lot49Constants.EXCHANGE_ADX,

                        "ssp", "50000000000000000000000000000000000000", "cId", "crId", "bId",
                        "iId", "brid", "10000", BID_CRETATED_TIMESTAMP, null, nurl,
                        "forceCookieReset", "forceCookieResync", "referer", "Cookie", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", Mockito.mock(HttpHeaders.class), "buyerUid", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logImpression(any(), any(), any(), any(), any(), any(), Mockito.anyLong(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyInt(), any(), any(),
                        any(), any(), any(), any(), any(), any(), Mockito.anyBoolean(), any(),
                        any(), any(), Mockito.anyBoolean(), any(), any());

        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.logWin(any(), any(), any(), any(), any(), any(), any(), Mockito.anyDouble(),
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), any(), any(), any(), any(), any(), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean(), any());

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logAccess(any());

        assertEquals(303, response.getStatus());
        assertEquals("https://test_site.comstats/proust?lot49version=v16&base=http%3A%2F%2Ftest_site.com&xch=adx&ssp=ssp&cid=cId&crid=crId&bid=bId&iid=iId&brid=brid&fcr=1&custom=custom&phase=rest",
                        response.getLocation().toString());
    }

}
