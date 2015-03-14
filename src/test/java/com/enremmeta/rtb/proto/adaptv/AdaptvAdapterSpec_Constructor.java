package com.enremmeta.rtb.proto.adaptv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import com.enremmeta.rtb.Lot49RuntimeException;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.adx.AdXAdapter;
import com.enremmeta.rtb.proto.adx.AdxGeo;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
public class AdaptvAdapterSpec_Constructor {

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
    public void positiveFlow_shouldSetAdaptvConfig() {

        adaptvCfg.setPartnerId(BUYER_ID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getAdaptv();

        assertEquals(BUYER_ID, Whitebox.getInternalState(adapter, "buyerId"));

    }

    @Test
    public void positiveFlow_shouldSetDefaultMaxDuration() {

        adaptvCfg.setPartnerId(BUYER_ID);
        adaptvCfg.setDefaultMaxDuration(DEFAULT_MAX_DURATION);

        AdaptvAdapter adapter = new AdaptvAdapter();

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getAdaptv();

        assertEquals(DEFAULT_MAX_DURATION,
                        Whitebox.getInternalState(adapter, "defaultMaxDuration"));

    }

    @Test
    public void positiveFlow_shouldSetAssumeSwfIfVpaid() {

        adaptvCfg.setPartnerId(BUYER_ID);
        adaptvCfg.setAssumeSwfIfVpaid(ASSUME_SWF_IF_VPAID);

        AdaptvAdapter adapter = new AdaptvAdapter();

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getAdaptv();

        assertEquals(ASSUME_SWF_IF_VPAID, Whitebox.getInternalState(adapter, "assumeSwfIfVpaid"));

    }

    @Test
    public void negativeFlow_buyerIdWasNotProvided() {

        try {
            new AdaptvAdapter();
            fail("Exception expected but not thrown!");
        } catch (Lot49RuntimeException e) {
            assertEquals("buyerId is null", e.getMessage());
        }

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getAdaptv();


    }

}
