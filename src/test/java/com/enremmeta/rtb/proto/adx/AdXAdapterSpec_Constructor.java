package com.enremmeta.rtb.proto.adx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.util.ServiceRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, CSVParser.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdXAdapterSpec_Constructor {
    private static final String GEO_TABLE_FILE_NAME = "geo_table_file_name";
    private final static int GEO_RECORD_ID = 1000010;

    private ServiceRunner serviceRunnerSimpleMock;
    private AdXConfig adxCfg;
    Lot49Config configMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(AdXAdapter.class, "geo", (Map<Integer, AdxGeo>) null);
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);

        adxCfg = new AdXConfig();

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                ExchangesConfig exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getAdx()).thenReturn(adxCfg);
                return exchangesConfigMock;
            }
        });


        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

    }

    private void prepareAdxConfigSecurity(AdXConfig adxCfg) {
        adxCfg.setEncryptionKey(SharedSetUp.ENCRIPTION_KEY);
        adxCfg.setIntegrityKey(SharedSetUp.INTEGRITY_KEY);
    }


    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_ShouldLoadGeoFile() throws Lot49Exception, IOException {

        // set security config for bidder
        prepareAdxConfigSecurity(adxCfg);
        // set geotable config for bidder
        adxCfg.setGeoTable(GEO_TABLE_FILE_NAME);

        // mimic concurrent loading of geotable
        ExecutorService testExecutor = Executors.newSingleThreadExecutor();

        Mockito.when(serviceRunnerSimpleMock.getExecutor()).thenReturn(testExecutor);

        PowerMockito.mockStatic(CSVParser.class);
        PowerMockito.when(CSVParser.parse(Mockito.any(File.class), Mockito.any(Charset.class),
                        Mockito.any())).thenAnswer(new Answer<CSVParser>() {
                            public CSVParser answer(InvocationOnMock invocation)
                                            throws IOException {
                                CSVParser testParser = new CSVParser(
                                                new StringReader(
                                                                "1000010,\"Abu Dhabi\",\"Abu Dhabi,Abu Dhabi,United Arab Emirates\",\"9041082,2784\",\"\",\"AE\",\"City\"\n"),
                                                CSVFormat.EXCEL);
                                return testParser;
                            }
                        });

        // action under test
        new AdXAdapter();

        // verify assignment of task for bidder's executor
        Mockito.verify(serviceRunnerSimpleMock, Mockito.times(1)).getExecutor();

        // wait for finish of loading
        long start = System.nanoTime();
        while (((Map<?, ?>) Whitebox.getInternalState(AdXAdapter.class, "geo")).keySet()
                        .isEmpty()) {
            long now = System.nanoTime();
            if ((now - start) / 1.0e9 > 5) // keep 5 seconds timeout
                fail("Concurrent task is not responding");
        }

        // verify result of geotable loading
        assertEquals(new HashSet<Integer>() {
            {
                add(GEO_RECORD_ID);
            }
        }, ((Map<?, ?>) Whitebox.getInternalState(AdXAdapter.class, "geo")).keySet());

        assertEquals("Abu Dhabi,Abu Dhabi,United Arab Emirates",
                        ((AdxGeo) ((Map<?, ?>) Whitebox.getInternalState(AdXAdapter.class, "geo"))
                                        .get(GEO_RECORD_ID)).getCanonicalName());

        assertEquals("Abu Dhabi",
                        ((AdxGeo) ((Map<?, ?>) Whitebox.getInternalState(AdXAdapter.class, "geo"))
                                        .get(GEO_RECORD_ID)).getName());

        assertEquals("ae",
                        ((AdxGeo) ((Map<?, ?>) Whitebox.getInternalState(AdXAdapter.class, "geo"))
                                        .get(GEO_RECORD_ID)).getCountryCode());

    }



    @Test
    public void negativeFlow_SecurityKeysNotSpecified() throws Lot49Exception {


        try {
            new AdXAdapter();
            fail("My method didn't throw when I expected it to");
        } catch (Lot49Exception expectedException) {
            assertTrue(expectedException.getMessage().contains(
                            "Either encryption or integrity key missing from OpenX configuration."));
        }

    }

    @Test
    public void negativeFlow_GeoTableNotSpecified() throws Lot49Exception {

        prepareAdxConfigSecurity(adxCfg);

        // mock getExecutor method
        ExecutorService testExecutor = Executors.newSingleThreadExecutor();
        Mockito.when(serviceRunnerSimpleMock.getExecutor()).thenReturn(testExecutor);

        new AdXAdapter();

        Mockito.verify(serviceRunnerSimpleMock, Mockito.never()).getExecutor();

    }


    // TODO
    // Refactoring ideas for code under test:
    // 1. extract runnable from constructor
    // 2. make helper class for geotable loading to avoid testing concurrent code
    // 3. remove parse method tests from constructor

}
