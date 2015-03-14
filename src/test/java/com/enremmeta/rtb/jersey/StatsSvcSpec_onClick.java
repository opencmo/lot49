package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import javax.servlet.http.HttpServletRequest;
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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onClick {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = new Lot49Config(); // Mockito.mock(Lot49Config.class);

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

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_MalformedCookie() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        svc.click(Mockito.mock(UriInfo.class), "xch", "ssp", "cid", "crid", "bid", "brid", "iid",
                        "redir", "Cookie", "referer", "user-agent", "x-forwarded-for",
                        Mockito.mock(HttpServletRequest.class), "x-real-ip", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));

    }

    @Test
    public void negativeFlow_CorrectCookieWithoutUserIdCookie() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());
        LogUtils.logClick(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());

        svc.click(Mockito.mock(UriInfo.class), "xch", "ssp", "cid", "crid", "bid", "brid", "iid",
                        "redir", "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT",
                        "referer", "user-agent", "x-forwarded-for",
                        Mockito.mock(HttpServletRequest.class), "x-real-ip", "nodeId");

        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));
        PowerMockito.verifyStatic(Mockito.times(2));
        LogUtils.logClick(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());

    }

    @Test
    public void negativeFlow_UserIdCookieProvidedButWithBadFormat() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());
        LogUtils.logClick(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());


        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);

        PowerMockito.doReturn("userIdCookieValue").when(svcMock).getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.click(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any()))
                        .thenCallRealMethod();

        // exception: Corrupted user ID userIdCookieValue: length is 17; expected length between 22
        // and 25
        try {
            svcMock.click(Mockito.mock(UriInfo.class), "xch", "ssp", "cid", "crid", "bid", "brid",
                            "iid", "redir",
                            "userIdCookie=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "referer",
                            "user-agent", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                            "x-real-ip", "nodeId");

            fail("should throw exception");
        } catch (Exception e) {
        }

    }

    @Test
    public void positiveFlow_UserIdCookieProvidedNotEncoded_redirToOtherURL() throws Throwable {


        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);

        PowerMockito.doReturn("odsp=userIdCookieValue").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.click(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any()))
                        .thenCallRealMethod();


        Response response = (Response) svcMock.click(Mockito.mock(UriInfo.class), "xch", "ssp",
                        "cid", "crid", "bid", "brid", "iid", "redirToOtherURL", null, // not needed
                                                                                      // Cookie
                                                                                      // parameter
                                                                                      // because
                                                                                      // getMyCookie
                                                                                      // was mocked
                        "referer", "user-agent", "x-forwarded-for",
                        Mockito.mock(HttpServletRequest.class), "x-real-ip", "nodeId");


        assertEquals("redirToOtherURL", response.getHeaders().get("Location").get(0).toString());
        assertEquals("[no-cache, no-transform]",
                        response.getHeaders().get("Cache-Control").toString());
        assertEquals("ctscid",
                        ((NewCookie) response.getHeaders().get("Set-Cookie").get(0)).getName());
        assertEquals(2, ((NewCookie) response.getHeaders().get("Set-Cookie").get(0)).getVersion());
    }


    @Test
    public void positiveFlow_UserIdCookieProvidedNotEncoded_noRedirToOtherURL() throws Throwable {


        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);

        PowerMockito.doReturn("odsp=userIdCookieValue").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.click(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any()))
                        .thenCallRealMethod();


        Response response = (Response) svcMock.click(Mockito.mock(UriInfo.class), "xch", "ssp",
                        "cid", "crid", "bid", "brid", "iid", null, // no redirect
                        null, // not needed Cookie parameter because getMyCookie was mocked
                        "referer", "user-agent", "x-forwarded-for",
                        Mockito.mock(HttpServletRequest.class), "x-real-ip", "nodeId");


        assertEquals(200, response.getStatus());
        assertEquals("[image/gif]", response.getHeaders().get("Content-Type").toString());
        assertEquals("[no-cache, no-transform]",
                        response.getHeaders().get("Cache-Control").toString());
    }


    @Test
    public void positiveFlow_UserIdCookieProvidedButWithGoodFormat() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.logClick(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any());


        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);

        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.click(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any(), any()))
                        .thenCallRealMethod();


        Response response = (Response) svcMock.click(Mockito.mock(UriInfo.class), "xch", "ssp",
                        "cid", "crid", "bid", "brid", "iid", null,
                        "userIdCookie=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "referer",
                        "user-agent", "x-forwarded-for", Mockito.mock(HttpServletRequest.class),
                        "x-real-ip", "nodeId");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logClick(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), Mockito.eq("21ABC7BA48A2A8D06EA9559EE3B75D7B"), any(), any(),
                        any(), any(), any());

        assertEquals(200, response.getStatus());

    }
}
