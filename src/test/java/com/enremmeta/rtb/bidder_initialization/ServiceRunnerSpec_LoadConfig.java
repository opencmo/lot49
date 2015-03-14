package com.enremmeta.rtb.bidder_initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class, ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_LoadConfig {
    // should load configuration from from protected field configFileName or from default
    // configuration

    private final String PATH_TO_CONFIG_FILE = "path_and_file";
    private ServiceRunner serviceRunnerSimpleMock;


    @Before
    public void beforeEach() throws Lot49Exception {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        PowerMockito.mockStatic(Utils.class);
        Lot49Config lot49ConfigMock = Mockito.mock(Lot49Config.class);
        PowerMockito.when(Utils.loadConfig(PATH_TO_CONFIG_FILE, Lot49Config.class))
                        .thenReturn(lot49ConfigMock);
    }


    @Test
    public void positiveFlowWhenConfigFileNameNotNull() throws Lot49Exception {
        // calls Util.loadConfig with value from protected field configFileName

        Whitebox.setInternalState(serviceRunnerSimpleMock, "configFilename", PATH_TO_CONFIG_FILE);
        assertEquals(PATH_TO_CONFIG_FILE,
                        Whitebox.getInternalState(serviceRunnerSimpleMock, "configFilename"));

        serviceRunnerSimpleMock.loadConfig();

        PowerMockito.verifyStatic(Mockito.times(1));
        Utils.loadConfig(PATH_TO_CONFIG_FILE, Lot49Config.class);

    }

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void positiveFlowWhenConfigFileNameIsNull() throws Lot49Exception {
        // load value from env[KVKeysValues.ENV_LOT49_CONFIG_FILE]

        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv(KVKeysValues.ENV_LOT49_CONFIG_FILE))
                        .thenReturn(PATH_TO_CONFIG_FILE);

        assertNull(Whitebox.getInternalState(serviceRunnerSimpleMock, "configFilename"));

        serviceRunnerSimpleMock.loadConfig();

        PowerMockito.verifyStatic(Mockito.times(1));
        System.getenv(KVKeysValues.ENV_LOT49_CONFIG_FILE);

        assertTrue(systemOutRule.getLog()
                        .contains("Config filename not specified, trying environment variable"));

    }

    @Test
    public void negativeFlowWhenConfigFileNameIsNull() throws Lot49Exception {
        // trying load value from env[KVKeysValues.ENV_LOT49_CONFIG_FILE] but it's empty

        assertNull(System.getenv(KVKeysValues.ENV_LOT49_CONFIG_FILE));
        assertNull(Whitebox.getInternalState(serviceRunnerSimpleMock, "configFilename"));

        try {
            serviceRunnerSimpleMock.loadConfig();
            fail("My method didn't throw when I expected it to");
        } catch (Lot49Exception expectedException) {
        }

    }
}
