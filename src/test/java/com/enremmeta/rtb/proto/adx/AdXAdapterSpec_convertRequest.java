package com.enremmeta.rtb.proto.adx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Assert;
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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.MarkupType;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.constants.RtbConstants;
import com.enremmeta.util.ServiceRunner;
import com.google.openrtb.OpenRtb;
import com.google.protos.adx.NetworkBid;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, CSVParser.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdXAdapterSpec_convertRequest {
    private static final String GEO_TABLE_FILE_NAME = "geo_table_file_name";
    private final static int GEO_RECORD_ID = 1000010;

    private ServiceRunner serviceRunnerSimpleMock;
    private AdXConfig adxCfg;
    private Lot49Config configMock;
    private AdXAdapter adapter;

    @Before
    public void setUp() throws Lot49Exception, IOException {
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

        prepareAdapter();

    }

    private void prepareAdxConfigSecurity(AdXConfig adxCfg) {
        adxCfg.setEncryptionKey(SharedSetUp.ENCRIPTION_KEY);
        adxCfg.setIntegrityKey(SharedSetUp.INTEGRITY_KEY);
    }



    @SuppressWarnings("serial")
    public void prepareAdapter() throws Lot49Exception, IOException {

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
        adapter = new AdXAdapter();

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



    }



    @Test
    public void positiveFlow_shouldSetAdapter() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("com.enremmeta.rtb.proto.adx.AdXAdapter",
                        openRtbRequest.getLot49Ext().getAdapter().getClass().getName());
        assertTrue(openRtbRequest.getLot49Ext().isRawRequestAlreadyLogged());
    }

    @Test
    public void positiveFlow_shouldSetIdAndIp() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        byte[] bytesOfIp = {(byte) 192, (byte) 168, (byte) 11, (byte) 234};
        com.google.protobuf.ByteString ip = com.google.protobuf.ByteString.copyFrom(bytesOfIp);
        Whitebox.setInternalState(req, "ip_", ip);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals(AdXAdapter.getStringFromByteSting(req.getId()), openRtbRequest.getId());
        assertEquals("192.168.11.234.1", openRtbRequest.getDevice().getIp());

    }

    @Test
    public void positiveFlow_shouldSetGeo() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "geoCriteriaId_", GEO_RECORD_ID);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("ae", openRtbRequest.getDevice().getGeo().getCountry());

    }

    @Test
    public void positiveFlow_shouldSetUAAndMobile() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "userAgent_", SharedSetUp.USER_AGENT);

        NetworkBid.BidRequest.Device mob = NetworkBid.BidRequest.Device.getDefaultInstance();
        Whitebox.setInternalState(mob, "platform_", "Android");
        NetworkBid.BidRequest.Device.OsVersion dos =
                        NetworkBid.BidRequest.Device.OsVersion.getDefaultInstance();
        Whitebox.setInternalState(dos, "major_", 19);
        Whitebox.setInternalState(mob, "osVersion_", dos);
        Whitebox.setInternalState(req, "device_", mob);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals(SharedSetUp.USER_AGENT, openRtbRequest.getDevice().getUa());
        assertEquals("Android", openRtbRequest.getDevice().getMake());
        assertEquals("19", openRtbRequest.getDevice().getOsv());

    }


    private static final String TEST_URL = "http://www.test-site.cn";

    @Test
    public void positiveFlow_shouldSetSite() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "url_", TEST_URL);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals(TEST_URL, openRtbRequest.getSite().getPage());
        assertEquals("test-site.cn", openRtbRequest.getSite().getDomain());

    }

    private static final String WRONG_TEST_URL = "&&&&&&&&&&&&&&&&&&&&&&&";

    @Test
    public void negativeFlow_wrongURL() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "url_", WRONG_TEST_URL);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        PowerMockito.verifyStatic(Mockito.never());

        assertEquals(WRONG_TEST_URL, openRtbRequest.getSite().getPage());
        assertEquals(WRONG_TEST_URL, openRtbRequest.getSite().getDomain());

        // REFACTROING IDEA:
        // the problem is broken url will be fixed before trying to do new URL(url)
        // todo: do ckeck before fix

    }

    @Test
    public void positiveFlow_shouldSetLanguage() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        com.google.protobuf.LazyStringList langs = new com.google.protobuf.LazyStringArrayList() {
            {
                add("ES");
                add("RU");
            }
        };

        Whitebox.setInternalState(req, "detectedLanguage_", langs);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("ES", openRtbRequest.getDevice().getLanguage());

    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetVerticals() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.Vertical> detectedVerticalList =
                        new ArrayList<NetworkBid.BidRequest.Vertical>() {
                            {
                                NetworkBid.BidRequest.Vertical v =
                                                NetworkBid.BidRequest.Vertical.getDefaultInstance();
                                Whitebox.setInternalState(v, "id_", 33);
                                add(v);
                            }
                        };

        Whitebox.setInternalState(req, "detectedVertical_", detectedVerticalList);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("GOOGLE_VERTICAL_33", openRtbRequest.getSite().getPagecat().get(0));

    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpBanner() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();
        Whitebox.setInternalState(req, "video_", (OpenRtb.BidRequest.Imp.Video) null);

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "width_", new ArrayList<Integer>() {
                                    {
                                        add(100);
                                    }
                                });
                                Whitebox.setInternalState(as, "height_", new ArrayList<Integer>() {
                                    {
                                        add(50);
                                    }
                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("11", openRtbRequest.getImp().get(0).getId());
        assertNotNull(openRtbRequest.getImp().get(0).getBanner());
        assertEquals("Widths: [100]", openRtbRequest.getLot49Ext().getComments().get(0));
        assertEquals(100, openRtbRequest.getImp().get(0).getBanner().getW());
        assertEquals("Heights: [50]", openRtbRequest.getLot49Ext().getComments().get(1));
        assertEquals(50, openRtbRequest.getImp().get(0).getBanner().getH());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpVideo() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "width_", new ArrayList<Integer>() {
                                    {
                                        add(100);
                                    }
                                });
                                Whitebox.setInternalState(as, "height_", new ArrayList<Integer>() {
                                    {
                                        add(50);
                                    }
                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 100);
        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("11", openRtbRequest.getImp().get(0).getId());
        assertNotNull(openRtbRequest.getImp().get(0).getVideo());
        assertEquals("Widths: [100]", openRtbRequest.getLot49Ext().getComments().get(0));
        assertEquals(100, openRtbRequest.getImp().get(0).getVideo().getW());
        assertEquals("Heights: [50]", openRtbRequest.getLot49Ext().getComments().get(1));
        assertEquals(50, openRtbRequest.getImp().get(0).getVideo().getH());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpVideoMimeParams() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 100000);
        Whitebox.setInternalState(vid, "minAdDuration_", 10000);
        Whitebox.setInternalState(vid, "allowedVideoFormats_",
                        new ArrayList<NetworkBid.BidRequest.Video.VideoFormat>() {
                            {
                                add(NetworkBid.BidRequest.Video.VideoFormat.VIDEO_MP4);
                            }
                        });
        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("11", openRtbRequest.getImp().get(0).getId());
        assertNotNull(openRtbRequest.getImp().get(0).getVideo());
        assertEquals(10, openRtbRequest.getImp().get(0).getVideo().getMinduration());
        assertEquals(100, openRtbRequest.getImp().get(0).getVideo().getMaxduration());
        assertEquals("video/mp4", openRtbRequest.getImp().get(0).getVideo().getMimes().get(0));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpVideoOtherParams() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals(1, openRtbRequest.getImp().get(0).getVideo().getApi().get(0).intValue());
        assertEquals(2, openRtbRequest.getImp().get(0).getVideo().getApi().get(1).intValue());
        assertEquals(1, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(0).intValue());
        assertEquals(2, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(1).intValue());
        assertEquals(3, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(2).intValue());
        assertEquals(4, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(3).intValue());
        assertEquals(5, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(4).intValue());
        assertEquals(6, openRtbRequest.getImp().get(0).getVideo().getProtocols().get(5).intValue());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_CREATIVETYPE_HTML()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_CREATIVETYPE_HTML);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups().contains(MarkupType.OWN_HTML));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.THIRD_PARTY_HTML));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.THIRD_PARTY_HTML5));
        assertFalse(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.VAST_PLAIN_FLASH_ONLY));

    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_CREATIVETYPE_VASTVIDEO()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_CREATIVETYPE_VASTVIDEO);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.VAST_PLAIN_FLASH_ONLY));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.VAST_PLAIN_MULTIPLE));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.VAST_WRAPPER_PLAIN_FLASH_ONLY));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.VAST_WRAPPER_PLAIN_MULTIPLE));

        assertFalse(openRtbRequest.getImp().get(0).getVideo().getMimes()
                        .contains(Lot49Constants.MEDIA_TYPE_VIDEO_FLV));
        assertFalse(openRtbRequest.getImp().get(0).getVideo().getMimes()
                        .contains(Lot49Constants.MEDIA_TYPE_VIDEO_MP4));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_INSTREAMVASTVIDEOTYPE_VPAID_FLASH()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_INSTREAMVASTVIDEOTYPE_VPAID_FLASH);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertFalse(openRtbRequest.getImp().get(0).getVideo().getMimes()
                        .contains(Lot49Constants.MEDIA_TYPE_APPLICATION_SHOCKWAVE_FLASH));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONSSL()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONSSL);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().isSsl());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.OWN_FLASH));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.THIRD_PARTY_FLASH));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetExcludedMarkups_EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5()
                    throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                Whitebox.setInternalState(as, "excludedAttribute_",
                                                new ArrayList<Integer>() {
                                                    {
                                                        add(AdXConstants.EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);

        NetworkBid.BidRequest.Video vid = NetworkBid.BidRequest.Video.getDefaultInstance();
        Whitebox.setInternalState(vid, "maxAdDuration_", 10000);

        Whitebox.setInternalState(req, "video_", vid);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.OWN_HTML5));
        assertTrue(openRtbRequest.getLot49Ext().getExcludedMarkups()
                        .contains(MarkupType.THIRD_PARTY_HTML5));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionAdxTargeting() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);
                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(new Long(1100), ((AdXTargeting) openRtbRequest.getImp().get(0).getExt()
                        .get("matching_ad_data")).getAdGroupIds().get(0));
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionBidfloor() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                Whitebox.setInternalState(mad, "minimumCpmMicros_", 25000L);

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(new Float(0.025), openRtbRequest.getImp().get(0).getBidfloor());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionDeals() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal
                                                                .getDefaultInstance();
                                Whitebox.setInternalState(dd, "directDealId_", 1234567L);

                                Whitebox.setInternalState(mad, "directDeal_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal>() {
                                                    {
                                                        add(dd);
                                                    }
                                                });

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals("1234567", openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getId());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionDealBidfloor() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal
                                                                .getDefaultInstance();
                                Whitebox.setInternalState(dd, "directDealId_", 1234567L);
                                Whitebox.setInternalState(dd, "fixedCpmMicros_", 38000L);

                                Whitebox.setInternalState(mad, "directDeal_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal>() {
                                                    {
                                                        add(dd);
                                                    }
                                                });

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(new Float(0.038), new Float(
                        openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getBidfloor()));
        assertEquals("USD",
                        openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getBidfloorcur());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionDealType_UNKNOWN_DEAL_TYPE() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal
                                                                .getDefaultInstance();
                                Whitebox.setInternalState(dd, "directDealId_", 1234567L);
                                Whitebox.setInternalState(dd, "dealType_",
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.UNKNOWN_DEAL_TYPE);

                                Whitebox.setInternalState(mad, "directDeal_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal>() {
                                                    {
                                                        add(dd);
                                                    }
                                                });

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(0, openRtbRequest.getImp().get(0).getPmp().getPrivate_auction());
        assertEquals(RtbConstants.AUCTION_TYPE_SECOND_PRICE_PLUS,
                        openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getAt());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionDealType_PREFERRED_DEAL() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal
                                                                .getDefaultInstance();
                                Whitebox.setInternalState(dd, "directDealId_", 1234567L);
                                Whitebox.setInternalState(dd, "dealType_",
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.PREFERRED_DEAL);

                                Whitebox.setInternalState(mad, "directDeal_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal>() {
                                                    {
                                                        add(dd);
                                                    }
                                                });

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(1, openRtbRequest.getImp().get(0).getPmp().getPrivate_auction());
        assertEquals(RtbConstants.AUCTION_TYPE_FIXED_PRICE,
                        openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getAt());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_shouldSetImpressionDealType_PRIVATE_AUCTION() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        List<NetworkBid.BidRequest.AdSlot> adSlotList =
                        new ArrayList<NetworkBid.BidRequest.AdSlot>() {
                            {
                                NetworkBid.BidRequest.AdSlot as =
                                                NetworkBid.BidRequest.AdSlot.getDefaultInstance();
                                Whitebox.setInternalState(as, "id_", 11);
                                NetworkBid.BidRequest.AdSlot.MatchingAdData mad =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData
                                                                .getDefaultInstance();
                                List<Long> biilingIds = new ArrayList<>();
                                biilingIds.add(1100L);
                                Whitebox.setInternalState(mad, "billingId_", biilingIds);

                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal dd =
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal
                                                                .getDefaultInstance();
                                Whitebox.setInternalState(dd, "directDealId_", 1234567L);
                                Whitebox.setInternalState(dd, "dealType_",
                                                NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal.DealType.PRIVATE_AUCTION);

                                Whitebox.setInternalState(mad, "directDeal_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData.DirectDeal>() {
                                                    {
                                                        add(dd);
                                                    }
                                                });

                                Whitebox.setInternalState(as, "matchingAdData_",
                                                new ArrayList<NetworkBid.BidRequest.AdSlot.MatchingAdData>() {
                                                    {
                                                        add(mad);
                                                    }
                                                });
                                add(as);
                            }
                        };

        Whitebox.setInternalState(req, "adslot_", adSlotList);


        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getImp().get(0).getExt().get("matching_ad_data"));
        assertEquals(0, openRtbRequest.getImp().get(0).getPmp().getPrivate_auction());
        assertEquals(RtbConstants.AUCTION_TYPE_SECOND_PRICE_PLUS,
                        openRtbRequest.getImp().get(0).getPmp().getDeals().get(0).getAt());
    }

    @Test
    public void positiveFlow_shouldSetIsTest() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "isTest_", true);

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertTrue(openRtbRequest.getLot49Ext().isTest());

        Whitebox.setInternalState(req, "isTest_", false);

        openRtbRequest = adapter.convertRequest(req);

        assertFalse(openRtbRequest.getLot49Ext().isTest());

    }

    @Test
    public void positiveFlow_shouldSetGoogleUserId() throws Lot49Exception {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        Whitebox.setInternalState(req, "googleUserId_", "someone@gmail.com");

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertEquals("someone@gmail.com", openRtbRequest.getUser().getId());

    }

    @Test
    public void positiveFlow_shouldSetHostedMatchData() {

        NetworkBid.BidRequest req = NetworkBid.BidRequest.getDefaultInstance();

        OpenRtbRequest openRtbRequest = adapter.convertRequest(req);

        assertNull(openRtbRequest.getLot49Ext().getReceivedBuyerUid());

        byte[] bytesOfBSUser = {(byte) 1, (byte) 2, (byte) 3, (byte) 4};
        com.google.protobuf.ByteString bsUser =
                        com.google.protobuf.ByteString.copyFrom(bytesOfBSUser);
        Whitebox.setInternalState(req, "hostedMatchData_", bsUser);

        openRtbRequest = adapter.convertRequest(req);

        assertNotNull(openRtbRequest.getLot49Ext().getReceivedBuyerUid());

    }

    @Test
    public void test_parseRequest() {

        try {
            AdXAdapter adXAdapter = new AdXAdapter(true);
            File adxRequestFile = getTestDataFile("adx" + File.separatorChar + "request1.json");
            NetworkBid.BidRequest bidRequest =
                            NetworkBid.BidRequest.parseFrom(new FileInputStream(adxRequestFile));
            assertNotNull(bidRequest);
            OpenRtbRequest req2 = adXAdapter.convertRequest(bidRequest);
            assertNotNull(req2.getId(), "V8lMj+LT+OeDPvD38tusaw==");
            assertNotNull(req2.getImp());
            assertTrue(!req2.getImp().isEmpty());
            assertNotNull(req2.getImp().get(0));
            assertNotNull(req2.getImp().get(0).getExt());
            assertTrue(!req2.getImp().get(0).getExt().isEmpty());
            AdXTargeting adXTargeting =
                            (AdXTargeting) req2.getImp().get(0).getExt().get("matching_ad_data");
            assertNotNull(adXTargeting);
            assertNotNull(adXTargeting.getAdGroupIds());
            assertTrue(adXTargeting.getAdGroupIds().size() == 1);
            assertNotNull(bidRequest.getAdslot(0));
            assertNotNull(bidRequest.getAdslot(0).getMatchingAdData(0));
            assertNotNull(bidRequest.getAdslot(0).getMatchingAdData(0).getBillingId(0));
            assertEquals(adXTargeting.getAdGroupIds().get(0).longValue(),
                            bidRequest.getAdslot(0).getMatchingAdData(0).getBillingId(0));
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    protected File getTestDataFile(String name) {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd, "src/test/resources/data/" + name);
        return f;
    }
}
