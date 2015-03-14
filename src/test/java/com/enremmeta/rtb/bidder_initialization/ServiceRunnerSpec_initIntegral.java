package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
import com.enremmeta.rtb.spi.providers.integral.IntegralConfig;
import com.enremmeta.rtb.spi.providers.integral.IntegralService;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initIntegral {
    // should initialize integral service

    private Lot49Config lot49ConfigMock;
    private ServiceRunner serviceRunnerMock;
    
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        lot49ConfigMock = Mockito.mock(Lot49Config.class);
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        serviceRunnerMock.setConfig(lot49ConfigMock);
    }

    @Test
    public void positiveFlow_initializesIntegralServiceIfItIsConfigured() throws Exception {
        IntegralConfig integralConfig = new IntegralConfig();
        integralConfig.setHost("http://ec2-52-2-36-93.compute-1.amazonaws.com");
        integralConfig.setPort(8080);
        
        Mockito.when(lot49ConfigMock.getIntegral()).thenReturn(integralConfig);
        
        IntegralService integralServiceMock = Mockito.mock(IntegralService.class);
        PowerMockito.whenNew(IntegralService.class).withArguments(integralConfig).thenReturn(integralServiceMock);
        
        serviceRunnerMock.initIntegral();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Integral endpoint : " + integralConfig.getEndpoint());
        
        assertThat(serviceRunnerMock.getIntegralService(), is(integralServiceMock));
    }

    @Test
    public void negativeFlow_doesNotInitializeIntegralServiceIfGetIntegralReturnsNull() {
        Mockito.when(lot49ConfigMock.getIntegral()).thenReturn(null);
        
        serviceRunnerMock.initIntegral();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Integral is not configurated");
        
        assertThat(serviceRunnerMock.getIntegralService(), equalTo(null));
    }
}
