package com.enremmeta.rtb.jersey;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.caches.CacheableWebResponse;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.dao.DaoShortLivedMap;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OpsSvc.class, ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class OpsSvcSpec_debugNurl {
    private ServiceRunner serviceRunnerSimpleMock;
    private Lot49Config configMock;
    private AdCache adCacheMock;
    private DaoShortLivedMap<CacheableWebResponse> cwrMapMock;
    private CacheableWebResponse cwrMock;
    private Response responseMock;
    private OpsSvc svc;
    private String nurlId = "123";

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(ServiceRunner.class);

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        configMock = Mockito.mock(Lot49Config.class);
        adCacheMock = Mockito.mock(AdCache.class);
        cwrMapMock = getDaoShortLivedMapMock();
        cwrMock = Mockito.mock(CacheableWebResponse.class);
        responseMock = Mockito.mock(Response.class);

        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        Mockito.when(serviceRunnerSimpleMock.getAdCache()).thenReturn(adCacheMock);
        Mockito.when(adCacheMock.getCwrMap()).thenReturn(cwrMapMock);
        Mockito.when(cwrMock.getResponse()).thenReturn(responseMock);

        svc = new OpsSvc();
    }

    @SuppressWarnings("unchecked")
    private <T> DaoShortLivedMap<T> getDaoShortLivedMapMock() {
        return (DaoShortLivedMap<T>) Mockito.mock(DaoShortLivedMap.class);
    }

    @Test
    public void positiveFlow_returnResponseIfCacheableWebResponseWasFound() throws Lot49Exception {
        Mockito.when(cwrMapMock.get(KVKeysValues.DEBUG_NURL_PREFIX + nurlId)).thenReturn(cwrMock);

        svc.debugNurl(nurlId);

        Mockito.verify(cwrMock, times(1)).getResponse();
    }

    @Test(expected = NullPointerException.class)
    public void negativeFlow_throwExceptionIfCacheableWebResponseWasNotFound()
                    throws Lot49Exception {
        Mockito.when(cwrMapMock.get(KVKeysValues.DEBUG_NURL_PREFIX + nurlId)).thenReturn(null);

        svc.debugNurl(nurlId);

        fail("Exception wanted but not happen");
    }
}
