package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.SharedAssert;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdminSvcSpec_auth_2 {
    private ServiceRunner serviceRunnerMock;
    private Lot49Config configMock;
    private AsyncResponse asyncResponseMock;
    private AdminSvc svc;

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        configMock = serviceRunnerMock.getConfig();
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        svc = new AdminSvc();
    }

    private void configMocks(String host, String baseUrl) throws Exception {
        Mockito.when(configMock.getHost()).thenReturn(host);
        Mockito.when(configMock.getBaseUrl()).thenReturn(baseUrl);
    }

    @Test
    public void positiveFlow_returnsTrueIfLocalhost() throws Exception {
        testAuthIfBaseUrlIsCorrect("localhost", null, true);
    }

    @Test
    public void positiveFlow_returnsTrueIfNotLocalhostAndGoodAuthCookie() throws Exception {
        testAuthIfBaseUrlIsCorrect("not localhost", JerseySvc.AUTH_COOKIE_DU_JOUR, true);
    }

    @Test
    public void negativeFlow_returnsFalseIfNotLocalhostAndBadAuthCookie() throws Exception {
        testAuthIfBaseUrlIsCorrect("not localhost", "bad auth cookie", false);
    }

    @Test
    public void negativeFlow_returnsFalseIfNotLocalhostAndNullAuthCookie() throws Exception {
        testAuthIfBaseUrlIsCorrect("not localhost", null, false);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwsExceptionIfBaseUrlIsIncorrect() throws Exception {
        configMocks("not localhost", "Incorrect Base Url");

        Whitebox.invokeMethod(svc, "auth", asyncResponseMock, null);
    }

    private void testAuthIfBaseUrlIsCorrect(String host, String authCookie, boolean expectedReturnValue) throws Exception {
        configMocks(host, "CorrectBaseUrl");

        boolean returnValue = Whitebox.invokeMethod(svc, "auth", asyncResponseMock, authCookie);

        assertThat(returnValue, equalTo(expectedReturnValue));

        if (!expectedReturnValue) {
            SharedAssert.redirectedToLogin(asyncResponseMock);
        }
    }
}
