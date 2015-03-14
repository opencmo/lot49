package com.enremmeta.rtb.bidder_initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.AwsOrchestrator;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AwsOrchestrator.class, ServiceRunner.class})
public class ServiceRunnerSpec_InitOrchestrator {
    // should detect the type of deployment orchestrator
    private OrchestratorConfig orchConfig;

    private ServiceRunner serviceRunnerSimpleMock;

    @SuppressWarnings("deprecation")
    @Before
    public void beforeEach() throws Lot49Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        orchConfig = new OrchestratorConfig();

        Lot49Config configMock = Mockito.mock(Lot49Config.class);
        Mockito.when(configMock.getOrchestrator()).thenReturn(orchConfig);
        Mockito.when(configMock.getDeploy()).thenReturn("deploy settings from Lot49Config object");
        Whitebox.setInternalState(serviceRunnerSimpleMock, "config", configMock);
    }

    @Test
    public void positiveFlow_LocalOchestrator() throws Lot49Exception {
        orchConfig.setDeployType(LocalOrchestrator.DEPLOY_TYPE);

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        serviceRunnerSimpleMock.initOrchestrator();

        assertNotNull(serviceRunnerSimpleMock.getOrchestrator());
        assertEquals(LocalOrchestrator.class, serviceRunnerSimpleMock.getOrchestrator().getClass());
    }

    @Test
    public void positiveFlow_AWSOchestrator() throws Exception {
        orchConfig.setDeployType(AwsOrchestrator.DEPLOY_TYPE);

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        AwsOrchestrator awsOchAwsOrchestratorMock = PowerMockito.mock(AwsOrchestrator.class);

        PowerMockito.whenNew(AwsOrchestrator.class)
                        .withArguments(Mockito.any(OrchestratorConfig.class))
                        .thenReturn(awsOchAwsOrchestratorMock);

        serviceRunnerSimpleMock.initOrchestrator();

        assertNotNull(serviceRunnerSimpleMock.getOrchestrator());
        assertEquals(awsOchAwsOrchestratorMock, serviceRunnerSimpleMock.getOrchestrator());
    }

    @Test
    public void negativeFlow_wrongDeployType() throws Exception {
        orchConfig.setDeployType("WRONG_DEPLOY_TYPE");

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        try {
            serviceRunnerSimpleMock.initOrchestrator();
            fail("My method didn't throw when I expected it to");
        } catch (Lot49Exception expectedException) {
            assertTrue(expectedException.getMessage().contains("Unknown deploy type"));
        }

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        // TODO:
        // refactoring idea;
        // wrong place for this verification
        // if (orchConfig == null)
        // should be at the top of method - not inside particular case
    }

    @Test
    public void negativeFlow_nullDeployType() throws Exception {
        // should fetch deployType from config field
        orchConfig.setDeployType(null);

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        try {
            serviceRunnerSimpleMock.initOrchestrator();
            fail("My method didn't throw when I expected it to");
        } catch (Lot49Exception expectedException) {
            assertTrue(expectedException.getMessage().contains("Unknown deploy type"));
        }

        assertNull(serviceRunnerSimpleMock.getOrchestrator());

        // TODO:
        // refactoring idea;
        // resolve deprecation status for method
        // config.getDeploy()
    }

}
