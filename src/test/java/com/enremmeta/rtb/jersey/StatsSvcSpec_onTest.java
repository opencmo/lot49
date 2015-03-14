package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

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
public class StatsSvcSpec_onTest {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = new Lot49Config();

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

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        Response response = (Response) svc.test(Mockito.mock(UriInfo.class), "Cookie", "user-agent",
                        "cId", Mockito.mock(HttpHeaders.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Malformed cookie: Cookie in Cookie"));

        assertEquals(200, response.getStatus());
        assertEquals("image/gif", response.getMediaType().toString());
        assertEquals("{Mock for NewCookie=Mock for NewCookie=;Version=1}",
                        response.getCookies().toString());

    }

    @Test
    public void positiveFlow_MockedGoodCookie() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());

        PowerMockito.whenNew(NewCookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(NewCookie.class));
        PowerMockito.whenNew(Cookie.class).withAnyArguments()
                        .thenReturn(Mockito.mock(Cookie.class));

        StatsSvc svcMock = PowerMockito.mock(StatsSvc.class);

        PowerMockito.doReturn("userIdCookieValue123456").when(svcMock)
                        .getMyCookie(Mockito.anyString());
        PowerMockito.when(svcMock.test(any(), any(), any(), any(), any())).thenCallRealMethod();

        Response response = (Response) svcMock.test(Mockito.mock(UriInfo.class), "Cookie",
                        "user-agent", "cId", Mockito.mock(HttpHeaders.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.eq(
                        "test: In /test\n\tHeaders: null\n\tMy cookie: userIdCookieValue123456"));

        assertEquals(200, response.getStatus());
        assertEquals("image/gif", response.getMediaType().toString());
        assertEquals("{}", response.getCookies().toString());

    }


}
