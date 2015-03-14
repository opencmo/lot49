package com.enremmeta.rtb.jersey;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;

import javax.ws.rs.container.AsyncResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.constants.Lot49Constants;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
public class AdminSvcSpec_login {
    private AdminSvc svc;
    private AsyncResponse asyncResponseMock;

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();

        asyncResponseMock = Mockito.mock(AsyncResponse.class);

        svc = new AdminSvc();
    }

    @Test
    public void positiveFlow_callResumeWithExpectedResponse() {
        String authPath = Lot49Constants.ROOT_PATH_ADMIN + "/auth";
        String regex = String.format("^<form .*action=\"%1$s\".*>.*<\\/form>$",
                        authPath.replace("/", "\\/"));

        svc.login(asyncResponseMock);

        Mockito.verify(asyncResponseMock, times(1)).resume(anyString());
        Mockito.verify(asyncResponseMock).resume(matches(regex));
    }
}
