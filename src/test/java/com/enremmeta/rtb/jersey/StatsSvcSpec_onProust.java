package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindFacade;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onProust {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        // configMock = new Lot49Config();
        configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        MaxMindFacade maxMindFacade = Mockito.mock(MaxMindFacade.class);
        Mockito.when(maxMindFacade.getGeo(SharedSetUp.IP_MARKER))
                        .thenReturn(SharedSetUp.GEO_MARKER);
        Mockito.when(serviceRunnerSimpleMock.getMaxMind()).thenReturn(maxMindFacade);

        AdCacheConfig adCacheConfigMarker = new AdCacheConfig();
        adCacheConfigMarker.setPacing(new PacingServiceConfig());

        RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        redisServiceConfig.setHost("221.34.157.44");
        redisServiceConfig.setPort(3000);

        adCacheConfigMarker.getPacing().setRedis(redisServiceConfig);

        AdCache acMarker = new AdCache(adCacheConfigMarker);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(acMarker);

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_MalformedCookie() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        svc.proust(Mockito.mock(UriInfo.class), "xch", "ssp", "cid", "crid", "bid", "iid", "brid",
                        "fcr", "phase", "referer", "Cookie", "user-agent", "custom",
                        "x-forwarded-for", Mockito.mock(HttpServletRequest.class), "x-real-ip",
                        "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));

    }

    @Test
    public void negativeFlow_CorrectCookieWithoutUserIdCookie_butNoCorrectPhase() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        Response response = (Response) svc.proust(Mockito.mock(UriInfo.class), "xch", "ssp", "cid",
                        "crid", "bid", "iid", "brid", "fcr", "phase", "referer",
                        "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Bad phase: phase", response.getEntity().toString());
    }

    @Test
    public void negativeFlow_nullPhase() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        Response response = (Response) svc.proust(Mockito.mock(UriInfo.class), "xch", "ssp", "cid",
                        "crid", "bid", "iid", "brid", "fcr", null, "referer",
                        "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Empty phase", response.getEntity().toString());
    }

    @Test
    public void positiveFlow_restPhase() throws Throwable {

        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));

        Response response = (Response) svc.proust(mockUriInfo, "xch", "ssp", "cid", "crid", "bid",
                        "iid", "brid", "fcr", "rest", "referer",
                        "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        SharedSetUp.IP_MARKER, "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
        assertEquals("https://www.test.com/gonull?lot49version=v16&base=http%3A%2F%2Fwww.test.com%2Fgo&xch=xch&ssp=ssp&cid=cid&crid=crid&bid=bid&iid=iid&brid=brid&fcr=fcr&custom=custom&phase=sync",
                        response.getLocation().toString());
        assertEquals("{}", response.getCookies().toString());
    }

    @Test
    public void positiveFlow_restPhase_fcrEqYES() throws Throwable {

        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Response response = (Response) svc.proust(mockUriInfo, "xch", "ssp", "cid", "crid", "bid",
                        "iid", "brid", "YES", "rest", "referer",
                        "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "user-agent",
                        "custom", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        SharedSetUp.IP_MARKER, "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
        assertEquals("https://www.test.com/gonull?lot49version=v16&base=http%3A%2F%2Fwww.test.com%2Fgo&xch=xch&ssp=ssp&cid=cid&crid=crid&bid=bid&iid=iid&brid=brid&fcr=YES&custom=custom&phase=sync",
                        response.getLocation().toString());
        assertEquals("{Mock for NewCookie=Mock for NewCookie=;Version=1}",
                        response.getCookies().toString());
    }


    @Test
    public void positiveFlow_syncPhase_nullCookie() throws Throwable {

        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);

        Response response = (Response) svc.proust(mockUriInfo, "xch", "ssp", "cid", "crid", "bid",
                        "iid", "brid", "YES", "sync", "referer", null, "user-agent", "custom",
                        "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        SharedSetUp.IP_MARKER, "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/gif", response.getMediaType().toString());
    }

    @Test
    public void positiveFlow_syncPhase_NotNullCookie() throws Throwable {

        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);

        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);
        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.proust(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any())).thenCallRealMethod();


        Response response = (Response) svcMock.proust(mockUriInfo,
                        Lot49Constants.EXCHANGE_SPOTXCHANGE, "ssp", "cid", "crid", "bid", "iid",
                        "brid", "YES", "sync", "referer", null, "user-agent", "custom",
                        "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        SharedSetUp.IP_MARKER, "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("image/gif", response.getMediaType().toString());
    }

    @Test
    public void positiveFlow_syncPhase_NotNullCookie_partnerInitiatedSyncURI() throws Throwable {

        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);

        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);
        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.proust(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any())).thenCallRealMethod();


        Response response = (Response) svcMock.proust(mockUriInfo, Lot49Constants.EXCHANGE_PUBMATIC,
                        "ssp", "cid", "crid", "bid", "iid", "brid", "YES", "sync", "referer", null,
                        "user-agent", "custom", "x-forwarded-for",
                        Mockito.mock(HttpServletRequest.class), SharedSetUp.IP_MARKER,
                        "Lot49Constants.LOT49_VERSION_KEY", "nodeId");

        assertEquals(Status.SEE_OTHER.getStatusCode(), response.getStatus());
        assertEquals("https://image2.pubmatic.com/AdServer/Pug?vcode=vcode&piggybackCookie=userIdCookieValue1234w",
                        response.getLocation().toString());

    }

}
