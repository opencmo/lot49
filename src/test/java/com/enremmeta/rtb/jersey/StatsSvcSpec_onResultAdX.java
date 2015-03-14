package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
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
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.ServiceRunner;
import com.google.protos.adx.NetworkBid;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class, StatsSvc.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class StatsSvcSpec_onResultAdX {
    private ServiceRunner serviceRunnerSimpleMock;
    private StatsSvc svc;
    private Lot49Config configMock;

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

        svc = new StatsSvc();

    }

    @Test
    public void negativeFlow_UnknownFeedbackCode() throws Throwable {

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        NetworkBid.BidRequest.BidResponseFeedback fb =
                        NetworkBid.BidRequest.BidResponseFeedback.getDefaultInstance();
        Whitebox.setInternalState(fb, "creativeStatusCode_", 0);

        Response response = (Response) svc.resultAdX(Mockito.mock(AsyncResponse.class),
                        Mockito.mock(UriInfo.class), fb, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");


        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.eq("Unknown feedback code: 0"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    @Test
    public void positiveFlow_FeedbackCodeIs1() throws Throwable {

        NetworkBid.BidRequest.BidResponseFeedback fb =
                        NetworkBid.BidRequest.BidResponseFeedback.getDefaultInstance();
        Whitebox.setInternalState(fb, "creativeStatusCode_", 1);

        Response response = (Response) svc.resultAdX(Mockito.mock(AsyncResponse.class),
                        Mockito.mock(UriInfo.class), fb, "xff",
                        Mockito.mock(HttpServletRequest.class), "xrip");

        assertEquals(Status.OK.getStatusCode(), response.getStatus());

    }


}
