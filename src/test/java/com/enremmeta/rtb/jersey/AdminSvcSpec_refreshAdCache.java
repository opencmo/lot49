package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.container.AsyncResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.WellFormedHtml;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_refreshAdCache {
    private ServiceRunner serviceRunnerMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        svc = new AdminSvc();
    }

    private void configMocks(String host, String doRunReturn) throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.getConfig()).thenReturn(Mockito.mock(AdCacheConfig.class));
        Mockito.when(adCacheMock.doRun(false)).thenReturn(doRunReturn);
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
    }

    @Test
    public void positiveFlow_resultContainsDoRunReturn() throws Exception {
        String doRunReturn = "Return value from doRun(false)";
        configMocks("localhost", doRunReturn);
        
        String returnValue = svc.refreshAdCache(asyncResponseMock, "auth cookie");
        
        assertThat(returnValue, containsString(doRunReturn));
        
        /// test of html-markup
        assertThat(WellFormedHtml.validate(returnValue), is(true));
    }
}
