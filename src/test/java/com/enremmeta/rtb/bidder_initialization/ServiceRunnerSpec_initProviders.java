package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderConfig;
import com.enremmeta.rtb.spi.providers.ProviderFactory;
import com.enremmeta.rtb.spi.providers.skyhook.SkyhookProvider;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.SimpleCallback;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, ProviderFactory.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initProviders {
    // should create and initialize providers and add them to the map 'providers'
    
    @SuppressWarnings("rawtypes")
    private Map<String, Map> providersConfig = new HashMap<String, Map>();
    private Map<String, Provider> providers = new HashMap<String, Provider>();
    private Lot49Config lot49ConfigMock;
    private ServiceRunner serviceRunnerMock;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        lot49ConfigMock = Mockito.mock(Lot49Config.class);
        Mockito.when(lot49ConfigMock.getProviders()).thenReturn(providersConfig);
        
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        serviceRunnerMock.setConfig(lot49ConfigMock);
        Whitebox.setInternalState(serviceRunnerMock, "providers", providers);
    }

    @Test
    public void negativeFlow_interruptsExecutionIfGetProvidersReturnsNull() throws Lot49Exception {
        Mockito.when(lot49ConfigMock.getProviders()).thenReturn(null);
        
        serviceRunnerMock.initProviders();
        
        PowerMockito.verifyStatic();
        LogUtils.init("No providers configured.");
        
        assertThat(providers.size(), equalTo(0));
    }

    @Test
    public void positiveFlow_addsProviderIfInitAsyncDoesNotThrowException() throws Exception {
        String provName = SkyhookProvider.SKYHOOK_PROVIDER_NAME;
        Provider providerMock = prepareProviderMock(provName, true, true, null);
        
        assertThat(providers.get(provName), equalTo(null));

        serviceRunnerMock.initProviders();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Initializing " + provName);
        
        PowerMockito.verifyStatic();
        LogUtils.init(contains("Successfully initialized provider '" + provName + "' in "));
        
        Mockito.verify(providerMock).initAsync(any());
        assertThat(providers.get(provName), is(providerMock));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_doesNotAddProviderIfInitAsyncThrowsException() throws Exception {
        // TODO in ServiceRunner.initProviders(): provider should not be added to the map 'providers' if exception was thrown
        String provName = SkyhookProvider.SKYHOOK_PROVIDER_NAME;
        Provider providerMock = prepareProviderMock(provName, true, true, new RuntimeException("Exception in initAsync()"));
        
        assertThat(providers.get(provName), equalTo(null));

        serviceRunnerMock.initProviders();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Initializing " + provName);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Error initializing provider '" + provName + "'"), any());
        
        Mockito.verify(providerMock).initAsync(any());
        assertThat(providers.get(provName), equalTo(null));
    }

    @Test
    public void positiveFlow_addsProviderIfInitFinishesSuccessfully() throws Exception {
        String provName = SkyhookProvider.SKYHOOK_PROVIDER_NAME;
        Provider providerMock = prepareProviderMock(provName, true, false, null);
        
        assertThat(providers.get(provName), equalTo(null));

        serviceRunnerMock.initProviders();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Initializing " + provName);
        
        Mockito.verify(providerMock).init();
        assertThat(providers.get(provName), is(providerMock));
    }

    @Test
    public void negativeFlow_skipsInitializationIfProviderIsNotEnabled() throws Exception {
        String provName = SkyhookProvider.SKYHOOK_PROVIDER_NAME;
        Provider providerMock = prepareProviderMock(provName, false, false, null);
        
        assertThat(providers.get(provName), equalTo(null));

        serviceRunnerMock.initProviders();
        
        PowerMockito.verifyStatic();
        LogUtils.init("Provider '" + provName + "' is not enabled, skipping.");
        
        Mockito.verify(providerMock, times(0)).init();
        assertThat(providers.get(provName), equalTo(null));
    }

    @SuppressWarnings("rawtypes")
    private Provider prepareProviderMock(String provName, boolean isEnabled, boolean isInitAsync, Throwable initAsyncException) throws Exception {
        Map provConfMap = new HashMap();
        providersConfig.put(provName, provConfMap);
        
        ProviderConfig providerConfigMock = Mockito.mock(ProviderConfig.class);
        Mockito.when(providerConfigMock.isEnabled()).thenReturn(isEnabled);
        PowerMockito.whenNew(ProviderConfig.class).withArguments(provConfMap).thenReturn(providerConfigMock);
        
        Provider providerMock = Mockito.mock(Provider.class);
        Mockito.when(providerMock.getName()).thenReturn(provName);
        Mockito.when(providerMock.isInitAsync()).thenReturn(isInitAsync);
        
        if (isInitAsync) {
            Mockito.doAnswer((InvocationOnMock invocation) -> {
                SimpleCallback cb = invocation.getArgumentAt(0, SimpleCallback.class);
                cb.done(initAsyncException);
                return null;
            }).when(providerMock).initAsync(any());
        }
        
        PowerMockito.mockStatic(ProviderFactory.class);
        PowerMockito.when(ProviderFactory.getProvider(any(), eq(provName), any())).thenReturn(providerMock);
        
        return providerMock;
    }
}
