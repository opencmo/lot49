package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedAssert;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.WellFormedHtml;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_setBudget {
    private ServiceRunner serviceRunnerMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        svc = new AdminSvc();
    }

    private void configMocks(String host) throws Exception {
        Lot49Config configMock = serviceRunnerMock.getConfig();
        Mockito.when(configMock.getHost()).thenReturn(host);

        AdCache adCacheMock = Mockito.mock(AdCache.class);
        Mockito.when(adCacheMock.setBudget(anyString(), anyLong())).thenReturn("OK");
        PowerMockito.doReturn(adCacheMock).when(serviceRunnerMock).getAdCache();
    }

    @Test
    public void negativeFlow_redirectsToLoginIfNotAuthenticated() throws Exception {
        configMocks("not localhost");

        svc.setBudget(asyncResponseMock, "AdId", 1000, "bad auth cookie");

        SharedAssert.redirectedToLogin(asyncResponseMock);
    }

    @Test
    public void positiveFlow_test_SetBudget() throws Exception {
        configMocks("localhost");

        svc.setBudget(asyncResponseMock, "AdId", 1000, "auth cookie");

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponseMock, times(1)).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(Response.Status.OK));
        
        /// test of html-markup
        String htmlString = fixMarkupErrors(responseValue.getEntity().toString());
        assertThat(WellFormedHtml.validate(htmlString), is(true));
    }

    private String fixMarkupErrors(String markup) {
        // TODO in AdminSvc.setBudget(): would be super to correct html markup
        markup = markup.replace("<br>", "<br />");
        return markup;
    }
}
