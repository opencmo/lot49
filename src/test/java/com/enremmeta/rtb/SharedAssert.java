package com.enremmeta.rtb;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.ArgumentCaptor;

import com.enremmeta.rtb.constants.Lot49Constants;

public class SharedAssert {
    public static void redirectedToLogin(AsyncResponse asyncResponse) {
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(asyncResponse, times(1)).resume(responseCaptor.capture());

        Response responseValue = responseCaptor.getValue();
        assertThat(Status.fromStatusCode(responseValue.getStatus()), equalTo(Response.Status.SEE_OTHER));
        assertThat(responseValue.getLocation().toString(), endsWith(Lot49Constants.ROOT_PATH_ADMIN + "/login"));
    }
}
