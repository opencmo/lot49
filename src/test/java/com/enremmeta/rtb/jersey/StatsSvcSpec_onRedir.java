package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.net.URI;

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
public class StatsSvcSpec_onRedir {
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
    public void negativeFlow_nullRedirect() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));

        Response response = (Response) svc.redir(uriInfoMock, null);

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Empty redirect."));

        assertEquals(400, response.getStatus());

    }

    @Test
    public void positiveFlow_goodRedirect() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logRedir(any(), any(), Mockito.anyInt());

        UriInfo uriInfoMock = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfoMock.getRequestUri()).thenReturn(new URI("http://test_site.com"));

        Response response = (Response) svc.redir(uriInfoMock, "http://redir_test_site.com");

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logRedir("http://test_site.com", "http://redir_test_site.com", 303);

        assertEquals(303, response.getStatus());

    }

}
