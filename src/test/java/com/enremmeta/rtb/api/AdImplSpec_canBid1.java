package com.enremmeta.rtb.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

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
import com.enremmeta.rtb.api.proto.openrtb.Content;
import com.enremmeta.rtb.api.proto.openrtb.Device;
import com.enremmeta.rtb.api.proto.openrtb.Geo;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.Site;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtGeo;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.spi.providers.Provider;
import com.enremmeta.rtb.spi.providers.ProviderImpl;
import com.enremmeta.rtb.spi.providers.ProviderInfoRequired;
import com.enremmeta.rtb.spi.providers.integral.IntegralInfoRequired;
import com.enremmeta.util.ServiceRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdImplSpec_canBid1 {

    private ServiceRunner serviceRunnerSimpleMock;
    private OrchestratorConfig orchConfig;

    @Before
    public void setUp() throws Exception {

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        orchConfig = new OrchestratorConfig();

        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(new LocalOrchestrator(orchConfig));

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
    }

    @Test
    public void negativeFlow_domainCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        //Ad adSpy = Mockito.spy(ad);
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("No domain received", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void positiveFlow_noNegativeChecks() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        //Ad adSpy = Mockito.spy(ad);
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertTrue(result);        
        assertNull(br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_exchangesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "exchanges", new TreeSet<String>(){{
            add(Lot49Constants.EXCHANGE_PUBMATIC);
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Exch: adaptv not in [pubmatic]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_sspsCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "ssps", new TreeSet<String>(){{
            add("OTHER_TEST_SSP");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getLot49Ext().setSsp("ONE_TEST_SSP");
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("SSP: ONE_TEST_SSP not in [OTHER_TEST_SSP]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }


    @Test
    public void negativeFlow_deviceMakesCheckFail_deviceIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceMakes", new TreeSet<String>(){{
            add("OTHER_TEST_DEVICE_MAKE");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Dev: UNKNOWN not in [OTHER_TEST_DEVICE_MAKE]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceMakesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceMakes", new TreeSet<String>(){{
            add("OTHER_TEST_DEVICE_MAKE");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setMake("ONE_TEST_DEVICE_MAKE");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Dev: one_test_device_make not in [OTHER_TEST_DEVICE_MAKE]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceModelsCheckFail_deviceIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceModels", new TreeSet<String>(){{
            add("OTHER_TEST_DEVICE_MODEL");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DevMod: UNKNOWN not in [OTHER_TEST_DEVICE_MODEL]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceModelsCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceModels", new TreeSet<String>(){{
            add("OTHER_TEST_DEVICE_MODEL");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setModel("ONE_TEST_DEVICE_MODEL");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DevMod: one_test_device_model not in [OTHER_TEST_DEVICE_MODEL]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceTypesCheckFail_deviceIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceTypes", new TreeSet<Integer>(){{
            add(22022);
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DevType: UNKNOWN not in [22022]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceTypesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceTypes", new TreeSet<Integer>(){{
            add(22022);
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setDeviceType(11011);}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DevType: 11011 not in [22022]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_deviceTypesCheckFail_deviceTypeIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "deviceTypes", new TreeSet<Integer>(){{
            add(22022);
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setDeviceType(null);}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DevType: UNKNOWN type", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_osesCheckFail_deviceIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "oses", new TreeSet<String>(){{
            add("TEST_OS");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("OS: UNKNOWN not in [TEST_OS]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    
    @Test
    public void negativeFlow_osesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "oses", new TreeSet<String>(){{
            add("TEST_OS");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setOs("Android"); setOsv("6.1");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("OS: android 6.1 not in [TEST_OS]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_languagesCheckFail_deviceAndContentIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "languages", new TreeSet<String>(){{
            add("es");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");  setContent(null);}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Lang: UNKNOWN not in [es]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_languagesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "languages", new TreeSet<String>(){{
            add("es");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        Content cnt = new Content();
        Whitebox.setInternalState(cnt, "language", "en");
        br.setSite(new Site(){{setDomain("TEST_DOMAIN"); setContent(cnt);}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("None of [es] found in [sk, en]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_browsersCheckFail_deviceIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "browsers", new TreeSet<String>(){{
            add("chrome");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("UA: UNKNOWN not in [chrome]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_browsersCheckFail_deviceUaIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "browsers", new TreeSet<String>(){{
            add("chrome");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getDevice().setUa(null);
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("UA: UNKNOWN not in [chrome]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_browsersCheckFail_browserFamilyAndNameIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "browsers", new TreeSet<String>(){{
            add("chrome");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        br.getLot49Ext().setBrowserFamily(null);
        br.getLot49Ext().setBrowserName(null);
        
        br.setDevice(new Device(){{setLanguage("sk"); setUa("TEST_UA");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("UA: UNKNOWN not in [chrome]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_browsersCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "browsers", new TreeSet<String>(){{
            add("chrome");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        br.getLot49Ext().setBrowserFamily("webkit");
        br.getLot49Ext().setBrowserName("mozilla");
        
        br.setDevice(new Device(){{setLanguage("sk"); setUa("TEST_UA");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        Content cnt = new Content();
        Whitebox.setInternalState(cnt, "language", "en");
        br.setSite(new Site(){{setDomain("TEST_DOMAIN"); setContent(cnt);}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("UA: mozilla/webkit not in [chrome]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
   
    
    @Test
    public void negativeFlow_geosCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "geos", new LinkedList<TargetingGeo>(){{
            add(new TargetingGeo("city", "metro", "region", "country", "zip"));
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("No match for [{Geo: City: city, Metro: metro, Zip/Postal code: zip, Region: region, Country: country, }] in User: null, Dev: null, Lot49: {Geo: }", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingCategoriesCheckFail_catsIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingCategories", new TreeSet<String>(){{
            add("TEST_CATEGORY");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{setDomain("TEST_DOMAIN");}});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Got no Cat/Pagecat, but targetingCategories", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingCategoriesCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingCategories", new TreeSet<String>(){{
            add("TEST_CATEGORY");
        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("None of [Cat1] match [TEST_CATEGORY]", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingHourCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingHour", TargetingHour.AFTERNOON);
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Cannot determine hour", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingHourCheckFail_getUserHourIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingHour", TargetingHour.AFTERNOON);
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getLot49Ext().setGeo(new Geo(){{setExt(new HashMap(){{
            put(Lot49ExtGeo.GEO_EXT_KEY, new Lot49ExtGeo());
        }}
        );}});
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Cannot determine hour", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingHourCheckFail_failedCheckHour() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingHour", 
                        new TargetingHourList(Arrays.asList(new Integer[]{9,10,11,12})));
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getLot49Ext().setGeo(new Geo(){{setExt(new HashMap(){{
            put(Lot49ExtGeo.GEO_EXT_KEY, new Lot49ExtGeo(){{setUserHour(3);}});
        }}
        );}});
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Hour 3 not in TargetingHourList([9, 10, 11, 12])", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingDOWCheckFail_lot49extGeoIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingDOW", 
                        new TargetingDOWList(Arrays.asList(new Integer[]{
                                        TargetingDOW.MONDAY, TargetingDOW.TUESDAY
                                      })));
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Cannot determine DOW", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingDOWCheckFail_getUserDowIsNull() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingDOW", 
                        new TargetingDOWList(Arrays.asList(new Integer[]{
                                        TargetingDOW.MONDAY, TargetingDOW.TUESDAY
                                      })));
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getLot49Ext().setGeo(new Geo(){{setExt(new HashMap(){{
            put(Lot49ExtGeo.GEO_EXT_KEY, new Lot49ExtGeo());
        }}
        );}});
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("Cannot determine DOW", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_targetingDOWCheckFail_failedCheckDOW() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "targetingDOW", 
                        new TargetingDOWList(Arrays.asList(new Integer[]{
                                        TargetingDOW.MONDAY, TargetingDOW.TUESDAY
                                      })));
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());
        
        br.getLot49Ext().setGeo(new Geo(){{setExt(new HashMap(){{
            put(Lot49ExtGeo.GEO_EXT_KEY, new Lot49ExtGeo(){{setUserDow(TargetingDOW.FRIDAY);}});
        }}
        );}});
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("DOW 5 not in TargetingDOWList([1, 2])", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
    
    @Test
    public void negativeFlow_providerTargetingCheckFail() throws Lot49Exception {

        AdImpl ad = new SharedSetUp.Ad_1001001_fake();
        
        IntegralInfoRequired iirMock = Mockito.mock(IntegralInfoRequired.class);
        Mockito.when(iirMock.toString()).thenReturn("IntegralInfoRequired_MARKER");
        
        Whitebox.setInternalState(ad, "providerTargeting", 
                        new HashMap<Provider, ProviderInfoRequired>(){{
                            put(Mockito.mock(ProviderImpl.class), iirMock);
                        }});
        
        OpenRtbRequest br = new OpenRtbRequest();
        
        br.getLot49Ext().setAdapter(new AdaptvAdapter());   
        
        br.setDevice(new Device(){{setLanguage("sk");}});
        
        br.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });
        
        br.setSite(new Site(){{
            setDomain("TEST_DOMAIN"); 
            setCat(new LinkedList<String>(){{add("Cat1");}});
        }});
        
        boolean result = ad.canBid1(br);
        
        assertFalse(result);        
        assertEquals("null: Need: IntegralInfoRequired_MARKER; got: null", br.getLot49Ext().getOptoutReasons().get(ad.getId()));

    }
}
