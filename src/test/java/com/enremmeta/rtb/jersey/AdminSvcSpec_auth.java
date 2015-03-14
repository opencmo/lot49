package com.enremmeta.rtb.jersey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdminConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
public class AdminSvcSpec_auth {
    private AdminSvc svc;
    private ServiceRunner serviceRunnerMock;
    private Lot49Config configMock;
    private AdminConfig adminConfigMock;
    private AsyncResponse asyncResponseMock;
    private String username = "CorrectUsername";
    private String password = "CorrectPassword";

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();

        configMock = serviceRunnerMock.getConfig();
        adminConfigMock = Mockito.mock(AdminConfig.class);
        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        Mockito.when(configMock.getAdmin()).thenReturn(adminConfigMock);

        svc = new AdminSvc();
    }

    @Test
    public void positiveFlow_callResumeSeeOtherIfConfigUserNameIsNull() throws Exception {
        // would be better to throw exception when adminConfigMock.getUsername() returns null or
        // empty string
        checkResponse(null, null, Response.Status.SEE_OTHER);
    }

    @Test
    public void positiveFlow_callResumeSeeOtherIfConfigUserNameIsEmty() throws Exception {
        // would be better to throw exception when adminConfigMock.getUsername() returns null or
        // empty string
        checkResponse("", null, Response.Status.SEE_OTHER);
    }

    @Test
    public void negativeFlow_callResumeForbiddenIfUsernameIsIncorrect() throws Exception {
        checkResponse("IncorrectUsername", null, Response.Status.FORBIDDEN);
    }

    @Test
    public void positiveFlow_callForbiddenIfConfigPasswordIsNull() throws Exception {
        // password = null; // causes java.lang.NullPointerException
        checkResponse("CorrectUsername", null, Response.Status.FORBIDDEN);
    }

    @Test
    public void positiveFlow_callResumeForbiddenIfConfigPasswordIsEmpty() throws Exception {
        // password = ""; // test will pass
        checkResponse("CorrectUsername", "", Response.Status.FORBIDDEN);
    }

    @Test
    public void negativeFlow_callResumeForbiddenIfPasswordIsIncorrect() throws Exception {
        checkResponse("CorrectUsername", "IncorrectPassword", Response.Status.FORBIDDEN);
    }

    @Test
    public void positiveFlow_callResumeSeeOtherIfUsernameAndPasswordAreCorrect() throws Exception {
        checkResponse("CorrectUsername", "CorrectPassword", Response.Status.SEE_OTHER);
    }

    @Test(expected = WebApplicationException.class)
    public void negativeFlow_throwExceptionIfBaseUrlIsIncorrect() throws Exception {
        Mockito.when(configMock.getBaseUrl()).thenReturn("Incorrect Base Url");

        checkResponse("CorrectUsername", "CorrectPassword", Response.Status.SEE_OTHER);
    }

    @Test
    public void positiveFlow_callResumeWithExpectedResponseIfAllConfigDataAreCorrect()
                    throws Exception {
        Mockito.when(configMock.getBaseUrl()).thenReturn("CorrectBaseUrl");

        checkResponse("CorrectUsername", "CorrectPassword", Response.Status.SEE_OTHER);
    }

    private void checkResponse(String adminConfigUsername, String adminConfigPassword,
                    Status expectedResponseStatus) throws Exception {
        Mockito.when(adminConfigMock.getUsername()).thenReturn(adminConfigUsername);
        Mockito.when(adminConfigMock.getPassword()).thenReturn(adminConfigPassword);

        svc.auth(asyncResponseMock, username, password);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponseMock, times(1)).resume(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertThat(response.getStatus(), equalTo(expectedResponseStatus.getStatusCode()));

        if (expectedResponseStatus == Response.Status.SEE_OTHER) {
            Map<String, NewCookie> cookies = response.getCookies();

            assertThat(response.getLocation().toString(), equalTo(
                            configMock.getBaseUrl() + Lot49Constants.ROOT_PATH_ADMIN + "/"));
            assertThat(cookies.containsKey("auth"), equalTo(true));
            assertThat(cookies.get("auth").getName(), equalTo("auth"));
            assertThat(cookies.get("auth").getPath(), equalTo("/"));
        }
    }
}
