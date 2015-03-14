package com.enremmeta.rtb.jersey;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OpsSvc.class, ServiceRunner.class, LogUtils.class, Response.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class OpsSvcSpec_favicon {
    private ServiceRunner serviceRunnerSimpleMock;
    private Lot49Config configMock;
    private ResponseBuilder responseBuilderMock;
    private OpsSvc svc;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(ServiceRunner.class);
        PowerMockito.mockStatic(Response.class);

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        configMock = Mockito.mock(Lot49Config.class);
        responseBuilderMock = Mockito.mock(ResponseBuilder.class);

        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        svc = new OpsSvc();
    }

    @Test
    public void positiveFlow_callNoContentIfFaviconIsNull() {
        positiveFlow_callNoContent(null);
    }

    @Test
    public void positiveFlow_callNoContentIfFaviconIsEmpty() {
        positiveFlow_callNoContent("");
    }

    private void positiveFlow_callNoContent(String favicon) {
        Mockito.when(configMock.getFavicon()).thenReturn(favicon);
        Mockito.when(Response.noContent()).thenReturn(responseBuilderMock);

        svc.favicon();

        PowerMockito.verifyStatic(times(1));
        Response.noContent();
    }

    @Test
    public void positiveFlow_callSeeOtherIfFaviconIsCorrect() {
        Mockito.when(configMock.getFavicon()).thenReturn("favicon.ico");
        Mockito.when(Response.seeOther(any(URI.class))).thenReturn(responseBuilderMock);

        svc.favicon();

        PowerMockito.verifyStatic(times(1));
        Response.seeOther(any(URI.class));
    }

    @Test
    public void negativeFlow_callServerErrorIfFaviconIsIncorrect() {
        Mockito.when(configMock.getFavicon()).thenReturn("fav icon.ico");
        Mockito.when(Response.serverError()).thenReturn(responseBuilderMock);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        svc.favicon();

        PowerMockito.verifyStatic(times(1));
        LogUtils.error(anyString());

        PowerMockito.verifyStatic(times(1));
        Response.serverError();
    }
}
