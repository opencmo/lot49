package com.enremmeta.rtb.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class TagImplSpec_constructor {

    private ServiceRunner serviceRunnerSimpleMock;
    private OrchestratorConfig orchConfig;

    @Before
    public void setUp() throws Exception {

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);
        Mockito.when(configMock.getStatsUrl()).thenReturn("http://stats.url");

        orchConfig = new OrchestratorConfig();

        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(new LocalOrchestrator(orchConfig));

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
    }

    @Test
    public void positiveFlow_shouldSetSecureStatsUrl() throws Lot49Exception {

        Ad ad = new SharedSetUp.Ad_1001001_fake();

        Tag tag = new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);

        assertEquals("https://stats.url", Whitebox.getInternalState(tag, "secureStatsUrl"));


    }

    @Test
    public void positiveFlow_shouldCallInit() throws Lot49Exception {

        SharedSetUp.Tag_2002002_tagMarker_1001001_fake.setInitCallCounter(0);

        Ad ad = new SharedSetUp.Ad_1001001_fake();

        new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);

        assertEquals(1, SharedSetUp.Tag_2002002_tagMarker_1001001_fake.getInitCallCounter());


    }

}
