package com.enremmeta.rtb.proto.adaptv;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.proto.adx.AdxGeo;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
public class AdaptvAdapterSpec_tmpCookieFixup {

    private ServiceRunner serviceRunnerSimpleMock;
    private AdaptvConfig adaptvCfg;
    private Lot49Config configMock;
    private ExchangesConfig exchangesConfigMock;

    private static final String BUYER_ID = "PARTNER_ID";
    private static final Integer DEFAULT_MAX_DURATION = 1001;
    private static final Boolean ASSUME_SWF_IF_VPAID = true;

    @Before
    public void setUp() {
        Whitebox.setInternalState(AdXAdapter.class, "geo", (Map<Integer, AdxGeo>) null);
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);

        adaptvCfg = new AdaptvConfig();

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getAdaptv()).thenReturn(adaptvCfg);
                return exchangesConfigMock;
            }
        });


        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

    }

    @Test
    public void positiveFlow_depthGT10() throws Exception {

        adaptvCfg.setPartnerId(BUYER_ID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        int depth = 11;
        String result = Whitebox.invokeMethod(adapter, "tmpCookieFixup", "COOKIE", 11);
        
        assertEquals("COOKIE", result);

    }
    
    @Test
    public void positiveFlow_cookieLengthBTW22And25() throws Exception {

        adaptvCfg.setPartnerId(BUYER_ID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        int depth = 11;
        String result = Whitebox.invokeMethod(adapter, "tmpCookieFixup", "COOKIE123456789012345678", depth);
        
        assertEquals("COOKIE123456789012345678", result);

    }

    @Test
    public void positiveFlow_maybeEncodedAlg1() throws Exception {

        adaptvCfg.setPartnerId(BUYER_ID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        int depth = 5;
        String result = Whitebox.invokeMethod(adapter, "tmpCookieFixup", "OOKIE1234567890123456%2578AAAAAAAAAAAAAAAAAAAAAAAAAAA", depth);
        
        assertEquals("OOKIE1234567890123456xAAAAAAAAAAAAAAAAAAAAAAAAAAA", result);

    }
    
    @Test
    public void positiveFlow_maybeEncodedAlg2() throws Exception {

        adaptvCfg.setPartnerId(BUYER_ID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        int depth = 5;
        String result = Whitebox.invokeMethod(adapter, "tmpCookieFixup", "CCCOOKIE1234567890123456%2578AAAAAAAAAAAAAAAAAAAAAAAAAAA", depth);
        
        assertEquals("CCCOOKIE1234567890123456%2578AAAAAAAAAAAAAAAAAAAAAAAAAAA", result);

    }
    
}
