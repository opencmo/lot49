package com.enremmeta.rtb.bidder_initialization;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initAdapters {
    // should initialize all exchange adapters

    private ServiceRunner serviceRunnerMock;
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
    }

    @Test
    public void negativeFlow_throwsExceptionIfCouldNotInitializeAllAdapters() throws Lot49Exception {
        expectedEx.expect(Lot49Exception.class);
        expectedEx.expectMessage("Could not initialize all adapters.");
        
        serviceRunnerMock.initAdapters();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Initializing exchange adapters.");
        
        PowerMockito.verifyStatic(atLeastOnce());
        LogUtils.error(contains("Error initializing adapter for "), any());
    }

    @Test
    public void positiveFlow_expectedLogsIfAllAdaptersAreInitialized() throws Lot49Exception {
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        
        Lot49Config lot49ConfigMock = serviceRunnerMock.getConfig();
        Mockito.when(lot49ConfigMock.getExchanges()).thenReturn(exchangesConfig);
        
        serviceRunnerMock.initAdapters();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Initializing exchange adapters.");
        
        int adaptersCount = ExchangeAdapterFactory.getAllExchangeAdapterNames().size();
        PowerMockito.verifyStatic(times(adaptersCount));
        LogUtils.init(contains("... OK: "));
    }
}
